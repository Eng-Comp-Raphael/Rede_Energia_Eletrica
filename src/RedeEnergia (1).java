import java.util.*;

/**
 * PROJETO: Rede de Energia Eletrica
 * Modelagem com Grafos - Estrutura da Equipe
 * 
 * Funcionalidades implementadas neste arquivo (JANINE + LARISSA):
 *   JANINE:
 *     1. Simular queda de uma subestacao
 *     2. Sugerir pontos prioritarios para manutencao
 *   LARISSA:
 *     3. Identificar bairros afetados por uma falha
 *     4. Encontrar caminhos alternativos de distribuicao
 * 
 * Algoritmos utilizados:
 *   - DFS/BFS para verificar areas afetadas e caminhos alternativos
 *   - Dijkstra para menor caminho alternativo (recomendado)
 *   - Tarjan (DFS) para identificacao de pontes (conexoes criticas)
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
        public void simularQuedaSubestacao(String idSubestacao) {
            System.out.println("\n========================================");
            System.out.println("SIMULACAO: QUEDA DA SUBESTACAO " + idSubestacao);
            System.out.println("========================================");

            Vertice subestacao = buscarVertice(idSubestacao);
            if (subestacao == null || !subestacao.tipo.equals("SUBESTACAO")) {
                System.out.println("Subestacao " + idSubestacao + " nao encontrada ou nao e subestacao.");
                return;
            }

            List<Aresta> arestasRemovidas = new ArrayList<>();
            for (Aresta a : subestacao.adjacencias) {
                arestasRemovidas.add(a);
            }
            for (Aresta a : arestasRemovidas) {
                Vertice vizinho = a.destino;
                vizinho.adjacencias.removeIf(ar -> ar.destino == subestacao);
            }
            subestacao.adjacencias.clear();

            Set<String> energizados = new HashSet<>();
            for (Vertice v : vertices) {
                if (v.tipo.equals("SUBESTACAO") && v != subestacao) {
                    bfsEnergizados(v, energizados);
                }
            }

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
            List<Vertice> ranking = new ArrayList<>(vertices);
            ranking.sort((a, b) -> Integer.compare(b.adjacencias.size(), a.adjacencias.size()));
            for (int i = 0; i < Math.min(5, ranking.size()); i++) {
                Vertice v = ranking.get(i);
                System.out.println("   " + (i+1) + ". " + v + " - " + v.adjacencias.size() + " conexoes");
            }

            System.out.println("\n4. CONEXOES MAIS LONGAS (maior distancia = maior risco):");
            List<Aresta> rankingArestas = new ArrayList<>(arestas);
            rankingArestas.sort((a, b) -> Double.compare(b.peso, a.peso));
            for (int i = 0; i < Math.min(5, rankingArestas.size()); i++) {
                Aresta a = rankingArestas.get(i);
                System.out.println("   " + (i+1) + ". " + a);
            }
        }

        // Algoritmo de Tarjan para encontrar pontes
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

        // Algoritmo para encontrar Vertices de Corte
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

        // ============================================================
        // LARISSA - FUNCIONALIDADE 3: Identificar bairros afetados
        // ============================================================
        // Dado uma falha (queda de subestacao OU transformador OU
        // uma linha especifica), identifica todos os bairros que ficam
        // sem energia.
        // ============================================================
        public void identificarBairrosAfetados(String idFalha, String tipoFalha) {
            System.out.println("\n========================================");
            System.out.println("LARISSA: IDENTIFICAR BAIRROS AFETADOS");
            System.out.println("Falha: " + tipoFalha + " " + idFalha);
            System.out.println("========================================");

            Vertice verticeFalha = buscarVertice(idFalha);
            if (verticeFalha == null) {
                System.out.println("Erro: " + idFalha + " nao encontrado na rede.");
                return;
            }

            List<Aresta> arestasRemovidas = new ArrayList<>();
            
            if (tipoFalha.equalsIgnoreCase("VERTICE")) {
                // Remove todas as arestas conectadas ao vertice
                for (Aresta a : verticeFalha.adjacencias) {
                    arestasRemovidas.add(a);
                }
                for (Aresta a : arestasRemovidas) {
                    Vertice vizinho = a.destino;
                    vizinho.adjacencias.removeIf(ar -> ar.destino == verticeFalha);
                }
                verticeFalha.adjacencias.clear();
            } else if (tipoFalha.equalsIgnoreCase("ARESTA")) {
                // Aqui implementamos remocao de uma aresta especifica
                // Para simplificar, removemos uma aresta do vertice para o primeiro vizinho
                System.out.println("Aresta removida: " + verticeFalha.adjacencias.get(0));
                Aresta a = verticeFalha.adjacencias.get(0);
                Vertice vizinho = a.destino;
                vizinho.adjacencias.removeIf(ar -> ar.destino == verticeFalha);
                verticeFalha.adjacencias.remove(a);
                arestasRemovidas.add(a);
            }

            // BFS a partir de todas as subestacoes para ver quem tem energia
            Set<String> energizados = new HashSet<>();
            for (Vertice v : vertices) {
                if (v.tipo.equals("SUBESTACAO")) {
                    bfsEnergizados(v, energizados);
                }
            }

            // Listar bairros afetados
            List<Vertice> bairrosAfetados = new ArrayList<>();
            for (Vertice v : vertices) {
                if (v.tipo.equals("BAIRRO") && !energizados.contains(v.id)) {
                    bairrosAfetados.add(v);
                }
            }

            System.out.println("BAIRROS AFETADOS: " + bairrosAfetados.size());
            if (bairrosAfetados.isEmpty()) {
                System.out.println("  Nenhum bairro afetado! A rede possui redundancia.");
            } else {
                for (Vertice b : bairrosAfetados) {
                    System.out.println("  - " + b.id);
                }
            }

            // Restaurar
            if (tipoFalha.equalsIgnoreCase("VERTICE")) {
                restaurarArestas(verticeFalha, arestasRemovidas);
            } else if (tipoFalha.equalsIgnoreCase("ARESTA") && !arestasRemovidas.isEmpty()) {
                Aresta a = arestasRemovidas.get(0);
                Vertice vizinho = a.destino;
                Aresta reversa = new Aresta(vizinho, verticeFalha, a.peso, a.tipo);
                verticeFalha.adjacencias.add(a);
                vizinho.adjacencias.add(reversa);
            }
        }

        // ============================================================
        // LARISSA - FUNCIONALIDADE 4: Encontrar caminhos alternativos
        // ============================================================
        // Dado um bairro afetado, encontra o melhor caminho alternativo
        // para reconecta-lo a uma subestacao. Usa Dijkstra para encontrar
        // o menor caminho (menor distancia) de uma subestacao ate o bairro.
        // ============================================================
        public void encontrarCaminhosAlternativos(String idBairro) {
            System.out.println("\n========================================");
            System.out.println("LARISSA: CAMINHOS ALTERNATIVOS");
            System.out.println("Bairro: " + idBairro);
            System.out.println("========================================");

            Vertice bairro = buscarVertice(idBairro);
            if (bairro == null || !bairro.tipo.equals("BAIRRO")) {
                System.out.println("Erro: " + idBairro + " nao e um bairro valido.");
                return;
            }

            // Para cada subestacao, encontra o menor caminho ate o bairro
            boolean encontrou = false;
            for (Vertice subestacao : vertices) {
                if (!subestacao.tipo.equals("SUBESTACAO")) continue;

                List<String> caminho = dijkstra(subestacao, bairro);
                if (caminho != null && !caminho.isEmpty()) {
                    encontrou = true;
                    System.out.println("\nCaminho alternativo de " + subestacao.id + ":");
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
        private List<String> dijkstra(Vertice origem, Vertice destino) {
            Map<String, Double> distancia = new HashMap<>();
            Map<String, String> predecessor = new HashMap<>();
            Set<String> visitados = new HashSet<>();

            for (Vertice v : vertices) {
                distancia.put(v.id, Double.MAX_VALUE);
                predecessor.put(v.id, null);
            }
            distancia.put(origem.id, 0.0);

            while (visitados.size() < vertices.size()) {
                Vertice u = null;
                double minDist = Double.MAX_VALUE;
                for (Vertice v : vertices) {
                    if (!visitados.contains(v.id) && distancia.get(v.id) < minDist) {
                        minDist = distancia.get(v.id);
                        u = v;
                    }
                }

                if (u == null) break;
                visitados.add(u.id);

                if (u.id.equals(destino.id)) break;

                for (Aresta a : u.adjacencias) {
                    Vertice v = a.destino;
                    if (!visitados.contains(v.id)) {
                        double novaDist = distancia.get(u.id) + a.peso;
                        if (novaDist < distancia.get(v.id)) {
                            distancia.put(v.id, novaDist);
                            predecessor.put(v.id, u.id);
                        }
                    }
                }
            }

            // Reconstruir caminho
            if (distancia.get(destino.id) == Double.MAX_VALUE) {
                return null; // Sem caminho
            }

            List<String> caminho = new ArrayList<>();
            String atual = destino.id;
            while (atual != null) {
                caminho.add(0, atual);
                atual = predecessor.get(atual);
            }
            return caminho;
        }

        // ============================================================
        // LARISSA - BONUS: Encontrar caminho alternativo para todos
        // bairros afetados apos uma falha
        // ============================================================
        public void analiseCompletaFalha(String idFalha) {
            System.out.println("\n========================================");
            System.out.println("LARISSA: ANALISE COMPLETA DE FALHA");
            System.out.println("Falha na subestacao: " + idFalha);
            System.out.println("========================================");

            Vertice subestacao = buscarVertice(idFalha);
            if (subestacao == null || !subestacao.tipo.equals("SUBESTACAO")) {
                System.out.println("Subestacao invalida.");
                return;
            }

            // Remove a subestacao
            List<Aresta> arestasRemovidas = new ArrayList<>();
            for (Aresta a : subestacao.adjacencias) arestasRemovidas.add(a);
            for (Aresta a : arestasRemovidas) {
                Vertice vizinho = a.destino;
                vizinho.adjacencias.removeIf(ar -> ar.destino == subestacao);
            }
            subestacao.adjacencias.clear();

            // BFS para encontrar energizados
            Set<String> energizados = new HashSet<>();
            for (Vertice v : vertices) {
                if (v.tipo.equals("SUBESTACAO") && v != subestacao) {
                    bfsEnergizados(v, energizados);
                }
            }

            // Para cada bairro afetado, tenta encontrar caminho alternativo
            List<Vertice> bairrosAfetados = new ArrayList<>();
            for (Vertice v : vertices) {
                if (v.tipo.equals("BAIRRO") && !energizados.contains(v.id)) {
                    bairrosAfetados.add(v);
                }
            }

            System.out.println("Total de bairros afetados: " + bairrosAfetados.size());

            for (Vertice bairro : bairrosAfetados) {
                System.out.println("\n  Bairro afetado: " + bairro.id);
                
                // Tentar caminho alternativo para cada subestacao restante
                boolean encontrou = false;
                for (Vertice se : vertices) {
                    if (!se.tipo.equals("SUBESTACAO") || se == subestacao) continue;
                    
                    List<String> caminho = dijkstra(se, bairro);
                    if (caminho != null && !caminho.isEmpty()) {
                        encontrou = true;
                        System.out.print("    Caminho alternativo de " + se.id + ": ");
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
            restaurarArestas(subestacao, arestasRemovidas);
        }
    }

    // ============================================================
    // METODO MAIN - Demonstracao completa
    // ============================================================
    public static void main(String[] args) {
        Grafo rede = new Grafo();

        // --- VETICES ---
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
        rede.adicionarAresta("SE-Norte", "T1", 2.5, "LINHA");
        rede.adicionarAresta("SE-Norte", "T2", 3.0, "LINHA");
        rede.adicionarAresta("SE-Norte", "Bairro-Centro", 1.5, "CONEXAO");
        rede.adicionarAresta("SE-Sul", "T3", 2.0, "LINHA");
        rede.adicionarAresta("SE-Sul", "T4", 2.8, "LINHA");
        rede.adicionarAresta("SE-Sul", "Bairro-Sul", 1.0, "CONEXAO");
        rede.adicionarAresta("T1", "Bairro-Leste", 1.2, "CONEXAO");
        rede.adicionarAresta("T2", "Bairro-Oeste", 1.8, "CONEXAO");
        rede.adicionarAresta("T2", "Bairro-Novo", 2.1, "CONEXAO");
        rede.adicionarAresta("T3", "Bairro-Novo", 1.5, "CONEXAO");
        rede.adicionarAresta("T4", "Bairro-Velho", 1.3, "CONEXAO");
        rede.adicionarAresta("T1", "T2", 1.5, "LINHA");
        rede.adicionarAresta("T3", "T4", 1.1, "LINHA");
        rede.adicionarAresta("T2", "T3", 4.5, "LINHA");

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
