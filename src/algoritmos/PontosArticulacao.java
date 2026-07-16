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
 * @file PontosArticulacao.java
 * @brief Identifica os postes que hoje são pontos de articulação da rede: se
 *        um deles cair, a rede ativa se parte em pedaços isolados. Usa o
 *        mesmo algoritmo de Tarjan (disc/low) já usado para Pontes, mas
 *        aplicado a vértices em vez de arestas. Considera somente postes e
 *        cabos atualmente ativos, assim como o algoritmo de Pontes.
 */
public class PontosArticulacao<TIPO extends Comparable<TIPO>> {

    private final Map<TIPO, Vertice<TIPO>> vertice;
    private final List<Aresta<TIPO>> aresta;

    public PontosArticulacao(Map<TIPO, Vertice<TIPO>> vertice, List<Aresta<TIPO>> aresta) {
        this.vertice = vertice;
        this.aresta = aresta;
    }

    /** Retorna os postes ativos que são pontos de articulação na rede ativa atual. */
    public List<TIPO> executar() {
        Map<TIPO, List<TIPO>> adj = construirListaAdjacencia();
        Map<TIPO, Integer> disc = new HashMap<>();
        Map<TIPO, Integer> low = new HashMap<>();
        Map<TIPO, TIPO> pai = new HashMap<>();
        Set<TIPO> visitados = new HashSet<>();
        Set<TIPO> articulacoes = new HashSet<>();
        int[] tempo = {0};

        for (TIPO id : vertice.keySet()) {
            if (vertice.get(id).getAtivo() && !visitados.contains(id)) {
                dfsArticulacao(id, id, adj, disc, low, pai, visitados, tempo, articulacoes);
            }
        }

        List<TIPO> resultado = new ArrayList<>(articulacoes);
        Collections.sort(resultado);
        return resultado;
    }

    private void dfsArticulacao(TIPO u, TIPO raiz, Map<TIPO, List<TIPO>> adj, Map<TIPO, Integer> disc,
            Map<TIPO, Integer> low, Map<TIPO, TIPO> pai, Set<TIPO> visitados, int[] tempo, Set<TIPO> articulacoes) {

        visitados.add(u);
        disc.put(u, tempo[0]);
        low.put(u, tempo[0]);
        tempo[0]++;
        int filhos = 0;

        for (TIPO v : adj.getOrDefault(u, Collections.emptyList())) {
            if (!visitados.contains(v)) {
                filhos++;
                pai.put(v, u);
                dfsArticulacao(v, raiz, adj, disc, low, pai, visitados, tempo, articulacoes);

                low.put(u, Math.min(low.get(u), low.get(v)));

                // Raiz da árvore de busca: só é ponto de articulação se tiver mais de um filho
                if (u.equals(raiz) && filhos > 1) {
                    articulacoes.add(u);
                }
                // Demais vértices: nenhuma aresta de volta da subárvore de v ultrapassa u
                if (!u.equals(raiz) && low.get(v) >= disc.get(u)) {
                    articulacoes.add(u);
                }
            } else if (!v.equals(pai.get(u))) {
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
            if (a.getAtivo() && adj.containsKey(u) && adj.containsKey(v)) {
                adj.get(u).add(v);
                adj.get(v).add(u);
            }
        }
        return adj;
    }
}
