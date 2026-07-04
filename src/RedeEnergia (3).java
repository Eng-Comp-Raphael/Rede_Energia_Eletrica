import estruturaGrafo.Grafo;
import estruturaGrafo.Vertice;
import estruturaGrafo.Aresta;

import java.util.*;

/**
 * PROJETO: Rede de Energia Eletrica
 * Modelagem com Grafos - Estrutura da Equipe
 * 
 * Funcionalidades implementadas (JANINE + LARISSA):
 *   JANINE:
 *     1. Simular queda de uma subestacao
 *     2. Sugerir pontos prioritarios para manutencao
 *   LARISSA:
 *     3. Identificar bairros afetados por uma falha
 *     4. Encontrar caminhos alternativos de distribuicao
 * 
 * Algoritmos utilizados:
 *   - BFS para verificar areas afetadas e caminhos alternativos
 *   - Dijkstra para menor caminho alternativo
 *   - Tarjan (DFS) para identificacao de pontes (conexoes criticas)
 */
public class RedeEnergia {

    private Grafo<String> grafo;
    private Map<String, String> tipos; // id -> tipo (SUBESTACAO, TRANSFORMADOR, BAIRRO)

    public RedeEnergia() {
        this.grafo = new Grafo<>();
        this.tipos = new HashMap<>();
    }

    public void adicionarVertice(String id, String tipo) {
        grafo.addVertice(id);
        this.tipos.put(id, tipo);
    }

    public void adicionarAresta(String origem, String destino, int lambda, String tipo) {
        grafo.addAresta(origem, destino, lambda);
    }

    private String getTipo(String id) {
        return tipos.getOrDefault(id, "DESCONHECIDO");
    }

    // ============================================================
    // METODOS AUXILIARES DE GRAFO
    // ============================================================
    private List<Aresta<String>> getAdjacencias(String id) {
        List<Aresta<String>> adj = new ArrayList<>();
        for (Aresta<String> a : grafo.getArestas()) {
            if (a.getU() != null && a.getV() != null) {
                String u = a.getU().getNome();
                String v = a.getV().getNome();
                if (u.equals(id) || v.equals(id)) {
                    adj.add(a);
                }
            }
        }
        return adj;
    }

    private String getVizinho(Aresta<String> a, String atual) {
        String u = a.getU().getNome();
        String v = a.getV().getNome();
        return u.equals(atual) ? v : u;
    }

    private void bfsEnergizados(String inicio, Set<String> energizados) {
        Queue<String> fila = new LinkedList<>();
        Set<String> visitados = new HashSet<>();
        visitados.add(inicio);
        fila.add(inicio);
        while (!fila.isEmpty()) {
            String atual = fila.poll();
            energizados.add(atual);
            for (Aresta<String> a : getAdjacencias(atual)) {
                String viz = getVizinho(a, atual);
                if (!visitados.contains(viz)) {
                    visitados.add(viz);
                    fila.add(viz);
                }
            }
        }
    }

    // ============================================================
    // JANINE - FUNCIONALIDADE 1: Simular queda de uma subestacao
    // ============================================================
    public void simularQuedaSubestacao(String idSubestacao) {
        System.out.println("\n========================================");
        System.out.println("SIMULACAO: QUEDA DA SUBESTACAO " + idSubestacao);
        System.out.println("========================================");

        if (!"SUBESTACAO".equals(getTipo(idSubestacao))) {
            System.out.println("Subestacao " + idSubestacao + " nao encontrada ou nao e subestacao.");
            return;
        }

        // Remover todas as arestas conectadas a subestacao
        List<Aresta<String>> arestasRemovidas = new ArrayList<>();
        for (Aresta<String> a : new ArrayList<>(grafo.getArestas())) {
            String u = a.getU().getNome();
            String v = a.getV().getNome();
            if (u.equals(idSubestacao) || v.equals(idSubestacao)) {
                arestasRemovidas.add(a);
                grafo.getArestas().remove(a);
            }
        }

        // BFS a partir de todas as outras subestacoes
        Set<String> energizados = new HashSet<>();
        for (String id : grafo.getVertices().keySet()) {
            if ("SUBESTACAO".equals(getTipo(id)) && !id.equals(idSubestacao)) {
                bfsEnergizados(id, energizados);
            }
        }

        // Classificar bairros
        List<String> bairrosComEnergia = new ArrayList<>();
        List<String> bairrosAfetados = new ArrayList<>();
        for (String id : grafo.getVertices().keySet()) {
            if ("BAIRRO".equals(getTipo(id))) {
                if (energizados.contains(id)) {
                    bairrosComEnergia.add(id);
                } else {
                    bairrosAfetados.add(id);
                }
            }
        }

        System.out.println("Subestacao " + idSubestacao + " foi REMOVIDA da rede.");
        System.out.println("\nBAIRROS COM ENERGIA (conectados a outras subestacoes): " + bairrosComEnergia.size());
        for (String b : bairrosComEnergia) {
            System.out.println("  - " + b);
        }

        System.out.println("\nBAIRROS AFETADOS (SEM ENERGIA): " + bairrosAfetados.size());
        if (bairrosAfetados.isEmpty()) {
            System.out.println("  Nenhum! A rede possui redundancia.");
        } else {
            for (String b : bairrosAfetados) {
                System.out.println("  - " + b);
            }
        }

        // Restaurar
        grafo.getArestas().addAll(arestasRemovidas);
    }

