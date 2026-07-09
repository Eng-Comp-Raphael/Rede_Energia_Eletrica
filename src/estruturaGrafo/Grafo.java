package estruturaGrafo;

import algoritmos.DFS;
import algoritmos.BFS;
import algoritmos.CaminhoAlternativo;
import algoritmos.AreasAfetadas;
import algoritmos.FluxoMaximo;
import algoritmos.Pontes;
import algoritmos.PontosPrioritarios;
import algoritmos.AGM;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * @file Grafo.java
 * @author Raphael Batista
 * @date 2026
 * @brief Criação e manipulação de arestas, vertices e arvores dos vertices.
 *        Os algoritmos (DFS, BFS, Áreas Afetadas, Fluxo Máximo, Pontes e AGM)
 *        vivem em classes próprias no pacote "algoritmos" e são apenas
 *        chamados a partir daqui.
 */

public class Grafo<TIPO extends Comparable<TIPO>> {// verteces == arvore == poste, filhos das Arvores == casas, arestas
                                                   // == conexoes eletricas

    private Map<TIPO, Vertice<TIPO>> vertice;
    private ArrayList<Aresta<TIPO>> aresta;
    private TIPO fontePrincipal = null; // primeiro vertice criado = subestacao/fonte da rede
    private TIPO ultimoVerticeAdicionado = null; // usado para encadear a auto-conexao

    public Grafo() {
        this.vertice = new HashMap<>();
        this.aresta = new ArrayList<>();
    }

    // add verteces ao grafo (arvores binirias vazias)
    // add verteces ao grafo (arvores binirias vazias)
    // A PARTIR DO SEGUNDO VERTICE, cada novo poste ja nasce conectado ao
    // ultimo poste criado (peso padrao = 1), garantindo que a rede nunca
    // fique desconexa. O PRIMEIRO vertice criado vira a fonte principal
    // (subestacao raiz), usada como ponto de partida no recalculo de rotas.
    public void addVertice(TIPO vertice) {
        addVertice(vertice, 1);
    }

    // mesma coisa, mas permitindo escolher o peso da conexao automatica
    // (ex: distancia real entre os dois postes no mapa)
    public void addVertice(TIPO vertice, int pesoConexaoAnterior) {
        if (!this.vertice.containsKey(vertice)) {
            Vertice<TIPO> novoVertece = new Vertice<>(vertice);
            this.vertice.put(vertice, novoVertece);

            if (fontePrincipal == null) {
                fontePrincipal = vertice; // primeiro vertice = fonte da rede
            } else {
                addAresta(ultimoVerticeAdicionado, vertice, pesoConexaoAnterior);
            }
            ultimoVerticeAdicionado = vertice;
        }
    }

    // Cria o vertice e, se "conectar" for true E ja existir um vertice anterior,
    // liga os dois automaticamente. O rastreio do "ultimo vertice criado" acontece
    // SEMPRE (mesmo com conectar=false), para que ligar o botao de volta depois
    // continue a cadeia a partir do vertice certo, e nao de um vertice antigo.
    public void addVerticeAutoConectado(TIPO vertice, int pesoConexaoAnterior, boolean conectar) {
        if (this.vertice.containsKey(vertice)) {
            return;
        }
        addVertice(vertice);

        if (fontePrincipal == null) {
            fontePrincipal = vertice;
        } else if (conectar) {
            addAresta(ultimoVerticeAdicionado, vertice, pesoConexaoAnterior);
        }
        ultimoVerticeAdicionado = vertice;
    }

    public void addVerticeAutoConectado(TIPO vertice, int pesoConexaoAnterior) {
        addVerticeAutoConectado(vertice, pesoConexaoAnterior, true);
    }

    public void addVerticeAutoConectado(TIPO vertice) {
        addVerticeAutoConectado(vertice, 1, true);
    }

    public TIPO getUltimoVerticeAdicionado() {
        return ultimoVerticeAdicionado;
    }

    public void setUltimoVerticeAdicionado(TIPO id) {
        this.ultimoVerticeAdicionado = id;
        if (this.fontePrincipal == null) {
            this.fontePrincipal = id; // cobre o caso raro de clicar num vertice
                                      // carregado de arquivo, que nunca passou
                                      // pelo addVerticeAutoConectado
        }
    }

