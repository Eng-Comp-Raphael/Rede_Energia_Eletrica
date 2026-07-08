package algoritmos;

import estruturaGrafo.Vertice;
import estruturaGrafo.Aresta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @file AlgoritmoPontes.java
 * @brief Identificação de pontes (conexões críticas) usando o algoritmo
 *        clássico de Tarjan com tempos de descoberta (disc) e menor
 *        alcance (low). Considera somente postes atualmente ativos.
 */
public class Pontes<TIPO extends Comparable<TIPO>> {

    private final Map<TIPO, Vertice<TIPO>> vertice;
    private final List<Aresta<TIPO>> aresta;

    public Pontes(Map<TIPO, Vertice<TIPO>> vertice, List<Aresta<TIPO>> aresta) {
        this.vertice = vertice;
        this.aresta = aresta;
    }

    /** Retorna a lista de arestas que são pontes na rede ativa atual. */
    public List<Aresta<TIPO>> executar() {
        Map<TIPO, List<TIPO>> adj = construirListaAdjacencia();
        Map<TIPO, Integer> disc = new HashMap<>();
        Map<TIPO, Integer> low = new HashMap<>();
        Map<TIPO, TIPO> pai = new HashMap<>();
        Set<TIPO> visitados = new HashSet<>();
        List<Aresta<TIPO>> pontes = new ArrayList<>();
        int[] tempo = {0};

        for (TIPO id : vertice.keySet()) {
            if (vertice.get(id).getAtivo() && !visitados.contains(id)) {
                dfsPontes(id, adj, disc, low, pai, visitados, tempo, pontes);
            }
        }
        return pontes;
    }

    private void dfsPontes(TIPO u, Map<TIPO, List<TIPO>> adj, Map<TIPO, Integer> disc, Map<TIPO, Integer> low,
            Map<TIPO, TIPO> pai, Set<TIPO> visitados, int[] tempo, List<Aresta<TIPO>> pontes) {

        visitados.add(u);
        disc.put(u, tempo[0]);
        low.put(u, tempo[0]);
        tempo[0]++;

        for (TIPO v : adj.getOrDefault(u, Collections.emptyList())) {
            if (!visitados.contains(v)) {
                pai.put(v, u);
                dfsPontes(v, adj, disc, low, pai, visitados, tempo, pontes);

                low.put(u, Math.min(low.get(u), low.get(v)));

                // condição de ponte: nenhuma aresta de volta liga a subarvore
                // de v a u ou a algum ancestral de u
                if (low.get(v) > disc.get(u)) {
                    pontes.add(new Aresta<>(vertice.get(u), vertice.get(v), 0));
                }
            } else if (!v.equals(pai.get(u))) {
                // aresta de retorno (back edge), ignora a aresta para o pai direto
                low.put(u, Math.min(low.get(u), disc.get(v)));
            }
        }
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
            if (adj.containsKey(u) && adj.containsKey(v)) {
                adj.get(u).add(v);
                adj.get(v).add(u);
            }
        }
        return adj;
    }
}