import java.util.*;

/**
 * PROJETO: Rede de Energia Eletrica
 * Modelagem com Grafos - Estrutura da Equipe
 * 
 * Funcionalidades implementadas neste arquivo (JANINE):
 *   1. Simular queda de uma subestacao
 *   2. Sugerir pontos prioritarios para manutencao
 * 
 * Algoritmos utilizados:
 *   - DFS/BFS para verificar areas afetadas
 *   - Tarjan (DFS) para identificacao de pontes (conexoes criticas)
 *   - Ordenacao por prioridade combinada (grau, peso, pontes)
 */
public class RedeEnergia {

    // ============================================================
    // CLASSE VERTICE (Subestacao, Bairro, Transformador)
    // ============================================================
    static class Vertice {
        String id;
        String tipo; // SUBESTACAO, BAIRRO, TRANSFORMADOR
        List<Aresta> adjacencias;
        boolean visitado; // uso temporario em algoritmos

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
    // CLASSE ARESTA (Linha eletrica ou conexao de distribuicao)
    // ============================================================
    static class Aresta {
        Vertice origem;
        Vertice destino;
        double peso; // Distancia (km)
        String tipo; // LINHA ou CONEXAO

        public Aresta(Vertice origem, Vertice destino, double peso, String tipo) {
            this.origem = origem;
            this.destino = destino;
            this.peso = peso;
            this.tipo = tipo;
        }

        @Override
        public String toString() {
            return origem.id + " -> " + destino.id + " [" + peso + "km]";
        }
    }

    // ============================================================
    // CLASSE GRAFO (Rede Eletrica)
    // ============================================================
    static class Grafo {
        List<Vertice> vertices;
        List<Aresta> arestas;

        public Grafo() {
            this.vertices = new ArrayList<>();
            this.arestas = new ArrayList<>();
        }

        // Adiciona vertice (Subestacao, Bairro, Transformador)
        public Vertice adicionarVertice(String id, String tipo) {
            Vertice v = new Vertice(id, tipo);
            vertices.add(v);
            return v;
        }