    public TIPO getFontePrincipal() {
        return fontePrincipal;
    }

    // add dados na arvore presente no vertece
    public void addElementoVertice(TIPO idVertice, TIPO dadoArvore) {
        Vertice<TIPO> vertice = this.vertice.get(idVertice);
        if (vertice != null) {
            vertice.add(dadoArvore);
        } else {
            System.out.println("Erro: Vertice [" + idVertice + "] não existe no grafo");
        }
    }

    public void addVerticeExixtente(TIPO id, Vertice<TIPO> verticeExistente) {
        this.vertice.put(id, verticeExistente);
    }

    /**
 * Remove todos os vértices, arestas e redefine os estados internos do grafo
 * para permitir um reset completo do simulador.
 */
public void limparGrafo() {
    // 1. Limpa a estrutura que armazena os vértices (seja um Map ou uma List)
    if (this.vertice != null) {
        this.vertice.clear();
    }
    
    // 2. Limpa a lista ou conjunto que armazena as arestas da rede
    if (this.aresta != null) {
        this.aresta.clear();
    }
    
    // 3. Reseta a referência do último vértice (essencial para a lógica do botão Auto-Conectar)
    this.ultimoVerticeAdicionado = null;
    
    // 4. Caso a tua estrutura controle o estado ativo/inativo dos postes 
    // em um mapa separado (e não dentro do próprio objeto Vértice), limpa-o também:
    // if (this.postesAtivos != null) {
    //     this.postesAtivos.clear();
    // }
}

    // add arestas ao grafo
    public void addAresta(TIPO idOrigem, TIPO idDestino, int lambda) {
        Vertice<TIPO> u = this.vertice.get(idOrigem);
        Vertice<TIPO> v = this.vertice.get(idDestino);

        if (u == null || v == null) {
            System.out.println("Erro: Um ou ambos os vértices não existem");
            return;
        }

        // NOVO: impede registrar a mesma conexao fisica duas vezes -- um cabo
        // duplicado nunca e' considerado "ponte" pelo algoritmo, mesmo quando
        // na pratica e' so uma unica linha, o que mascara pontes de verdade
        // em toda a cadeia de ancestrais na DFS
        for (Aresta<TIPO> existente : this.aresta) {
            boolean mesmoPar = (existente.getU().getNome().equals(idOrigem)
                    && existente.getV().getNome().equals(idDestino)) ||
                    (existente.getU().getNome().equals(idDestino) && existente.getV().getNome().equals(idOrigem));
            if (mesmoPar) {
                System.out.println(
                        "Aviso: já existe conexão entre [" + idOrigem + "] e [" + idDestino + "], duplicata ignorada.");
                return;
            }
        }

        this.aresta.add(new Aresta<>(u, v, lambda));
    }

    public void printArestasDuplicadas() {
        System.out.println("=== Verificando arestas duplicadas ===");
        boolean achouAlguma = false;
        for (int i = 0; i < aresta.size(); i++) {
            for (int j = i + 1; j < aresta.size(); j++) {
                Aresta<TIPO> a = aresta.get(i);
                Aresta<TIPO> b = aresta.get(j);
                boolean mesmoPar = (a.getU().getNome().equals(b.getU().getNome())
                        && a.getV().getNome().equals(b.getV().getNome())) ||
                        (a.getU().getNome().equals(b.getV().getNome())
                                && a.getV().getNome().equals(b.getU().getNome()));
                if (mesmoPar) {
                    achouAlguma = true;
                    System.out.println("DUPLICATA: [" + a.getU().getNome() + "] - [" + a.getV().getNome() + "]");
                }
            }
        }
        if (!achouAlguma)
            System.out.println("Nenhuma duplicata encontrada.");
    }

    // imprime verteces e as arvores
    public void printVertices() {
        System.out.println("=== VÉRTICES (Árvore Binária)) ===");
        for (TIPO id : vertice.keySet()) {
            System.out.print("Vértice [" + id + "] -> Arvore em ordem: ");
            Vertice<TIPO> arvore = vertice.get(id);
            arvore.printInsert(arvore);
            System.out.println();
        }
    }

