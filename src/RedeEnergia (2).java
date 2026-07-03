import java.util.*;

/**
 * ============================================================
 * PROJETO: Rede de Energia Eletrica - Modelagem com Grafos
 * ============================================================
 * Modelagem da Equipe:
 *   Vertices: Subestacoes, Transformadores, Bairros
 *   Arestas:  Linhas eletricas ou Conexoes de distribuicao
 *   Pesos:    Distancia (km)
 *   Capacidade: Fluxo maximo (MW)
 *
 * Algoritmos implementados:
 *   1. DFS/BFS - verificar areas afetadas por falha
 *   2. Edmonds-Karp - fluxo maximo (Marcos)
 *   3. Kruskal - arvore geradora minima (Raphael)
 *   4. Tarjan - identificacao de pontes (Isaias)
 *
 * Funcionalidades:
 *   Janine:  Simular queda de subestacao
 *            Sugerir pontos prioritarios para manutencao
 *   Larissa: Identificar bairros afetados
 *            Encontrar caminhos alternativos
 * ============================================================
 */
public class RedeEnergia {

    // ============================================================
    // CLASSE VERTICE
    // ============================================================
    static class Vertice {
        String id;
        String tipo; // SUBESTACAO, TRANSFORMADOR, BAIRRO
        List<Aresta> adjacencias;
        boolean visitado;

        public Vertice(String id, String tipo) {
            this.id = id;
            this.tipo = tipo;
            this.adjacencias = new ArrayList<>();
            this.visitado = false;
        }

        @Override
        public String toString() {
            return id + "(" + tipo + ")";
        }
    }

    // ============================================================
    // CLASSE ARESTA
    // ============================================================
    static class Aresta {
        Vertice origem;
        Vertice destino;
        double peso;        // Distancia em km
        double capacidade;  // Capacidade em MW (para fluxo maximo)
        String tipo;        // LINHA ou CONEXAO

        public Aresta(Vertice origem, Vertice destino, double peso, double capacidade, String tipo) {
            this.origem = origem;
            this.destino = destino;
            this.peso = peso;
            this.capacidade = capacidade;
            this.tipo = tipo;
        }

        @Override
        public String toString() {
            return origem.id + " -> " + destino.id + " [" + peso + "km, " + capacidade + "MW]";
        }
    }

    // ============================================================
    // CLASSE GRAFO
    // ============================================================
    static class Grafo {
        List<Vertice> vertices;
        List<Aresta> arestas;

        public Grafo() {
            this.vertices = new ArrayList<>();
            this.arestas = new ArrayList<>();
        }

        public Vertice adicionarVertice(String id, String tipo) {
            Vertice v = new Vertice(id, tipo);
            vertices.add(v);
            return v;
        }

        public void adicionarAresta(String idOrigem, String idDestino, double peso, double capacidade, String tipo) {
            Vertice origem = buscarVertice(idOrigem);
            Vertice destino = buscarVertice(idDestino);
            if (origem == null || destino == null) {
                System.out.println("Erro: vertice nao encontrado: " + idOrigem + " ou " + idDestino);
                return;
            }
            Aresta a1 = new Aresta(origem, destino, peso, capacidade, tipo);
            Aresta a2 = new Aresta(destino, origem, peso, capacidade, tipo);
            origem.adjacencias.add(a1);
            destino.adjacencias.add(a2);
            arestas.add(a1);
        }

        public Vertice buscarVertice(String id) {
            for (Vertice v : vertices) {
                if (v.id.equals(id)) return v;
            }
            return null;
        }

