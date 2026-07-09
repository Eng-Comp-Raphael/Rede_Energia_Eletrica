package algoritmos;

import estruturaGrafo.Vertice;
import estruturaGrafo.Aresta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * @file AlgoritmoBFS.java
 * @brief Busca em Largura (BFS) sobre a rede de distribuição.
 *
 * Considera somente postes ativos: um poste com "ativo == false" (e suas
 * arestas) é automaticamente excluído da busca.
 */
public class BFS<TIPO extends Comparable<TIPO>> {

    private final Map<TIPO, Vertice<TIPO>> vertice;
    private final List<Aresta<TIPO>> aresta;

    public BFS(Map<TIPO, Vertice<TIPO>> vertice, List<Aresta<TIPO>> aresta) {
        this.vertice = vertice;
        this.aresta = aresta;
    }

    /** Executa o BFS a partir de "inicio" e retorna a ordem de visita. */
    public List<TIPO> executar(TIPO inicio) {
        List<TIPO> ordem = new ArrayList<>();

        if (!vertice.containsKey(inicio)) {
            System.out.println("Erro: Vertice [" + inicio + "] não existe no grafo");
            return ordem;
        }
        if (!vertice.get(inicio).getAtivo()) {
            System.out.println("Aviso: Poste [" + inicio + "] está inativo, nenhuma área é alcançada a partir dele.");
            return ordem;
        }

        Map<TIPO, List<TIPO>> adj = construirListaAdjacencia();
        Set<TIPO> visitados = new HashSet<>();
        Queue<TIPO> fila = new LinkedList<>();

        visitados.add(inicio);
        fila.add(inicio);

        while (!fila.isEmpty()) {
            TIPO atual = fila.poll();
            ordem.add(atual);
            for (TIPO vizinho : adj.getOrDefault(atual, Collections.emptyList())) {
                if (!visitados.contains(vizinho)) {
                    visitados.add(vizinho);
                    fila.add(vizinho);
                }
            }
        }
        return ordem;
    }

    /** Lista de adjacência não-direcionada considerando somente postes ativos. */
    private Map<TIPO, List<TIPO>> construirListaAdjacencia() {
        Map<TIPO, List<TIPO>> adj = new HashMap<>();
        for (TIPO id : vertice.keySet()) {
            if (vertice.get(id).getAtivo()) {
                adj.put(id, new ArrayList<>());
            }
        }
        for (Aresta<TIPO> a : aresta) {
            TIPO u = a.getU().getNome();
            TIPO v = a.getV().getNome();
            if (a.getAtivo() && adj.containsKey(u) && adj.containsKey(v)) {
                adj.get(u).add(v);
                adj.get(v).add(u);
            }
        }
        return adj;
    }
}