    // imprimir arestas
    public void printArestas() {
        System.out.println("=== Arestas ===");
        for (int i = 0; i < aresta.size(); i++) {
            Aresta<TIPO> edge = aresta.get(i);
            TIPO raizU = edge.getU() != null ? edge.getU().getNome() : null;
            TIPO raizV = edge.getV() != null ? edge.getV().getNome() : null;
            System.out.println((i + 1) + "[" + raizU + "] - [" + raizV + "], " + edge.getLambda());
        }
    }

    // imprimir grafo
    public void printGrafo() {
        printVertices();
        printArestas();
    }

    // ============================================================
    // ÁRVORE GERADORA MÍNIMA (delega para AlgoritmoAGM)
    // ============================================================

    public Grafo<TIPO> AGM(Grafo<TIPO> grafoOriginal) {
        Grafo<TIPO> agm = new Grafo<>();
        for (TIPO id : grafoOriginal.vertice.keySet()) {
            agm.addVerticeExixtente(id, grafoOriginal.vertice.get(id));
        }

        AGM<TIPO> algoritmo = new AGM<>();
        List<Aresta<TIPO>> selecionadas = algoritmo.executar(grafoOriginal.vertice, grafoOriginal.aresta);

        int pesoTotal = 0;
        for (Aresta<TIPO> a : selecionadas) {
            agm.addAresta(a.getU().getNome(), a.getV().getNome(), a.getLambda());
            pesoTotal += a.getLambda();
        }

        int totalVertices = grafoOriginal.vertice.size();
        if (selecionadas.size() < totalVertices - 1) {
            System.out.println("Aviso: O grafo nao e conexo, a AGM gerada e uma floresta geradora minima");
        }
        System.out.println("Peso total da AGM: " + pesoTotal);

        return agm;
    }

    // ============================================================
    // POSTE (VÉRTICE) E CASA (NODO DA ÁRVORE): CONTROLE DE FALHAS
    // ============================================================
    // Regra de negócio: Vertice extends Nodo, logo o próprio poste já
    // possui o campo "ativo" herdado. Se o poste está inativo, TODAS as
    // casas penduradas nele são consideradas sem atendimento, independente
    // do "ativo" individual de cada casa. Se o poste está ativo, cada casa
    // liga/desliga de forma independente (falha local).

    // ============================================================
    // REMOÇÃO DE VÉRTICES E ARESTAS
    // ============================================================

    public void removerVertice(TIPO idVertice) {
        // Verifica se o vértice realmente existe no grafo
        if (!this.vertice.containsKey(idVertice)) {
            System.out.println("Erro: Vértice [" + idVertice + "] não existe no grafo.");
            return;
        }

        // 1. Remove o vértice do mapa principal
        this.vertice.remove(idVertice);

        // 2. Remove todas as arestas (cabos) que começam ou terminam neste vértice
        this.aresta.removeIf(a -> a.getU().getNome().equals(idVertice) ||
                a.getV().getNome().equals(idVertice));

        // 3. NOVO: evita que a auto-conexao (addVerticeAutoConectado) aponte para
        // um vertice que acabou de ser removido
        if (idVertice.equals(fontePrincipal)) {
            fontePrincipal = null;
        }
        if (idVertice.equals(ultimoVerticeAdicionado)) {
            ultimoVerticeAdicionado = null;
        }

        System.out.println("Vértice [" + idVertice + "] e suas conexões foram removidos do backend.");
    }

    /** Marca o poste (vértice) como ativo/inativo (ex: subestação caiu). */
    public void setPosteAtivo(TIPO idPoste, boolean ativo) {
        Vertice<TIPO> poste = this.vertice.get(idPoste);
        if (poste == null) {
            System.out.println("Erro: Poste [" + idPoste + "] não existe no grafo");
            return;
        }
        poste.setAtivo(ativo);
    }

    /** Consulta se o poste (vértice) está ativo. */
    public boolean posteAtivo(TIPO idPoste) {
        Vertice<TIPO> poste = this.vertice.get(idPoste);
        if (poste == null) {
            System.out.println("Erro: Poste [" + idPoste + "] não existe no grafo");
            return false;
        }
        return poste.getAtivo();
    }

    // ============================================================
    // CURTO-CIRCUITO: recalculo automatico de rotas
    // ============================================================

