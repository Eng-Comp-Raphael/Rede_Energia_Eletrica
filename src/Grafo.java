import estruturaGrafo.Vertice;
import estruturaGrafo.Aresta;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 * @file Grafo.java
 * @author Raphael Batista 
 * @date 2026
 * @brief Criação e manipulação de arestas, vertices e arvores dos vertices
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
            Vertice<TIPO> novoVertece = new Vertice<TIPO>(vertice);
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
            // System.out.println("Aresta " + (i + 1) + ": Liga árvore de raiz [" + raizU +
            // "] à árvore de raiz [" + raizV
            // + "] com peso (lambda) = " + edge.getLambda());
            System.out.println((i + 1) + "[" + raizU + "] -> [" + raizV + "], " + edge.getLambda());
        }
    }

    // imprimir grafo
    public void printGrafo() {
        printVertices();
        printArestas();
    }
}
