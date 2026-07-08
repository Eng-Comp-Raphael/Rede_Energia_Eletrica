package algoritmos;

import estruturaGrafo.Vertice;
import estruturaGrafo.Aresta;
import estruturaGrafo.ConjuntoDisjunto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @file AlgoritmoAGM.java
 * @brief Árvore Geradora Mínima (Kruskal), usando um Conjunto Disjunto para
 *        detectar ciclos e ordenação das arestas por peso (lambda) crescente.
 */
public class AGM<TIPO extends Comparable<TIPO>> {

    /**
     * @param vertice todos os vértices (postes) do grafo original
     * @param arestas todas as arestas do grafo original
     * @return lista de arestas selecionadas para compor a AGM (ou floresta
     *         geradora mínima, caso o grafo não seja conexo)
     */
    public List<Aresta<TIPO>> executar(Map<TIPO, Vertice<TIPO>> vertice, List<Aresta<TIPO>> arestas) {
        // Ordena as arestas por peso (lambda) crescente
        ArrayList<Aresta<TIPO>> arestasOrdenadas = new ArrayList<>(arestas);
        Collections.sort(arestasOrdenadas, Comparator.comparingInt(Aresta::getLambda));

        // Inicializa o conjunto disjunto, um conjunto por vertice
        ConjuntoDisjunto<TIPO> conjunto = new ConjuntoDisjunto<>();
        for (TIPO id : vertice.keySet()) {
            conjunto.criarConjunto(id);
        }

        int totalVertices = vertice.size();
        List<Aresta<TIPO>> selecionadas = new ArrayList<>();

        // Percorre as arestas em ordem crescente de peso
        for (Aresta<TIPO> aresta : arestasOrdenadas) {
            if (selecionadas.size() == totalVertices - 1) {
                break; // arvore geradora ja completa
            }
            TIPO nomeU = aresta.getU().getNome();
            TIPO nomeV = aresta.getV().getNome();

            // se unir() retornar true, os vertices estavam em conjuntos diferentes
            // -> nao forma ciclo -> pode entrar na AGM
            if (conjunto.unir(nomeU, nomeV)) {
                selecionadas.add(aresta);
            }
        }

        return selecionadas;
    }
}