    /**
     * Marca o poste como CURTO-CIRCUITO (inativo) e recalcula a rota da energia.
     */
    public void marcarCurtoCircuito(TIPO idPoste) {
        Vertice<TIPO> poste = this.vertice.get(idPoste);
        if (poste == null) {
            System.out.println("Erro: Poste [" + idPoste + "] não existe no grafo");
            return;
        }
        poste.setAtivo(false);
        System.out.println("\n>>> CURTO-CIRCUITO detectado no poste [" + idPoste + "] <<<");
        recalcularRotas();
    }

    /** Repara o poste (volta a ficar ativo) e recalcula a rota da energia. */
    public void repararCurtoCircuito(TIPO idPoste) {
        Vertice<TIPO> poste = this.vertice.get(idPoste);
        if (poste == null) {
            System.out.println("Erro: Poste [" + idPoste + "] não existe no grafo");
            return;
        }
        poste.setAtivo(true);
        System.out.println("\n>>> Poste [" + idPoste + "] reparado, recalculando rotas <<<");
        recalcularRotas();
    }

    /**
     * Recalcula, a partir da fonte principal (primeiro vertice criado), quais
     * postes ficam sem atendimento.
     */
    public void recalcularRotas() {
        if (fontePrincipal == null) {
            System.out.println("Nenhuma fonte principal definida ainda (nenhum vertice foi criado).");
            return;
        }
        printAreasAfetadas(fontePrincipal);
    }

    /** Busca um Nodo (casa) dentro da árvore de um poste, por comparação BST. */
    private Nodo<TIPO> buscarNodo(Nodo<TIPO> atual, TIPO alvo) {
        if (atual == null) {
            return null;
        }
        int cmp = alvo.compareTo(atual.getNome());
        if (cmp == 0) {
            return atual;
        }
        return cmp < 0 ? buscarNodo(atual.getEsquerda(), alvo) : buscarNodo(atual.getDireita(), alvo);
    }

    /** Marca uma casa (elemento da árvore do poste) como ativa/inativa. */
    public void setCasaAtiva(TIPO idPoste, TIPO idCasa, boolean ativo) {
        Vertice<TIPO> poste = this.vertice.get(idPoste);
        if (poste == null) {
            System.out.println("Erro: Poste [" + idPoste + "] não existe no grafo");
            return;
        }
        Nodo<TIPO> casa = buscarNodo(poste, idCasa);
        if (casa == null || casa == poste) {
            System.out.println("Erro: Casa [" + idCasa + "] não encontrada na árvore do poste [" + idPoste + "]");
            return;
        }
        casa.setAtivo(ativo);
    }

    /** Consulta o "ativo" bruto de uma casa (sem considerar o poste). */
    public boolean casaAtiva(TIPO idPoste, TIPO idCasa) {
        Vertice<TIPO> poste = this.vertice.get(idPoste);
        if (poste == null) {
            System.out.println("Erro: Poste [" + idPoste + "] não existe no grafo");
            return false;
        }
        Nodo<TIPO> casa = buscarNodo(poste, idCasa);
        if (casa == null || casa == poste) {
            System.out.println("Erro: Casa [" + idCasa + "] não encontrada na árvore do poste [" + idPoste + "]");
            return false;
        }
        return casa.getAtivo();
    }

    /**
     * Verdadeiro apenas se o poste está ativo E a casa está ativa
     * (é a checagem "está sendo atendida de fato" que combina os dois níveis).
     */
    public boolean estaAtendida(TIPO idPoste, TIPO idCasa) {
        Vertice<TIPO> poste = this.vertice.get(idPoste);
        if (poste == null) {
            System.out.println("Erro: Poste [" + idPoste + "] não existe no grafo");
            return false;
        }
        if (!poste.getAtivo()) {
            return false; // poste caiu -> ninguem pendurado nele e atendido
        }
        Nodo<TIPO> casa = buscarNodo(poste, idCasa);
        if (casa == null || casa == poste) {
            System.out.println("Erro: Casa [" + idCasa + "] não encontrada na árvore do poste [" + idPoste + "]");
            return false;
        }
        return casa.getAtivo();
    }