    // ============================================================
    // JANINE - FUNCIONALIDADE 2: Sugerir pontos prioritarios
    // ============================================================
    public void sugerirPontosPrioritariosManutencao() {
        System.out.println("\n========================================");
        System.out.println("PONTOS PRIORITARIOS PARA MANUTENCAO");
        System.out.println("========================================");

        List<String> pontes = encontrarPontes();
        System.out.println("\n1. PONTES (conexoes criticas - falha desconecta a rede):");
        if (pontes.isEmpty()) {
            System.out.println("   Nenhuma ponte encontrada. A rede e bem redundante.");
        } else {
            for (String ponte : pontes) {
                System.out.println("   -> " + ponte);
            }
        }

        List<String> verticesCorte = encontrarVerticesDeCorte();
        System.out.println("\n2. VERTICES DE CORTE (pontos criticos da rede):");
        if (verticesCorte.isEmpty()) {
            System.out.println("   Nenhum vertice de corte encontrado.");
        } else {
            for (String v : verticesCorte) {
                System.out.println("   -> " + v);
            }
        }

        System.out.println("\n3. RANKING DE PRIORIDADE POR CONECTIVIDADE:");
        List<String> ids = new ArrayList<>(grafo.getVertices().keySet());
        ids.sort((a, b) -> Integer.compare(getAdjacencias(b).size(), getAdjacencias(a).size()));
        for (int i = 0; i < Math.min(5, ids.size()); i++) {
            String id = ids.get(i);
            System.out.println("   " + (i+1) + ". " + id + "(" + getTipo(id) + ") - " + getAdjacencias(id).size() + " conexoes");
        }

        System.out.println("\n4. CONEXOES MAIS LONGAS (maior distancia = maior risco):");
        List<Aresta<String>> rankingArestas = new ArrayList<>(grafo.getArestas());
        rankingArestas.sort((a, b) -> Integer.compare(b.getLambda(), a.getLambda()));
        for (int i = 0; i < Math.min(5, rankingArestas.size()); i++) {
            Aresta<String> a = rankingArestas.get(i);
            System.out.println("   " + (i+1) + ". [" + a.getU().getNome() + "] -> [" + a.getV().getNome() + "], " + a.getLambda() + "km");
        }
    }

    // Algoritmo de Tarjan para encontrar pontes
    private List<String> encontrarPontes() {
        List<String> pontes = new ArrayList<>();
        Map<String, Integer> disc = new HashMap<>();
        Map<String, Integer> low = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        int[] time = {0};

        for (String id : grafo.getVertices().keySet()) {
            disc.put(id, -1);
            low.put(id, -1);
            parent.put(id, null);
        }

        for (String id : grafo.getVertices().keySet()) {
            if (disc.get(id) == -1) {
                dfsPontes(id, disc, low, parent, time, pontes);
            }
        }
        return pontes;
    }

    private void dfsPontes(String u, Map<String, Integer> disc, Map<String, Integer> low,
                           Map<String, String> parent, int[] time, List<String> pontes) {
        disc.put(u, time[0]);
        low.put(u, time[0]);
        time[0]++;

        for (Aresta<String> a : getAdjacencias(u)) {
            String v = getVizinho(a, u);
            if (disc.get(v) == -1) {
                parent.put(v, u);
                dfsPontes(v, disc, low, parent, time, pontes);
                low.put(u, Math.min(low.get(u), low.get(v)));

                if (low.get(v) > disc.get(u)) {
                    pontes.add(u + " -- " + v + " (dist: " + a.getLambda() + "km)");
                }
            } else if (!v.equals(parent.get(u))) {
                low.put(u, Math.min(low.get(u), disc.get(v)));
            }
        }
    }