        // ============================================================
        // ALGORITMO: DFS/BFS
        // Verificar quais areas sao afetadas por uma falha
        // ============================================================
        public Set<String> verificarAreasAfetadas(Vertice origem) {
            Set<String> alcancados = new HashSet<>();
            Queue<Vertice> fila = new LinkedList<>();
            origem.visitado = true;
            fila.add(origem);
            while (!fila.isEmpty()) {
                Vertice atual = fila.poll();
                alcancados.add(atual.id);
                for (Aresta a : atual.adjacencias) {
                    Vertice viz = a.destino;
                    if (!viz.visitado) {
                        viz.visitado = true;
                        fila.add(viz);
                    }
                }
            }
            for (Vertice v : vertices) v.visitado = false;
            return alcancados;
        }

        // ============================================================
        // ALGORITMO: Edmonds-Karp (Fluxo Maximo) - MARCOS
        // ============================================================
        public double fluxoMaximo(String idOrigem, String idDestino) {
            Vertice origem = buscarVertice(idOrigem);
            Vertice destino = buscarVertice(idDestino);
            if (origem == null || destino == null) {
                System.out.println("Vertice nao encontrado para fluxo maximo.");
                return 0;
            }

            // Mapa de capacidades residuais
            Map<String, Map<String, Double>> capacidade = new HashMap<>();
            for (Vertice v : vertices) {
                capacidade.put(v.id, new HashMap<>());
                for (Vertice u : vertices) {
                    capacidade.get(v.id).put(u.id, 0.0);
                }
            }

            for (Aresta a : arestas) {
                capacidade.get(a.origem.id).put(a.destino.id, a.capacidade);
            }

            double fluxoMaximo = 0;
            Map<String, String> pai = new HashMap<>();

            while (bfsFluxo(origem, destino, capacidade, pai)) {
                double fluxoCaminho = Double.MAX_VALUE;
                String v = destino.id;
                while (!v.equals(origem.id)) {
                    String u = pai.get(v);
                    fluxoCaminho = Math.min(fluxoCaminho, capacidade.get(u).get(v));
                    v = u;
                }

                v = destino.id;
                while (!v.equals(origem.id)) {
                    String u = pai.get(v);
                    capacidade.get(u).put(v, capacidade.get(u).get(v) - fluxoCaminho);
                    capacidade.get(v).put(u, capacidade.get(v).get(u) + fluxoCaminho);
                    v = u;
                }
                fluxoMaximo += fluxoCaminho;
            }
            return fluxoMaximo;
        }

        private boolean bfsFluxo(Vertice origem, Vertice destino,
                                  Map<String, Map<String, Double>> capacidade,
                                  Map<String, String> pai) {
            for (Vertice v : vertices) v.visitado = false;
            Queue<Vertice> fila = new LinkedList<>();
            fila.add(origem);
            origem.visitado = true;
            pai.put(origem.id, null);

            while (!fila.isEmpty()) {
                Vertice u = fila.poll();
                for (Vertice v : vertices) {
                    if (!v.visitado && capacidade.get(u.id).get(v.id) > 0) {
                        fila.add(v);
                        v.visitado = true;
                        pai.put(v.id, u.id);
                        if (v.id.equals(destino.id)) return true;
                    }
                }
            }
            return false;
        }

        // ============================================================
        // ALGORITMO: Kruskal - Arvore Geradora Minima - RAPHAEL
        // ============================================================
        public List<Aresta> arvoreGeradoraMinima() {
            List<Aresta> resultado = new ArrayList<>();
            List<Aresta> ordenadas = new ArrayList<>(arestas);
            ordenadas.sort(Comparator.comparingDouble(a -> a.peso));

            UnionFind uf = new UnionFind(vertices.size());
            Map<String, Integer> indice = new HashMap<>();
            for (int i = 0; i < vertices.size(); i++) {
                indice.put(vertices.get(i).id, i);
            }

            for (Aresta a : ordenadas) {
                int i = indice.get(a.origem.id);
                int j = indice.get(a.destino.id);
                if (uf.find(i) != uf.find(j)) {
                    uf.union(i, j);
                    resultado.add(a);
                }
            }
            return resultado;
        }