    /** Lista todas as casas de um poste que estão sem atendimento no momento. */
    public List<TIPO> listarCasasSemAtendimento(TIPO idPoste) {
        List<TIPO> semAtendimento = new ArrayList<>();
        Vertice<TIPO> poste = this.vertice.get(idPoste);
        if (poste == null) {
            System.out.println("Erro: Poste [" + idPoste + "] não existe no grafo");
            return semAtendimento;
        }
        boolean posteCaiu = !poste.getAtivo();
        coletarCasasSemAtendimento(poste.getEsquerda(), posteCaiu, semAtendimento);
        coletarCasasSemAtendimento(poste.getDireita(), posteCaiu, semAtendimento);
        return semAtendimento;
    }

    private void coletarCasasSemAtendimento(Nodo<TIPO> atual, boolean posteCaiu, List<TIPO> semAtendimento) {
        if (atual == null) {
            return;
        }
        if (posteCaiu || !atual.getAtivo()) {
            semAtendimento.add(atual.getNome());
        }
        coletarCasasSemAtendimento(atual.getEsquerda(), posteCaiu, semAtendimento);
        coletarCasasSemAtendimento(atual.getDireita(), posteCaiu, semAtendimento);
    }

    public void printAtendimento(TIPO idPoste) {
        System.out.println("=== Atendimento das casas do poste [" + idPoste + "] ===");
        if (!posteAtivo(idPoste)) {
            System.out.println("Poste [" + idPoste + "] está INATIVO -> todas as casas estão sem atendimento.");
        }
        List<TIPO> semAtendimento = listarCasasSemAtendimento(idPoste);
        if (semAtendimento.isEmpty()) {
            System.out.println("Todas as casas deste poste estão sendo atendidas.");
        } else {
            System.out.println("Casas sem atendimento: " + semAtendimento);
        }
    }

    // ============================================================
    // BUSCA EM PROFUNDIDADE (delega para AlgoritmoDFS)
    // ============================================================

    public List<TIPO> dfs(TIPO inicio) {
        return new DFS<>(this.vertice, this.aresta).executar(inicio);
    }

    public void printDFS(TIPO inicio) {
        System.out.println("=== DFS a partir de [" + inicio + "] ===");
        System.out.println(dfs(inicio));
    }

    // ============================================================
    // BUSCA EM LARGURA (delega para AlgoritmoBFS)
    // ============================================================

    public List<TIPO> bfs(TIPO inicio) {
        return new BFS<>(this.vertice, this.aresta).executar(inicio);
    }

    public void printBFS(TIPO inicio) {
        System.out.println("=== BFS a partir de [" + inicio + "] ===");
        System.out.println(bfs(inicio));
    }

    // ============================================================
    // ÁREAS AFETADAS (delega para AlgoritmoAreasAfetadas)
    // ============================================================

    public List<TIPO> identificarAreasAfetadas(TIPO fonte) {
        return new AreasAfetadas<>(this.vertice, this.aresta).executar(fonte);
    }

    public void printAreasAfetadas(TIPO fonte) {
        System.out.println("=== Áreas afetadas a partir da fonte [" + fonte + "] ===");
        List<TIPO> afetados = identificarAreasAfetadas(fonte);
        if (afetados.isEmpty()) {
            System.out.println("Nenhuma área ficou sem atendimento.");
        } else {
            System.out.println("Postes sem atendimento: " + afetados);
        }
    }

    // ============================================================
    // FLUXO MÁXIMO (delega para AlgoritmoFluxoMaximo)
    // ============================================================

    public int fluxoMaximo(TIPO origem, TIPO destino) {
        return new FluxoMaximo<>(this.vertice, this.aresta).executar(origem, destino);
    }

    public void printFluxoMaximo(TIPO origem, TIPO destino) {
        int fluxo = fluxoMaximo(origem, destino);
        System.out.println("=== Fluxo máximo de [" + origem + "] até [" + destino + "] ===");
        System.out.println("Capacidade máxima de distribuição: " + fluxo);
    }

    // ============================================================
    // IDENTIFICAÇÃO DE PONTES (delega para AlgoritmoPontes)
    // ============================================================

    public List<Aresta<TIPO>> encontrarPontes() {
        return new Pontes<>(this.vertice, this.aresta).executar();
    }

