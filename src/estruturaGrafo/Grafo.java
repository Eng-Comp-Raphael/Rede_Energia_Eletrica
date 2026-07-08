package estruturaGrafo;
import algoritmos.DFS;
import algoritmos.BFS;
import algoritmos.AreasAfetadas;
import algoritmos.FluxoMaximo;
import algoritmos.Pontes;
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

    public Grafo() {
        this.vertice = new HashMap<>();
        this.aresta = new ArrayList<>();
    }

    // add verteces ao grafo (arvores binirias vazias)
    public void addVertice(TIPO vertice) {
        if (!this.vertice.containsKey(vertice)) {
            Vertice<TIPO> novoVertece = new Vertice<>(vertice);
            this.vertice.put(vertice, novoVertece);
        }
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

    // add arestas ao grafo
    public void addAresta(TIPO idOrigem, TIPO idDestino, int lambda) {
        Vertice<TIPO> u = this.vertice.get(idOrigem);
        Vertice<TIPO> v = this.vertice.get(idDestino);

        if (u != null && v != null) {
            Aresta<TIPO> novAresta = new Aresta<>(u, v, lambda);
            this.aresta.add(novAresta);
        } else {
            System.out.println("Erro: Um ou ambos os vértices não existem");
        }
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
    //  ÁRVORE GERADORA MÍNIMA (delega para AlgoritmoAGM)
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
    //  POSTE (VÉRTICE) E CASA (NODO DA ÁRVORE): CONTROLE DE FALHAS
    // ============================================================
    // Regra de negócio: Vertice extends Nodo, logo o próprio poste já
    // possui o campo "ativo" herdado. Se o poste está inativo, TODAS as
    // casas penduradas nele são consideradas sem atendimento, independente
    // do "ativo" individual de cada casa. Se o poste está ativo, cada casa
    // liga/desliga de forma independente (falha local).

    // ============================================================
    //  REMOÇÃO DE VÉRTICES E ARESTAS
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
        // O método removeIf percorre a lista e apaga a aresta se a condição for verdadeira
        this.aresta.removeIf(a -> 
            a.getU().getNome().equals(idVertice) || 
            a.getV().getNome().equals(idVertice)
        );

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
    //  BUSCA EM PROFUNDIDADE (delega para AlgoritmoDFS)
    // ============================================================

    public List<TIPO> dfs(TIPO inicio) {
        return new DFS<>(this.vertice, this.aresta).executar(inicio);
    }

    public void printDFS(TIPO inicio) {
        System.out.println("=== DFS a partir de [" + inicio + "] ===");
        System.out.println(dfs(inicio));
    }

    // ============================================================
    //  BUSCA EM LARGURA (delega para AlgoritmoBFS)
    // ============================================================

    public List<TIPO> bfs(TIPO inicio) {
        return new BFS<>(this.vertice, this.aresta).executar(inicio);
    }

    public void printBFS(TIPO inicio) {
        System.out.println("=== BFS a partir de [" + inicio + "] ===");
        System.out.println(bfs(inicio));
    }

    // ============================================================
    //  ÁREAS AFETADAS (delega para AlgoritmoAreasAfetadas)
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
    //  FLUXO MÁXIMO (delega para AlgoritmoFluxoMaximo)
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
    //  IDENTIFICAÇÃO DE PONTES (delega para AlgoritmoPontes)
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
    // ============================================================
    //  MÉTODOS DE ACESSO PARA A INTERFACE GRÁFICA
    // ============================================================
    public List<Aresta<TIPO>> getArestas() {
        return this.aresta;
    }

    public Map<TIPO, Vertice<TIPO>> getVerticesMap() {
        return this.vertice;
    }
}