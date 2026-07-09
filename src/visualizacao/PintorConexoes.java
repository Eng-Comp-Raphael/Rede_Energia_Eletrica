package visualizacao;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PintorConexoes implements Painter<JXMapViewer> {

    public enum TipoVertice {
        SUBESTACAO, POSTE, CASA
    }

    public static class VerticeVis {
        public String nome;
        public GeoPosition posicao;
        public TipoVertice tipo;

        public VerticeVis(String nome, GeoPosition posicao, TipoVertice tipo) {
            this.nome = nome;
            this.posicao = posicao;
            this.tipo = tipo;
        }
    }
    
    private Map<String, VerticeVis> vertices;
    private List<ConexaoVis> arestas;
    private double progressoAnimacao = 0.0;
    private final JXMapViewer mapa;

    private String verticeDestaque = null;
    private Set<String> arestasDestacadas = new HashSet<>();
    private Set<String> verticesDestacados = new HashSet<>();
    private Map<String, Integer> ordemVisitaAlgoritmo = new HashMap<>();

    private Set<String> verticesSemEnergia = new HashSet<>();
    private Set<String> arestasSemEnergia = new HashSet<>();
    private Set<String> verticesInativosManuais = new HashSet<>();
    private Map<String, Integer> distanciasSubestacao = new HashMap<>();

    private Color corDestaque = Color.GREEN;
    private boolean mostrarApenasDestaques = false;

    public PintorConexoes(JXMapViewer mapa) {
        this.mapa = mapa;
        this.vertices = new HashMap<>();
        this.arestas = new ArrayList<>();
    }

    public void adicionarVertice(String nome, GeoPosition posicao, TipoVertice tipo) {
        vertices.put(nome, new VerticeVis(nome, posicao, tipo));
    }

    public void removerVertice(String nome) {
        if (vertices.containsKey(nome)) {
            vertices.remove(nome);
            arestas.removeIf(aresta -> aresta.origem.equals(nome) || aresta.destino.equals(nome));
            arestasDestacadas.removeIf(id -> id.contains(nome));
            verticesDestacados.remove(nome);
            ordemVisitaAlgoritmo.remove(nome);
            if (nome.equals(verticeDestaque))
                verticeDestaque = null;
        }
    }

    public void adicionarAresta(String origem, String destino, int peso) {
        for (ConexaoVis c : arestas) {
            if ((c.origem.equals(origem) && c.destino.equals(destino)) ||
                    (c.origem.equals(destino) && c.destino.equals(origem)))
                return;
        }
        if (vertices.containsKey(origem) && vertices.containsKey(destino)) {
            arestas.add(new ConexaoVis(origem, destino, peso));
        }
    }

    public Map<String, VerticeVis> getVertices() {
        return vertices;
    }

    public void setVerticeDestaque(String nome) {
        this.verticeDestaque = nome;
    }

    public void destacarAresta(String origem, String destino) {
        arestasDestacadas.add(origem + "-" + destino);
        arestasDestacadas.add(destino + "-" + origem);
    }

    public void destacarVertice(String nome) {
        verticesDestacados.add(nome);
    }

    public void definirOrdemVisita(String nome, int ordem) {
        ordemVisitaAlgoritmo.put(nome, ordem);
        verticesDestacados.add(nome);
    }

    public void setEstadoEnergia(Set<String> semEnergiaV, Set<String> semEnergiaA, Set<String> inativos, Map<String, Integer> distancias) {
        this.verticesSemEnergia = semEnergiaV;
        this.arestasSemEnergia = semEnergiaA;
        this.verticesInativosManuais = inativos;
        this.distanciasSubestacao = distancias; 
    }

    public void setCorDestaque(Color cor) {
        this.corDestaque = cor;
    }

    public void setMostrarApenasDestaques(boolean mostrar) {
        this.mostrarApenasDestaques = mostrar;
    }

    public void limparDestaquesAlgoritmos() {
        arestasDestacadas.clear();
        verticesDestacados.clear();
        ordemVisitaAlgoritmo.clear();
        mostrarApenasDestaques = false;
    }

    public void limparTudo() {
        vertices.clear();
        arestas.clear();
        distanciasSubestacao.clear();
        limparDestaquesAlgoritmos();
        verticesSemEnergia.clear();
        arestasSemEnergia.clear();
        verticesInativosManuais.clear();
        verticeDestaque = null;
    }

    public String getVerticeProximo(Point2D pontoTela, JXMapViewer map, int raioClique) {
        Rectangle bounds = map.getViewportBounds();
        for (Map.Entry<String, VerticeVis> entry : vertices.entrySet()) {
            Point2D pt = map.getTileFactory().geoToPixel(entry.getValue().posicao, map.getZoom());
            int x = (int) (pt.getX() - bounds.getX());
            int y = (int) (pt.getY() - bounds.getY());
            if (pontoTela.distance(x, y) <= raioClique)
                return entry.getKey();
        }
        return null;
    }

    public void atualizarAnimacao() {
        progressoAnimacao += 0.015;
        if (progressoAnimacao > 1.0)
            progressoAnimacao = 0.0;
    }

    private void desenharRaioFalha(Graphics2D g, int cx, int cy, float alpha, float escala) {
        Composite compositeOriginal = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                Math.max(0f, Math.min(1f, alpha))));

        int[] xs = {2, 2, 8, -2, -2, -8};
        int[] ys = {-10, -2, -2, 10, 2, 2};
        Polygon raio = new Polygon();
        for (int i = 0; i < xs.length; i++) {
            raio.addPoint(cx + Math.round(xs[i] * escala), cy + Math.round(ys[i] * escala));
        }

        g.setColor(Color.RED);
        g.fillPolygon(raio);
        g.setColor(Color.BLACK);
        g.drawPolygon(raio);

        g.setComposite(compositeOriginal);
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        g = (Graphics2D) g.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Set<String> verticesParaMostrar = new HashSet<>(verticesDestacados);
        for (String idAresta : arestasDestacadas) {
            String[] partes = idAresta.split("-");
            if (partes.length == 2) {
                verticesParaMostrar.add(partes[0]);
                verticesParaMostrar.add(partes[1]);
            }
        }

        for (ConexaoVis aresta : arestas) {
            String idAresta = aresta.origem + "-" + aresta.destino;
            String idArestaInverso = aresta.destino + "-" + aresta.origem;
            boolean isDestacada = arestasDestacadas.contains(idAresta) || arestasDestacadas.contains(idArestaInverso);
            boolean isSemEnergia = arestasSemEnergia.contains(idAresta) || arestasSemEnergia.contains(idArestaInverso);

            if (mostrarApenasDestaques) {
                boolean ambosDestacados = verticesDestacados.contains(aresta.origem)
                        && verticesDestacados.contains(aresta.destino);
                if (!isDestacada && !ambosDestacados)
                    continue;
            }

            GeoPosition posOri = vertices.get(aresta.origem).posicao;
            GeoPosition posDes = vertices.get(aresta.destino).posicao;
            Point2D ptOri = map.getTileFactory().geoToPixel(posOri, map.getZoom());
            Point2D ptDes = map.getTileFactory().geoToPixel(posDes, map.getZoom());
            Rectangle bounds = map.getViewportBounds();
            int x1 = (int) (ptOri.getX() - bounds.getX());
            int y1 = (int) (ptOri.getY() - bounds.getY());
            int x2 = (int) (ptDes.getX() - bounds.getX());
            int y2 = (int) (ptDes.getY() - bounds.getY());

            boolean tocaVerticeQuebrado = verticesInativosManuais.contains(aresta.origem)
                    || verticesInativosManuais.contains(aresta.destino);

            if (isSemEnergia) {
                float faseTraço = (float) (progressoAnimacao * 40);
                g.setColor(Color.RED);
                g.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        10, new float[] { 12f, 8f }, faseTraço));
            } else if (isDestacada) {
                if (corDestaque == Color.MAGENTA)
                    g.setColor(Color.DARK_GRAY);
                else
                    g.setColor(corDestaque);
                g.setStroke(new BasicStroke(5));
            } else {
                g.setColor(Color.DARK_GRAY);
                g.setStroke(new BasicStroke(3));
            }

            g.drawLine(x1, y1, x2, y2);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(aresta.peso + "m", (x1 + x2) / 2, (y1 + y2) / 2);

            if (isSemEnergia) {
                int midX = (x1 + x2) / 2;
                int midY = (y1 + y2) / 2;
                if (tocaVerticeQuebrado) {
                    float alpha = (float) (0.5 + 0.5 * Math.sin(progressoAnimacao * Math.PI * 8));
                    desenharRaioFalha(g, midX, midY, alpha, 1.3f);
                } else {
                    float alpha = (float) (0.3 + 0.3 * Math.sin(progressoAnimacao * Math.PI * 2));
                    desenharRaioFalha(g, midX, midY, alpha, 0.8f);
                }
            } else if (!isDestacada || corDestaque == Color.GREEN || corDestaque == Color.MAGENTA) {
                int dOrigem = distanciasSubestacao.getOrDefault(aresta.origem, 9999);
                int dDestino = distanciasSubestacao.getOrDefault(aresta.destino, 9999);

                int startX = x1, startY = y1;
                int endX = x2, endY = y2;
                
                if (dOrigem > dDestino) {
                    startX = x2;
                    startY = y2;
                    endX = x1;
                    endY = y1;
                }

                int currentX = (int) (startX + (endX - startX) * progressoAnimacao);
                int currentY = (int) (startY + (endY - startY) * progressoAnimacao);
                g.setColor(Color.YELLOW);
                g.fillOval(currentX - 6, currentY - 6, 12, 12);
            }
        }

        g.setFont(new Font("Arial", Font.BOLD, 10));

        for (Map.Entry<String, VerticeVis> entry : vertices.entrySet()) {
            String nomeVertice = entry.getKey();
            if (mostrarApenasDestaques && !verticesParaMostrar.contains(nomeVertice))
                continue;

            VerticeVis v = entry.getValue();
            Point2D pt = map.getTileFactory().geoToPixel(v.posicao, map.getZoom());
            Rectangle bounds = map.getViewportBounds();
            int x = (int) (pt.getX() - bounds.getX());
            int y = (int) (pt.getY() - bounds.getY());

            // TAMANHOS AJUSTADOS CONFORME SEU PEDIDO
            int tamanho = 14; // POSTE (tamanho antigo das casas)
            Color cor = Color.ORANGE;

            if (v.tipo == TipoVertice.SUBESTACAO) {
                tamanho = 26; // SUBESTAÇÃO (tamanho original)
                cor = new Color(138, 43, 226);
            } else if (v.tipo == TipoVertice.CASA) {
                tamanho = 10; // CASA (um pouco menor que o poste)
                cor = new Color(30, 144, 255);
            }

            boolean isSemEnergia = verticesSemEnergia.contains(nomeVertice);
            boolean isQuebrado = verticesInativosManuais.contains(nomeVertice);

            // Borda de seleção quando destacado por algum algoritmo
            if (verticesDestacados.contains(nomeVertice) && corDestaque != Color.MAGENTA) {
                g.setColor(corDestaque);
                g.fillOval(x - (tamanho / 2 + 6), y - (tamanho / 2 + 6), tamanho + 12, tamanho + 12);
            }

            // Borda de destaque quando o poste está quebrado
            if (isQuebrado) {
                g.setColor(Color.BLACK);
                g.fillOval(x - (tamanho / 2 + 6), y - (tamanho / 2 + 6), tamanho + 12, tamanho + 12);
                cor = Color.RED;
            } else if (isSemEnergia) {
                cor = Color.RED;
            }

            // Borda azul clara para vértice selecionado manualmente
            if (nomeVertice.equals(verticeDestaque)) {
                g.setColor(Color.CYAN);
                g.fillOval(x - (tamanho / 2 + 4), y - (tamanho / 2 + 4), tamanho + 8, tamanho + 8);
            }

            // Círculo principal do nó
            g.setColor(cor);
            g.fillOval(x - tamanho / 2, y - tamanho / 2, tamanho, tamanho);
            
            // Bordinha sutil para dar acabamento
            g.setColor(Color.DARK_GRAY);
            g.drawOval(x - tamanho / 2, y - tamanho / 2, tamanho, tamanho);

            // Mantém apenas a renderização da caixinha com o número (#1, #2)
            // para as buscas BFS/DFS, ignorando os rótulos de nome fixos.
            if (ordemVisitaAlgoritmo.containsKey(nomeVertice)) {
                String numOrdem = "#" + ordemVisitaAlgoritmo.get(nomeVertice);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fmOrdem = g.getFontMetrics();

                int boxWidth = fmOrdem.stringWidth(numOrdem) + 8;
                int boxHeight = fmOrdem.getHeight() + 2;

                int boxX = x + (tamanho / 2) + 2;
                int boxY = y - (tamanho / 2) - boxHeight + 4;

                g.setColor(new Color(255, 255, 255, 240));
                g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 4, 4);
                g.setColor(Color.DARK_GRAY);
                g.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 4, 4);
                g.setColor(Color.BLACK);
                g.drawString(numOrdem, boxX + 4, boxY + fmOrdem.getAscent() + 1);
                g.setFont(new Font("Arial", Font.BOLD, 10));
            }
        }
        g.dispose();
    }

    private static class ConexaoVis {
        String origem;
        String destino;
        int peso;

        public ConexaoVis(String origem, String destino, int peso) {
            this.origem = origem;
            this.destino = destino;
            this.peso = peso;
        }
    }
}