        // Adiciona aresta bidirecional (rede de distribuicao)
        public void adicionarAresta(String idOrigem, String idDestino, double peso, String tipo) {
            Vertice origem = buscarVertice(idOrigem);
            Vertice destino = buscarVertice(idDestino);
            if (origem == null || destino == null) {
                System.out.println("Erro: vertice nao encontrado para aresta " + idOrigem + " - " + idDestino);
                return;
            }
            Aresta a1 = new Aresta(origem, destino, peso, tipo);
            Aresta a2 = new Aresta(destino, origem, peso, tipo);
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
        // JANINE - FUNCIONALIDADE 1: Simular queda de uma subestacao
        // ============================================================
        // Remove a subestacao do grafo e identifica quais bairros ficam
        // sem energia (desconectados das subestacoes restantes).
        // ============================================================
        public void simularQuedaSubestacao(String idSubestacao) {
            System.out.println("\n========================================");
            System.out.println("SIMULACAO: QUEDA DA SUBESTACAO " + idSubestacao);
            System.out.println("========================================");

            Vertice subestacao = buscarVertice(idSubestacao);
            if (subestacao == null || !subestacao.tipo.equals("SUBESTACAO")) {
                System.out.println("Subestacao " + idSubestacao + " nao encontrada ou nao e subestacao.");
                return;
            }

            // Remover arestas conectadas a subestacao (simular desligamento)
            List<Aresta> arestasRemovidas = new ArrayList<>();
            for (Aresta a : subestacao.adjacencias) {
                arestasRemovidas.add(a);
            }
            // Remover da lista de adjacencia dos vizinhos
            for (Aresta a : arestasRemovidas) {
                Vertice vizinho = a.destino;
                vizinho.adjacencias.removeIf(ar -> ar.destino == subestacao);
            }
            subestacao.adjacencias.clear(); // desconecta a subestacao

            // Identificar bairros com e sem energia
            Set<String> energizados = new HashSet<>();
            
            // Faz BFS/DFS a partir de todas as subestacoes restantes
            for (Vertice v : vertices) {
                if (v.tipo.equals("SUBESTACAO") && v != subestacao) {
                    bfsEnergizados(v, energizados);
                }
            }

            // Listar bairros afetados (sem energia)
            List<Vertice> bairrosAfetados = new ArrayList<>();
            List<Vertice> bairrosComEnergia = new ArrayList<>();
            for (Vertice v : vertices) {
                if (v.tipo.equals("BAIRRO")) {
                    if (energizados.contains(v.id)) {
                        bairrosComEnergia.add(v);
                    } else {
                        bairrosAfetados.add(v);
                    }
                }
            }

            System.out.println("Subestacao " + idSubestacao + " foi REMOVIDA da rede.");
            System.out.println("\nBAIRROS COM ENERGIA (conectados a outras subestacoes): " + bairrosComEnergia.size());
            for (Vertice b : bairrosComEnergia) {
                System.out.println("  - " + b.id);
            }

            System.out.println("\nBAIRROS AFETADOS (SEM ENERGIA): " + bairrosAfetados.size());
            if (bairrosAfetados.isEmpty()) {
                System.out.println("  Nenhum! A rede possui redundancia.");
            } else {
                for (Vertice b : bairrosAfetados) {
                    System.out.println("  - " + b.id);
                }
            }

            // Restaurar conexoes para manter o grafo original
            restaurarArestas(subestacao, arestasRemovidas);
        }

        private void bfsEnergizados(Vertice inicio, Set<String> energizados) {
            Queue<Vertice> fila = new LinkedList<>();
            inicio.visitado = true;
            fila.add(inicio);
            while (!fila.isEmpty()) {
                Vertice atual = fila.poll();
                energizados.add(atual.id);
                for (Aresta a : atual.adjacencias) {
                    Vertice vizinho = a.destino;
                    if (!vizinho.visitado) {
                        vizinho.visitado = true;
                        fila.add(vizinho);
                    }
                }
            }
            // limpar visitados
            for (Vertice v : vertices) v.visitado = false;
        }

        private void restaurarArestas(Vertice subestacao, List<Aresta> arestasRemovidas) {
            for (Aresta a : arestasRemovidas) {
                Vertice vizinho = a.destino;
                Aresta reversa = new Aresta(vizinho, subestacao, a.peso, a.tipo);
                subestacao.adjacencias.add(a);
                vizinho.adjacencias.add(reversa);
            }
        }

        // ============================================================
        // JANINE - FUNCIONALIDADE 2: Sugerir pontos prioritarios
        // para manutencao
        // ============================================================
        // Combina:
        //   a) Pontes (arestas criticas - desconectam a rede se falharem)
        //   b) Vertices de corte (articulation points)
        //   c) Vertices/arestas com maior carga (grau / peso)
        // ============================================================
        public void sugerirPontosPrioritariosManutencao() {
            System.out.println("\n========================================");
            System.out.println("PONTOS PRIORITARIOS PARA MANUTENCAO");
            System.out.println("========================================");

            // 1. Identificar PONTES (conexoes criticas)
            List<String> pontes = encontrarPontes();
            System.out.println("\n1. PONTES (conexoes criticas - falha desconecta a rede):");
            if (pontes.isEmpty()) {
                System.out.println("   Nenhuma ponte encontrada. A rede e bem redundante.");
            } else {
                for (String ponte : pontes) {
                    System.out.println("   -> " + ponte);
                }
            }

            // 2. Identificar VERTICES DE CORTE (subestacoes/bairros criticos)
            List<String> verticesCorte = encontrarVerticesDeCorte();
            System.out.println("\n2. VERTICES DE CORTE (pontos criticos da rede):");
            if (verticesCorte.isEmpty()) {
                System.out.println("   Nenhum vertice de corte encontrado.");
            } else {
                for (String v : verticesCorte) {
                    System.out.println("   -> " + v);
                }
            }

            // 3. Rankear por carga/grau (quanto mais conexoes, mais critico)
            System.out.println("\n3. RANKING DE PRIORIDADE POR CONECTIVIDADE:");
            List<Vertice> ranking = new ArrayList<>(vertices);
            ranking.sort((a, b) -> Integer.compare(b.adjacencias.size(), a.adjacencias.size()));
            for (int i = 0; i < Math.min(5, ranking.size()); i++) {
                Vertice v = ranking.get(i);
                System.out.println("   " + (i+1) + ". " + v + " - " + v.adjacencias.size() + " conexoes");
            }

            // 4. Arestas com maior distancia (maior risco de falha por extensao)
            System.out.println("\n4. CONEXOES MAIS LONGAS (maior distancia = maior risco):");
            List<Aresta> rankingArestas = new ArrayList<>(arestas);
            rankingArestas.sort((a, b) -> Double.compare(b.peso, a.peso));
            for (int i = 0; i < Math.min(5, rankingArestas.size()); i++) {
                Aresta a = rankingArestas.get(i);
                System.out.println("   " + (i+1) + ". " + a);
            }
        }

        // Algoritmo de Tarjan para encontrar pontes (Arestas de corte)
        private List<String> encontrarPontes() {
            List<String> pontes = new ArrayList<>();
            Map<String, Integer> disc = new HashMap<>();
            Map<String, Integer> low = new HashMap<>();
            Map<String, String> parent = new HashMap<>();
            int[] time = {0};

            for (Vertice v : vertices) {
                disc.put(v.id, -1);
                low.put(v.id, -1);
                parent.put(v.id, null);
            }

            for (Vertice v : vertices) {
                if (disc.get(v.id) == -1) {
                    dfsPontes(v, disc, low, parent, time, pontes);
                }
            }
            return pontes;
        }

        private void dfsPontes(Vertice u, Map<String, Integer> disc, Map<String, Integer> low,
                               Map<String, String> parent, int[] time, List<String> pontes) {
            disc.put(u.id, time[0]);
            low.put(u.id, time[0]);
            time[0]++;

            for (Aresta a : u.adjacencias) {
                Vertice v = a.destino;
                if (disc.get(v.id) == -1) {
                    parent.put(v.id, u.id);
                    dfsPontes(v, disc, low, parent, time, pontes);
                    low.put(u.id, Math.min(low.get(u.id), low.get(v.id)));

                    if (low.get(v.id) > disc.get(u.id)) {
                        pontes.add(u.id + " -- " + v.id + " (dist: " + a.peso + "km)");
                    }
                } else if (!v.id.equals(parent.get(u.id))) {
                    low.put(u.id, Math.min(low.get(u.id), disc.get(v.id)));
                }
            }
        }

        // Algoritmo para encontrar Vertices de Corte (Articulation Points)
        private List<String> encontrarVerticesDeCorte() {
            List<String> verticesCorte = new ArrayList<>();
            Map<String, Integer> disc = new HashMap<>();
            Map<String, Integer> low = new HashMap<>();
            Map<String, String> parent = new HashMap<>();
            Map<String, Boolean> ap = new HashMap<>();
            int[] time = {0};

            for (Vertice v : vertices) {
                disc.put(v.id, -1);
                low.put(v.id, -1);
                parent.put(v.id, null);
                ap.put(v.id, false);
            }

            for (Vertice v : vertices) {
                if (disc.get(v.id) == -1) {
                    dfsVerticesCorte(v, disc, low, parent, ap, time);
                }
            }

            for (Vertice v : vertices) {
                if (ap.get(v.id)) {
                    verticesCorte.add(v.id + " (tipo: " + v.tipo + ")");
                }
            }
            return verticesCorte;
        }

        private void dfsVerticesCorte(Vertice u, Map<String, Integer> disc, Map<String, Integer> low,
                                       Map<String, String> parent, Map<String, Boolean> ap, int[] time) {
            int children = 0;
            disc.put(u.id, time[0]);
            low.put(u.id, time[0]);
            time[0]++;

            for (Aresta a : u.adjacencias) {
                Vertice v = a.destino;
                if (disc.get(v.id) == -1) {
                    children++;
                    parent.put(v.id, u.id);
                    dfsVerticesCorte(v, disc, low, parent, ap, time);
                    low.put(u.id, Math.min(low.get(u.id), low.get(v.id)));

                    if (parent.get(u.id) == null && children > 1) {
                        ap.put(u.id, true);
                    }
                    if (parent.get(u.id) != null && low.get(v.id) >= disc.get(u.id)) {
                        ap.put(u.id, true);
                    }
                } else if (!v.id.equals(parent.get(u.id))) {
                    low.put(u.id, Math.min(low.get(u.id), disc.get(v.id)));
                }
            }
        }
    }