    // Algoritmo para encontrar Vertices de Corte
    private List<String> encontrarVerticesDeCorte() {
        List<String> verticesCorte = new ArrayList<>();
        Map<String, Integer> disc = new HashMap<>();
        Map<String, Integer> low = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        Map<String, Boolean> ap = new HashMap<>();
        int[] time = {0};

        for (String id : grafo.getVertices().keySet()) {
            disc.put(id, -1);
            low.put(id, -1);
            parent.put(id, null);
            ap.put(id, false);
        }

        for (String id : grafo.getVertices().keySet()) {
            if (disc.get(id) == -1) {
                dfsVerticesCorte(id, disc, low, parent, ap, time);
            }
        }

        for (String id : grafo.getVertices().keySet()) {
            if (ap.get(id)) {
                verticesCorte.add(id + " (tipo: " + getTipo(id) + ")");
            }
        }
        return verticesCorte;
    }

    private void dfsVerticesCorte(String u, Map<String, Integer> disc, Map<String, Integer> low,
                                  Map<String, String> parent, Map<String, Boolean> ap, int[] time) {
        int children = 0;
        disc.put(u, time[0]);
        low.put(u, time[0]);
        time[0]++;

        for (Aresta<String> a : getAdjacencias(u)) {
            String v = getVizinho(a, u);
            if (disc.get(v) == -1) {
                children++;
                parent.put(v, u);
                dfsVerticesCorte(v, disc, low, parent, ap, time);
                low.put(u, Math.min(low.get(u), low.get(v)));

                if (parent.get(u) == null && children > 1) {
                    ap.put(u, true);
                }
                if (parent.get(u) != null && low.get(v) >= disc.get(u)) {
                    ap.put(u, true);
                }
            } else if (!v.equals(parent.get(u))) {
                low.put(u, Math.min(low.get(u), disc.get(v)));
            }
        }
    }

    // ============================================================
    // LARISSA - FUNCIONALIDADE 3: Identificar bairros afetados
    // ============================================================
    public void identificarBairrosAfetados(String idFalha, String tipoFalha) {
        System.out.println("\n========================================");
        System.out.println("LARISSA: IDENTIFICAR BAIRROS AFETADOS");
        System.out.println("Falha: " + tipoFalha + " " + idFalha);
        System.out.println("========================================");

        if (!grafo.getVertices().containsKey(idFalha)) {
            System.out.println("Erro: " + idFalha + " nao encontrado na rede.");
            return;
        }

        List<Aresta<String>> arestasRemovidas = new ArrayList<>();
        
        if (tipoFalha.equalsIgnoreCase("VERTICE")) {
            for (Aresta<String> a : new ArrayList<>(grafo.getArestas())) {
                String u = a.getU().getNome();
                String v = a.getV().getNome();
                if (u.equals(idFalha) || v.equals(idFalha)) {
                    arestasRemovidas.add(a);
                    grafo.getArestas().remove(a);
                }
            }
        } else if (tipoFalha.equalsIgnoreCase("ARESTA")) {
            List<Aresta<String>> adj = getAdjacencias(idFalha);
            if (!adj.isEmpty()) {
                Aresta<String> a = adj.get(0);
                System.out.println("Aresta removida: " + a.getU().getNome() + " -> " + a.getV().getNome());
                arestasRemovidas.add(a);
                grafo.getArestas().remove(a);
            }
        }

        // BFS a partir de todas as subestacoes
        Set<String> energizados = new HashSet<>();
        for (String id : grafo.getVertices().keySet()) {
            if ("SUBESTACAO".equals(getTipo(id))) {
                bfsEnergizados(id, energizados);
            }
        }

        // Listar bairros afetados
        List<String> bairrosAfetados = new ArrayList<>();
        for (String id : grafo.getVertices().keySet()) {
            if ("BAIRRO".equals(getTipo(id)) && !energizados.contains(id)) {
                bairrosAfetados.add(id);
            }
        }

        System.out.println("BAIRROS AFETADOS: " + bairrosAfetados.size());
        if (bairrosAfetados.isEmpty()) {
            System.out.println("  Nenhum bairro afetado! A rede possui redundancia.");
        } else {
            for (String b : bairrosAfetados) {
                System.out.println("  - " + b);
            }
        }

        // Restaurar
        grafo.getArestas().addAll(arestasRemovidas);
    }