        // ============================================================
        // ALGORITMO: Tarjan - Identificacao de Pontes - ISAIAS
        // ============================================================
        public List<String> encontrarPontes() {
            List<String> pontes = new ArrayList<>();
            Map<String, Integer> disc = new HashMap<>();
            Map<String, Integer> low = new HashMap<>();
            Map<String, String> parent = new HashMap<>();
            int[] tempo = {0};

            for (Vertice v : vertices) {
                disc.put(v.id, -1);
                low.put(v.id, -1);
                parent.put(v.id, null);
            }

            for (Vertice v : vertices) {
                if (disc.get(v.id) == -1) {
                    dfsPontes(v, disc, low, parent, tempo, pontes);
                }
            }
            return pontes;
        }

        private void dfsPontes(Vertice u, Map<String, Integer> disc, Map<String, Integer> low,
                               Map<String, String> parent, int[] tempo, List<String> pontes) {
            disc.put(u.id, tempo[0]);
            low.put(u.id, tempo[0]);
            tempo[0]++;

            for (Aresta a : u.adjacencias) {
                Vertice v = a.destino;
                if (disc.get(v.id) == -1) {
                    parent.put(v.id, u.id);
                    dfsPontes(v, disc, low, parent, tempo, pontes);
                    low.put(u.id, Math.min(low.get(u.id), low.get(v.id)));
                    if (low.get(v.id) > disc.get(u.id)) {
                        pontes.add(u.id + " -- " + v.id + " [" + a.peso + "km]");
                    }
                } else if (!v.id.equals(parent.get(u.id))) {
                    low.put(u.id, Math.min(low.get(u.id), disc.get(v.id)));
                }
            }
        }

        // ============================================================
        // JANINE - Funcionalidade 1: Simular queda de subestacao
        // ============================================================
        public void simularQuedaSubestacao(String idSubestacao) {
            System.out.println("\n========================================");
            System.out.println("JANINE: SIMULAR QUEDA DE SUBESTACAO");
            System.out.println("Subestacao: " + idSubestacao);
            System.out.println("========================================");

            Vertice sub = buscarVertice(idSubestacao);
            if (sub == null || !sub.tipo.equals("SUBESTACAO")) {
                System.out.println("Subestacao invalida.");
                return;
            }

            List<Aresta> removidas = new ArrayList<>();
            for (Aresta a : sub.adjacencias) removidas.add(a);
            for (Aresta a : removidas) {
                a.destino.adjacencias.removeIf(ar -> ar.destino == sub);
            }
            sub.adjacencias.clear();

            Set<String> energizados = new HashSet<>();
            for (Vertice v : vertices) {
                if (v.tipo.equals("SUBESTACAO") && v != sub) {
                    energizados.addAll(verificarAreasAfetadas(v));
                }
            }

            List<Vertice> afetados = new ArrayList<>();
            for (Vertice v : vertices) {
                if (v.tipo.equals("BAIRRO") && !energizados.contains(v.id)) afetados.add(v);
            }

            System.out.println("Bairros AFETADOS (sem energia): " + afetados.size());
            if (afetados.isEmpty()) System.out.println("  Nenhum! Rede com redundancia.");
            else for (Vertice b : afetados) System.out.println("  - " + b.id);

            // Restaurar
            for (Aresta a : removidas) {
                Vertice viz = a.destino;
                Aresta rev = new Aresta(viz, sub, a.peso, a.capacidade, a.tipo);
                sub.adjacencias.add(a);
                viz.adjacencias.add(rev);
            }
        }