    public void printPontes() {
        System.out.println("=== Conexões críticas (pontes) da rede ===");
        List<Aresta<TIPO>> pontes = encontrarPontes();
        if (pontes.isEmpty()) {
            System.out.println("Nenhuma conexão crítica encontrada (a rede tem rotas alternativas).");
        } else {
            for (Aresta<TIPO> p : pontes) {
                System.out.println("[" + p.getU().getNome() + "] - [" + p.getV().getNome() + "]");
            }
        }
    }

    /**
     * Simula rompimento de um cabo/tubulação especifico (sem derrubar os postes das
     * pontas).
     */
    public void romperAresta(TIPO idOrigem, TIPO idDestino) {
        for (Aresta<TIPO> a : this.aresta) {
            boolean mesmoPar = (a.getU().getNome().equals(idOrigem) && a.getV().getNome().equals(idDestino)) ||
                    (a.getU().getNome().equals(idDestino) && a.getV().getNome().equals(idOrigem));
            if (mesmoPar) {
                a.setAtivo(false);
                System.out.println("Cabo [" + idOrigem + "] - [" + idDestino + "] rompido.");
                return;
            }
        }
        System.out.println("Erro: conexão entre [" + idOrigem + "] e [" + idDestino + "] não encontrada.");
    }

    public void repararAresta(TIPO idOrigem, TIPO idDestino) {
        for (Aresta<TIPO> a : this.aresta) {
            boolean mesmoPar = (a.getU().getNome().equals(idOrigem) && a.getV().getNome().equals(idDestino)) ||
                    (a.getU().getNome().equals(idDestino) && a.getV().getNome().equals(idOrigem));
            if (mesmoPar) {
                a.setAtivo(true);
                System.out.println("Cabo [" + idOrigem + "] - [" + idDestino + "] reparado.");
                return;
            }
        }
    }

    public void simularQuedaSubestacao(TIPO idSubestacao) {
    System.out.println("\n>>> SIMULANDO QUEDA DA SUBESTACAO [" + idSubestacao + "] <<<");
    setPosteAtivo(idSubestacao, false);
    printAreasAfetadas(idSubestacao);
}

public void simularRompimentoTubulacao(TIPO idOrigem, TIPO idDestino) {
    System.out.println("\n>>> SIMULANDO ROMPIMENTO ENTRE [" + idOrigem + "] E [" + idDestino + "] <<<");
    romperAresta(idOrigem, idDestino);
    printAreasAfetadas(idOrigem); // usa idOrigem como fonte de referencia
}

public List<TIPO> caminhoAlternativo(TIPO origem, TIPO destino) {
    return new CaminhoAlternativo<>(this.vertice, this.aresta).executar(origem, destino);
}

public void printCaminhoAlternativo(TIPO origem, TIPO destino) {
    List<TIPO> caminho = caminhoAlternativo(origem, destino);
    System.out.println("=== Caminho alternativo de [" + origem + "] até [" + destino + "] ===");
    System.out.println(caminho.isEmpty() ? "Nenhum caminho disponível." : caminho);
}

public void printPontosPrioritarios() {
    PontosPrioritarios<TIPO> analise = new PontosPrioritarios<>(this.vertice, this.aresta);
    System.out.println("\n=== PONTOS PRIORITÁRIOS PARA MANUTENÇÃO ===");
    System.out.println("1. Pontes (conexões críticas):");
    List<Aresta<TIPO>> pontes = analise.pontesCriticas();
    if (pontes.isEmpty()) System.out.println("   Nenhuma.");
    else for (Aresta<TIPO> p : pontes) System.out.println("   -> [" + p.getU().getNome() + "] - [" + p.getV().getNome() + "]");

    System.out.println("2. Postes mais frágeis (menor conectividade):");
    for (TIPO id : analise.postesMaisFragilizados(5)) System.out.println("   -> " + id);

    System.out.println("3. Cabos mais longos (maior risco/custo):");
    for (Aresta<TIPO> a : analise.cabosMaisLongos(5))
        System.out.println("   -> [" + a.getU().getNome() + "] - [" + a.getV().getNome() + "] (" + a.getLambda() + "m)");
}



    // ============================================================
    // MÉTODOS DE ACESSO PARA A INTERFACE GRÁFICA
    // ============================================================
    public List<Aresta<TIPO>> getArestas() {
        return this.aresta;
    }

    public Map<TIPO, Vertice<TIPO>> getVerticesMap() {
        return this.vertice;
    }
}