    // ============================================================
    // LARISSA - FUNCIONALIDADE 4: Encontrar caminhos alternativos
    // ============================================================
    public void encontrarCaminhosAlternativos(String idBairro) {
        System.out.println("\n========================================");
        System.out.println("LARISSA: CAMINHOS ALTERNATIVOS");
        System.out.println("Bairro: " + idBairro);
        System.out.println("========================================");

        if (!"BAIRRO".equals(getTipo(idBairro))) {
            System.out.println("Erro: " + idBairro + " nao e um bairro valido.");
            return;
        }

        boolean encontrou = false;
        for (String id : grafo.getVertices().keySet()) {
            if (!"SUBESTACAO".equals(getTipo(id))) continue;

            List<String> caminho = dijkstra(id, idBairro);
            if (caminho != null && !caminho.isEmpty()) {
                encontrou = true;
                System.out.println("\nCaminho alternativo de " + id + ":");
                System.out.print("  ");
                for (int i = 0; i < caminho.size(); i++) {
                    System.out.print(caminho.get(i));
                    if (i < caminho.size() - 1) System.out.print(" -> ");
                }
                System.out.println();
            }
        }

        if (!encontrou) {
            System.out.println("Nao foi encontrado caminho alternativo para " + idBairro);
            System.out.println("O bairro ficara sem energia ate a falha ser corrigida.");
        }
    }

    // Algoritmo de Dijkstra para encontrar o menor caminho
    private List<String> dijkstra(String origem, String destino) {
        Map<String, Integer> distancia = new HashMap<>();
        Map<String, String> predecessor = new HashMap<>();
        Set<String> visitados = new HashSet<>();

        for (String id : grafo.getVertices().keySet()) {
            distancia.put(id, Integer.MAX_VALUE);
            predecessor.put(id, null);
        }
        distancia.put(origem, 0);

        while (visitados.size() < grafo.getVertices().size()) {
            String u = null;
            int minDist = Integer.MAX_VALUE;
            for (String id : grafo.getVertices().keySet()) {
                if (!visitados.contains(id) && distancia.get(id) < minDist) {
                    minDist = distancia.get(id);
                    u = id;
                }
            }

            if (u == null) break;
            visitados.add(u);

            if (u.equals(destino)) break;

            for (Aresta<String> a : getAdjacencias(u)) {
                String v = getVizinho(a, u);
                if (!visitados.contains(v)) {
                    int novaDist = distancia.get(u) + a.getLambda();
                    if (novaDist < distancia.get(v)) {
                        distancia.put(v, novaDist);
                        predecessor.put(v, u);
                    }
                }
            }
        }

        if (distancia.get(destino) == Integer.MAX_VALUE) {
            return null;
        }

        List<String> caminho = new ArrayList<>();
        String atual = destino;
        while (atual != null) {
            caminho.add(0, atual);
            atual = predecessor.get(atual);
        }
        return caminho;
    }

    // ============================================================
    // LARISSA - BONUS: Analise completa de falha
    // ============================================================
    public void analiseCompletaFalha(String idFalha) {
        System.out.println("\n========================================");
        System.out.println("LARISSA: ANALISE COMPLETA DE FALHA");
        System.out.println("Falha na subestacao: " + idFalha);
        System.out.println("========================================");

        if (!"SUBESTACAO".equals(getTipo(idFalha))) {
            System.out.println("Subestacao invalida.");
            return;
        }

        // Remove a subestacao
        List<Aresta<String>> arestasRemovidas = new ArrayList<>();
        for (Aresta<String> a : new ArrayList<>(grafo.getArestas())) {
            String u = a.getU().getNome();
            String v = a.getV().getNome();
            if (u.equals(idFalha) || v.equals(idFalha)) {
                arestasRemovidas.add(a);
                grafo.getArestas().remove(a);
            }
        }

        // BFS para encontrar energizados
        Set<String> energizados = new HashSet<>();
        for (String id : grafo.getVertices().keySet()) {
            if ("SUBESTACAO".equals(getTipo(id)) && !id.equals(idFalha)) {
                bfsEnergizados(id, energizados);
            }
        }

        // Bairros afetados
        List<String> bairrosAfetados = new ArrayList<>();
        for (String id : grafo.getVertices().keySet()) {
            if ("BAIRRO".equals(getTipo(id)) && !energizados.contains(id)) {
                bairrosAfetados.add(id);
            }
        }

        System.out.println("Total de bairros afetados: " + bairrosAfetados.size());

        for (String bairro : bairrosAfetados) {
            System.out.println("\n  Bairro afetado: " + bairro);
            
            boolean encontrou = false;
            for (String se : grafo.getVertices().keySet()) {
                if (!"SUBESTACAO".equals(getTipo(se)) || se.equals(idFalha)) continue;
                
                List<String> caminho = dijkstra(se, bairro);
                if (caminho != null && !caminho.isEmpty()) {
                    encontrou = true;
                    System.out.print("    Caminho alternativo de " + se + ": ");
                    for (int i = 0; i < caminho.size(); i++) {
                        System.out.print(caminho.get(i));
                        if (i < caminho.size() - 1) System.out.print(" -> ");
                    }
                    System.out.println();
                }
            }
            if (!encontrou) {
                System.out.println("    NENHUM CAMINHO ALTERNATIVO DISPONIVEL!");
            }
        }

        // Restaurar
        grafo.getArestas().addAll(arestasRemovidas);
    }