        // ============================================================
        // JANINE - Funcionalidade 2: Sugerir pontos prioritarios manutencao
        // ============================================================
        public void sugerirPontosPrioritariosManutencao() {
            System.out.println("\n========================================");
            System.out.println("JANINE: PONTOS PRIORITARIOS MANUTENCAO");
            System.out.println("========================================");

            List<String> pontes = encontrarPontes();
            System.out.println("\n1. PONTES (conexoes criticas):");
            if (pontes.isEmpty()) System.out.println("   Nenhuma. Rede bem redundante.");
            else for (String p : pontes) System.out.println("   -> " + p);

            List<String> verticesCorte = encontrarVerticesDeCorte();
            System.out.println("\n2. VERTICES DE CORTE:");
            if (verticesCorte.isEmpty()) System.out.println("   Nenhum.");
            else for (String v : verticesCorte) System.out.println("   -> " + v);

            System.out.println("\n3. TOP 5 POR CONECTIVIDADE:");
            List<Vertice> ranking = new ArrayList<>(vertices);
            ranking.sort((a, b) -> Integer.compare(b.adjacencias.size(), a.adjacencias.size()));
            for (int i = 0; i < Math.min(5, ranking.size()); i++) {
                Vertice v = ranking.get(i);
                System.out.println("   " + (i+1) + ". " + v + " - " + v.adjacencias.size() + " conexoes");
            }

            System.out.println("\n4. TOP 5 CONEXOES MAIS LONGAS (maior risco):");
            List<Aresta> rankingA = new ArrayList<>(arestas);
            rankingA.sort((a, b) -> Double.compare(b.peso, a.peso));
            for (int i = 0; i < Math.min(5, rankingA.size()); i++) {
                System.out.println("   " + (i+1) + ". " + rankingA.get(i));
            }
        }

        private List<String> encontrarVerticesDeCorte() {
            List<String> corte = new ArrayList<>();
            Map<String, Integer> disc = new HashMap<>();
            Map<String, Integer> low = new HashMap<>();
            Map<String, String> parent = new HashMap<>();
            Map<String, Boolean> ap = new HashMap<>();
            int[] tempo = {0};

            for (Vertice v : vertices) {
                disc.put(v.id, -1); low.put(v.id, -1);
                parent.put(v.id, null); ap.put(v.id, false);
            }
            for (Vertice v : vertices) {
                if (disc.get(v.id) == -1) dfsVerticesCorte(v, disc, low, parent, ap, tempo);
            }
            for (Vertice v : vertices) {
                if (ap.get(v.id)) corte.add(v.id + " (" + v.tipo + ")");
            }
            return corte;
        }

        private void dfsVerticesCorte(Vertice u, Map<String, Integer> disc, Map<String, Integer> low,
                                       Map<String, String> parent, Map<String, Boolean> ap, int[] tempo) {
            int filhos = 0;
            disc.put(u.id, tempo[0]); low.put(u.id, tempo[0]); tempo[0]++;
            for (Aresta a : u.adjacencias) {
                Vertice v = a.destino;
                if (disc.get(v.id) == -1) {
                    filhos++;
                    parent.put(v.id, u.id);
                    dfsVerticesCorte(v, disc, low, parent, ap, tempo);
                    low.put(u.id, Math.min(low.get(u.id), low.get(v.id)));
                    if (parent.get(u.id) == null && filhos > 1) ap.put(u.id, true);
                    if (parent.get(u.id) != null && low.get(v.id) >= disc.get(u.id)) ap.put(u.id, true);
                } else if (!v.id.equals(parent.get(u.id))) {
                    low.put(u.id, Math.min(low.get(u.id), disc.get(v.id)));
                }
            }
        }

