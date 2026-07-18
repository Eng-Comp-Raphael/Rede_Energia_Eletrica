package visualizacao;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Descobre a cidade e o bairro de um ponto do mapa via geocodificação reversa (Nominatim/OpenStreetMap),
 * para que o relatório aponte a localização real de cada falha em vez da subestação mais próxima.
 * Resultados ficam em cache por coordenada e qualquer falha de rede cai de volta em "não identificada"
 * sem travar o relatório.
 */
public class GeocodificadorReverso {

    public static class Endereco {
        public final String cidade;
        public final String bairro;

        public Endereco(String cidade, String bairro) {
            this.cidade = cidade;
            this.bairro = bairro;
        }

        public String descricao() {
            if (cidade == null && bairro == null) {
                return "Localização não identificada";
            }
            if (bairro == null) {
                return cidade;
            }
            if (cidade == null) {
                return bairro;
            }
            return bairro + " - " + cidade;
        }
    }

    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    private static final Map<String, Endereco> cache = new HashMap<>();

    public static Endereco buscar(double lat, double lon) {
        String chave = String.format(Locale.US, "%.4f,%.4f", lat, lon);
        Endereco emCache = cache.get(chave);
        if (emCache != null) {
            return emCache;
        }

        Endereco resultado = new Endereco(null, null);
        try {
            String url = String.format(Locale.US,
                    "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=%f&lon=%f&zoom=16&addressdetails=1",
                    lat, lon);
            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(4))
                    .header("User-Agent", "RedeDeEnergia-TrabalhoFinal-EstruturaDeDados/1.0")
                    .GET()
                    .build();
            HttpResponse<String> resposta = CLIENTE.send(requisicao, HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() == 200) {
                resultado = extrair(resposta.body());
            }
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            // Sem internet ou serviço indisponível: mantém "localização não identificada".
        }

        cache.put(chave, resultado);
        return resultado;
    }

    private static Endereco extrair(String json) {
        String cidade = primeiroCampo(json, "city", "town", "village", "municipality");
        String bairro = primeiroCampo(json, "suburb", "neighbourhood", "quarter", "city_district");
        return new Endereco(cidade, bairro);
    }

    private static String primeiroCampo(String json, String... chaves) {
        for (String chave : chaves) {
            Matcher m = Pattern.compile("\"" + chave + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        }
        return null;
    }
}