    // ============================================================
    // METODO MAIN - Demonstracao das funcionalidades da JANINE
    // ============================================================
    public static void main(String[] args) {
        Grafo rede = new Grafo();

        // --- VETICES (Subestacoes, Transformadores, Bairros) ---
        // Subestacoes (Origem)
        rede.adicionarVertice("SE-Norte", "SUBESTACAO");
        rede.adicionarVertice("SE-Sul", "SUBESTACAO");
        
        // Transformadores (intermediarios)
        rede.adicionarVertice("T1", "TRANSFORMADOR");
        rede.adicionarVertice("T2", "TRANSFORMADOR");
        rede.adicionarVertice("T3", "TRANSFORMADOR");
        rede.adicionarVertice("T4", "TRANSFORMADOR");
        
        // Bairros (Destino)
        rede.adicionarVertice("Bairro-Centro", "BAIRRO");
        rede.adicionarVertice("Bairro-Leste", "BAIRRO");
        rede.adicionarVertice("Bairro-Oeste", "BAIRRO");
        rede.adicionarVertice("Bairro-Novo", "BAIRRO");
        rede.adicionarVertice("Bairro-Velho", "BAIRRO");
        rede.adicionarVertice("Bairro-Sul", "BAIRRO");

        // --- ARESTAS (Linhas eletricas / Conexoes) ---
        // Pesos = distancia em km
        // Conexoes da SE-Norte
        rede.adicionarAresta("SE-Norte", "T1", 2.5, "LINHA");
        rede.adicionarAresta("SE-Norte", "T2", 3.0, "LINHA");
        rede.adicionarAresta("SE-Norte", "Bairro-Centro", 1.5, "CONEXAO");
        
        // Conexoes da SE-Sul
        rede.adicionarAresta("SE-Sul", "T3", 2.0, "LINHA");
        rede.adicionarAresta("SE-Sul", "T4", 2.8, "LINHA");
        rede.adicionarAresta("SE-Sul", "Bairro-Sul", 1.0, "CONEXAO");
        
        // Conexoes entre transformadores e bairros
        rede.adicionarAresta("T1", "Bairro-Leste", 1.2, "CONEXAO");
        rede.adicionarAresta("T2", "Bairro-Oeste", 1.8, "CONEXAO");
        rede.adicionarAresta("T2", "Bairro-Novo", 2.1, "CONEXAO");
        rede.adicionarAresta("T3", "Bairro-Novo", 1.5, "CONEXAO"); // redundancia para Bairro-Novo
        rede.adicionarAresta("T4", "Bairro-Velho", 1.3, "CONEXAO");
        
        // Conexao entre transformadores (malha parcial)
        rede.adicionarAresta("T1", "T2", 1.5, "LINHA");
        rede.adicionarAresta("T3", "T4", 1.1, "LINHA");
        
        // Conexao critica (ponte): T2 -> T3 (unica ligacao entre norte e sul)
        rede.adicionarAresta("T2", "T3", 4.5, "LINHA");

        // ============================================================
        // DEMONSTRACAO DAS FUNCIONALIDADES DA JANINE
        // ============================================================
        
        // 1. Simular queda da subestacao SE-Norte
        rede.simularQuedaSubestacao("SE-Norte");
        
        // 2. Simular queda da subestacao SE-Sul
        rede.simularQuedaSubestacao("SE-Sul");
        
        // 3. Sugerir pontos prioritarios para manutencao
        rede.sugerirPontosPrioritariosManutencao();

        System.out.println("\n========================================");
        System.out.println("FIM DAS SIMULACOES (JANINE)");
        System.out.println("========================================");
    }
}