        // ============================================================
        // LARISSA - Funcionalidade 3: Identificar bairros afetados
        // ============================================================
        public void identificarBairrosAfetados(String idFalha) {
            System.out.println("\n========================================");
            System.out.println("LARISSA: IDENTIFICAR BAIRROS AFETADOS");
            System.out.println("Falha no vertice: " + idFalha);
            System.out.println("========================================");

            Vertice falha = buscarVertice(idFalha);
            if (falha == null) {
                System.out.println("Vertice nao encontrado.");
                return;
            }

            List<Aresta> removidas = new ArrayList<>();
            for (Aresta a : falha.adjacencias) removidas.add(a);
            for (Aresta a : removidas) {
                a.destino.adjacencias.removeIf(ar -> ar.destino == falha);
            }
            falha.adjacencias.clear();

            Set<String> energizados = new HashSet<>();
            for (Vertice v : vertices) {
                if (v.tipo.equals("SUBESTACAO")) energizados.addAll(verificarAreasAfetadas(v));
            }

            List<Vertice> afetados = new ArrayList<>();
            for (Vertice v : vertices) {
                if (v.tipo.equals("BAIRRO") && !energizados.contains(v.id)) afetados.add(v);
            }

            System.out.println("Bairros AFETADOS: " + afetados.size());
            if (afetados.isEmpty()) System.out.println("  Nenhum! Rede com redundancia.");
            else for (Vertice b : afetados) System.out.println("  - " + b.id);

            // Restaurar
            for (Aresta a : removidas) {
                Vertice viz = a.destino;
                Aresta rev = new Aresta(viz, falha, a.peso, a.capacidade, a.tipo);
                falha.adjacencias.add(a);
                viz.adjacencias.add(rev);
            }
        }

        // ============================================================
        // LARISSA - Funcionalidade 4: Encontrar caminhos alternativos
        // ============================================================
        public void encontrarCaminhosAlternativos(String idBairro) {
            System.out.println("\n========================================");
            System.out.println("LARISSA: CAMINHOS ALTERNATIVOS");
            System.out.println("Bairro destino: " + idBairro);
            System.out.println("========================================");

            Vertice destino = buscarVertice(idBairro);
            if (destino == null || !destino.tipo.equals("BAIRRO")) {
                System.out.println("Bairro invalido.");
                return;
            }

            boolean achou = false;
            for (Vertice se : vertices) {
                if (!se.tipo.equals("SUBESTACAO")) continue;
                List<String> caminho = dijkstra(se, destino);
                if (caminho != null) {
                    achou = true;
                    System.out.print("  " + se.id + " -> ");
                    for (int i = 0; i < caminho.size(); i++) {
                        System.out.print(caminho.get(i));
                        if (i < caminho.size() - 1) System.out.print(" -> ");
                    }
                    System.out.println();
                }
            }
            if (!achou) System.out.println("  Nenhum caminho alternativo encontrado.");
        }

        // Dijkstra para menor caminho (distancia)
        private List<String> dijkstra(Vertice origem, Vertice destino) {
            Map<String, Double> dist = new HashMap<>();
            Map<String, String> pred = new HashMap<>();
            Set<String> visitados = new HashSet<>();

            for (Vertice v : vertices) dist.put(v.id, Double.MAX_VALUE);
            dist.put(origem.id, 0.0);

            while (visitados.size() < vertices.size()) {
                Vertice u = null;
                double min = Double.MAX_VALUE;
                for (Vertice v : vertices) {
                    if (!visitados.contains(v.id) && dist.get(v.id) < min) {
                        min = dist.get(v.id);
                        u = v;
                    }
                }
                if (u == null) break;
                visitados.add(u.id);
                if (u.id.equals(destino.id)) break;

                for (Aresta a : u.adjacencias) {
                    Vertice v = a.destino;
                    if (!visitados.contains(v.id)) {
                        double nova = dist.get(u.id) + a.peso;
                        if (nova < dist.get(v.id)) {
                            dist.put(v.id, nova);
                            pred.put(v.id, u.id);
                        }
                    }
                }
            }

            if (dist.get(destino.id) == Double.MAX_VALUE) return null;
            List<String> caminho = new ArrayList<>();
            String atual = destino.id;
            while (atual != null) {
                caminho.add(0, atual);
                atual = pred.get(atual);
            }
            return caminho;
        }
    }

