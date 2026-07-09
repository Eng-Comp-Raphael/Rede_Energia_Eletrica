package algoritmos;

import estruturaGrafo.Vertice;
import estruturaGrafo.Aresta;

import java.util.*;

/**
 * @brief Busca o menor caminho (em peso/distancia) entre uma fonte e um poste
 *        afetado, ignorando postes/arestas inativos -- usado para achar rota
 *        alternativa apos queda de subestacao ou rompimento de cabo.
 */
public class CaminhoAlternativo<TIPO extends Comparable<TIPO>> {

    private final Map<TIPO, Vertice<TIPO>> vertice;
    private final List<Aresta<TIPO>> aresta;

    public CaminhoAlternativo(Map<TIPO, Vertice<TIPO>> vertice, List<Aresta<TIPO>> aresta) {
        this.vertice = vertice;
        this.aresta = aresta;
    }

    /** @return caminho da origem ate o destino, ou lista vazia se nao houver rota. */
    public List<TIPO> executar(TIPO origem, TIPO destino) {
        if (!vertice.containsKey(origem) || !vertice.containsKey(destino)) {
            return Collections.emptyList();
        }
        if (!vertice.get(origem).getAtivo() || !vertice.get(destino).getAtivo()) {
            return Collections.emptyList();
        }

        Map<TIPO, List<Aresta<TIPO>>> adj = new HashMap<>();
        for (TIPO id : vertice.keySet()) {
            if (vertice.get(id).getAtivo()) adj.put(id, new ArrayList<>());
        }
        for (Aresta<TIPO> a : aresta) {
            TIPO u = a.getU().getNome(), v = a.getV().getNome();
            if (a.getAtivo() && adj.containsKey(u) && adj.containsKey(v)) {
                adj.get(u).add(a);
                adj.get(v).add(a);
            }
        }

        Map<TIPO, Integer> dist = new HashMap<>();
        Map<TIPO, TIPO> pred = new HashMap<>();
        Set<TIPO> visitados = new HashSet<>();
        for (TIPO id : adj.keySet()) dist.put(id, Integer.MAX_VALUE);
        dist.put(origem, 0);

        while (visitados.size() < adj.size()) {
            TIPO u = null;
            int menor = Integer.MAX_VALUE;
            for (TIPO id : adj.keySet()) {
                if (!visitados.contains(id) && dist.get(id) < menor) {
                    menor = dist.get(id);
                    u = id;
                }
            }
            if (u == null) break;
            visitados.add(u);
            if (u.equals(destino)) break;

            for (Aresta<TIPO> a : adj.get(u)) {
                TIPO outro = a.getU().getNome().equals(u) ? a.getV().getNome() : a.getU().getNome();
                if (!visitados.contains(outro)) {
                    int novaDist = dist.get(u) + a.getLambda();
                    if (novaDist < dist.get(outro)) {
                        dist.put(outro, novaDist);
                        pred.put(outro, u);
                    }
                }
            }
        }

        if (dist.get(destino) == null || dist.get(destino) == Integer.MAX_VALUE) {
            return Collections.emptyList();
        }
        List<TIPO> caminho = new ArrayList<>();
        TIPO atual = destino;
        while (atual != null) {
            caminho.add(0, atual);
            atual = pred.get(atual);
        }
        return caminho;
    }
}