    // ============================================================
    // METODO MAIN - Demonstracao completa
    // ============================================================
    public static void main(String[] args) {
        RedeEnergia rede = new RedeEnergia();

        // --- VERTICES ---
        rede.adicionarVertice("SE-Norte", "SUBESTACAO");
        rede.adicionarVertice("SE-Sul", "SUBESTACAO");
        rede.adicionarVertice("T1", "TRANSFORMADOR");
        rede.adicionarVertice("T2", "TRANSFORMADOR");
        rede.adicionarVertice("T3", "TRANSFORMADOR");
        rede.adicionarVertice("T4", "TRANSFORMADOR");
        rede.adicionarVertice("Bairro-Centro", "BAIRRO");
        rede.adicionarVertice("Bairro-Leste", "BAIRRO");
        rede.adicionarVertice("Bairro-Oeste", "BAIRRO");
        rede.adicionarVertice("Bairro-Novo", "BAIRRO");
        rede.adicionarVertice("Bairro-Velho", "BAIRRO");
        rede.adicionarVertice("Bairro-Sul", "BAIRRO");

        // --- ARESTAS ---
        rede.adicionarAresta("SE-Norte", "T1", 2, "LINHA");
        rede.adicionarAresta("SE-Norte", "T2", 3, "LINHA");
        rede.adicionarAresta("SE-Norte", "Bairro-Centro", 1, "CONEXAO");
        rede.adicionarAresta("SE-Sul", "T3", 2, "LINHA");
        rede.adicionarAresta("SE-Sul", "T4", 2, "LINHA");
        rede.adicionarAresta("SE-Sul", "Bairro-Sul", 1, "CONEXAO");
        rede.adicionarAresta("T1", "Bairro-Leste", 1, "CONEXAO");
        rede.adicionarAresta("T2", "Bairro-Oeste", 1, "CONEXAO");
        rede.adicionarAresta("T2", "Bairro-Novo", 2, "CONEXAO");
        rede.adicionarAresta("T3", "Bairro-Novo", 1, "CONEXAO");
        rede.adicionarAresta("T4", "Bairro-Velho", 1, "CONEXAO");
        rede.adicionarAresta("T1", "T2", 1, "LINHA");
        rede.adicionarAresta("T3", "T4", 1, "LINHA");
        rede.adicionarAresta("T2", "T3", 4, "LINHA");

        // ============================================================
        // DEMONSTRACAO JANINE
        // ============================================================
        rede.simularQuedaSubestacao("SE-Norte");
        rede.simularQuedaSubestacao("SE-Sul");
        rede.sugerirPontosPrioritariosManutencao();

        // ============================================================
        // DEMONSTRACAO LARISSA
        // ============================================================
        System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("  INICIO DAS FUNCIONALIDADES LARISSA");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");

        // 3. Identificar bairros afetados por queda de subestacao
        rede.identificarBairrosAfetados("SE-Norte", "VERTICE");

        // 3. Identificar bairros afetados por queda de transformador
        rede.identificarBairrosAfetados("T2", "VERTICE");

        // 4. Encontrar caminhos alternativos para um bairro especifico
        rede.encontrarCaminhosAlternativos("Bairro-Novo");
        rede.encontrarCaminhosAlternativos("Bairro-Centro");
        rede.encontrarCaminhosAlternativos("Bairro-Sul");

        // BONUS: Analise completa
        rede.analiseCompletaFalha("SE-Norte");

        System.out.println("\n========================================");
        System.out.println("FIM DAS SIMULACOES (JANINE + LARISSA)");
        System.out.println("========================================");
    }
}