    // ============================================================
    // UNION-FIND (Disjoint Set) para Kruskal
    // ============================================================
    static class UnionFind {
        int[] pai, rank;
        UnionFind(int n) {
            pai = new int[n]; rank = new int[n];
            for (int i = 0; i < n; i++) pai[i] = i;
        }
        int find(int x) {
            if (pai[x] != x) pai[x] = find(pai[x]);
            return pai[x];
        }
        void union(int x, int y) {
            int rx = find(x), ry = find(y);
            if (rx == ry) return;
            if (rank[rx] < rank[ry]) pai[rx] = ry;
            else if (rank[rx] > rank[ry]) pai[ry] = rx;
            else { pai[ry] = rx; rank[rx]++; }
        }
    }

    // ============================================================
    // MAIN - Demonstracao completa de TODO o trabalho
    // ============================================================
    public static void main(String[] args) {
        Grafo rede = new Grafo();

        // --- VERTICES ---
        rede.adicionarVertice("SE-Norte", "SUBESTACAO");
        rede.adicionarVertice("SE-Sul", "SUBESTACAO");
        rede.adicionarVertice("SE-Leste", "SUBESTACAO");
        rede.adicionarVertice("T1", "TRANSFORMADOR");
        rede.adicionarVertice("T2", "TRANSFORMADOR");
        rede.adicionarVertice("T3", "TRANSFORMADOR");
        rede.adicionarVertice("T4", "TRANSFORMADOR");
        rede.adicionarVertice("T5", "TRANSFORMADOR");
        rede.adicionarVertice("Bairro-Centro", "BAIRRO");
        rede.adicionarVertice("Bairro-Leste", "BAIRRO");
        rede.adicionarVertice("Bairro-Oeste", "BAIRRO");
        rede.adicionarVertice("Bairro-Norte", "BAIRRO");
        rede.adicionarVertice("Bairro-Sul", "BAIRRO");
        rede.adicionarVertice("Bairro-Novo", "BAIRRO");
        rede.adicionarVertice("Bairro-Velho", "BAIRRO");

        // --- ARESTAS (idOrigem, idDestino, peso_km, capacidade_MW, tipo) ---
        // SE-Norte
        rede.adicionarAresta("SE-Norte", "T1", 2.5, 100, "LINHA");
        rede.adicionarAresta("SE-Norte", "T2", 3.0, 100, "LINHA");
        rede.adicionarAresta("SE-Norte", "Bairro-Centro", 1.5, 50, "CONEXAO");
        rede.adicionarAresta("SE-Norte", "Bairro-Norte", 2.0, 50, "CONEXAO");

        // SE-Sul
        rede.adicionarAresta("SE-Sul", "T3", 2.0, 100, "LINHA");
        rede.adicionarAresta("SE-Sul", "T4", 2.8, 100, "LINHA");
        rede.adicionarAresta("SE-Sul", "Bairro-Sul", 1.0, 50, "CONEXAO");
        rede.adicionarAresta("SE-Sul", "Bairro-Velho", 1.8, 50, "CONEXAO");

        // SE-Leste
        rede.adicionarAresta("SE-Leste", "T5", 1.5, 100, "LINHA");
        rede.adicionarAresta("SE-Leste", "Bairro-Leste", 1.2, 50, "CONEXAO");

        // Transformadores -> Bairros
        rede.adicionarAresta("T1", "Bairro-Leste", 1.2, 50, "CONEXAO");
        rede.adicionarAresta("T2", "Bairro-Oeste", 1.8, 50, "CONEXAO");
        rede.adicionarAresta("T2", "Bairro-Novo", 2.1, 50, "CONEXAO");
        rede.adicionarAresta("T3", "Bairro-Novo", 1.5, 50, "CONEXAO");
        rede.adicionarAresta("T4", "Bairro-Velho", 1.3, 50, "CONEXAO");
        rede.adicionarAresta("T5", "Bairro-Oeste", 2.0, 50, "CONEXAO");
        rede.adicionarAresta("T5", "Bairro-Norte", 2.5, 50, "CONEXAO");

        // Malha entre transformadores
        rede.adicionarAresta("T1", "T2", 1.5, 80, "LINHA");
        rede.adicionarAresta("T3", "T4", 1.1, 80, "LINHA");
        rede.adicionarAresta("T2", "T3", 4.5, 60, "LINHA"); // ponte critica
        rede.adicionarAresta("T4", "T5", 3.0, 70, "LINHA");

        // ============================================================
        // DEMONSTRACAO DE TODOS OS ALGORITMOS E FUNCIONALIDADES
        // ============================================================

        System.out.println("============================================================");
        System.out.println("  PROJETO: REDE DE ENERGIA ELETRICA - GRAFOS");
        System.out.println("============================================================");

        // --- ALGORITMO: DFS/BFS (Equipe) ---
        System.out.println("\n--- ALGORITMO: DFS/BFS - Verificar areas afetadas ---");
        Set<String> alcance = rede.verificarAreasAfetadas(rede.buscarVertice("SE-Norte"));
        System.out.println("Alcance a partir de SE-Norte: " + alcance);

        // --- ALGORITMO: Fluxo Maximo (Marcos) ---
        System.out.println("\n--- ALGORITMO: FLUXO MAXIMO (Edmonds-Karp) - Marcos ---");
        double fluxo1 = rede.fluxoMaximo("SE-Norte", "Bairro-Sul");
        System.out.println("Fluxo maximo SE-Norte -> Bairro-Sul: " + fluxo1 + " MW");
        double fluxo2 = rede.fluxoMaximo("SE-Sul", "Bairro-Norte");
        System.out.println("Fluxo maximo SE-Sul -> Bairro-Norte: " + fluxo2 + " MW");
        double fluxo3 = rede.fluxoMaximo("SE-Leste", "Bairro-Velho");
        System.out.println("Fluxo maximo SE-Leste -> Bairro-Velho: " + fluxo3 + " MW");

        // --- ALGORITMO: Arvore Geradora Minima (Raphael) ---
        System.out.println("\n--- ALGORITMO: ARVORE GERADORA MINIMA (Kruskal) - Raphael ---");
        List<Aresta> agm = rede.arvoreGeradoraMinima();
        double custoTotal = 0;
        System.out.println("Arestas da AGM (expansao de menor custo):");
        for (Aresta a : agm) {
            System.out.println("  " + a.origem.id + " - " + a.destino.id + " [" + a.peso + "km]");
            custoTotal += a.peso;
        }
        System.out.println("Custo total da expansao: " + custoTotal + " km");

        // --- ALGORITMO: Identificacao de Pontes (Isaias) ---
        System.out.println("\n--- ALGORITMO: IDENTIFICACAO DE PONTES (Tarjan) - Isaias ---");
        List<String> pontes = rede.encontrarPontes();
        System.out.println("Conexoes criticas (pontes) encontradas: " + pontes.size());
        for (String p : pontes) System.out.println("  -> " + p);

        // --- FUNCIONALIDADES: Janine ---
        System.out.println("\n--- FUNCIONALIDADES: JANINE ---");
        rede.simularQuedaSubestacao("SE-Norte");
        rede.simularQuedaSubestacao("SE-Sul");
        rede.sugerirPontosPrioritariosManutencao();

        // --- FUNCIONALIDADES: Larissa ---
        System.out.println("\n--- FUNCIONALIDADES: LARISSA ---");
        rede.identificarBairrosAfetados("T2");
        rede.identificarBairrosAfetados("SE-Norte");
        rede.encontrarCaminhosAlternativos("Bairro-Novo");
        rede.encontrarCaminhosAlternativos("Bairro-Sul");
        rede.encontrarCaminhosAlternativos("Bairro-Oeste");

        System.out.println("\n============================================================");
        System.out.println("  FIM DAS DEMONSTRACOES");
        System.out.println("============================================================");
    }
}
