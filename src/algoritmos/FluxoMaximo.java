package algoritmos;

import estruturaGrafo.Vertice;
import estruturaGrafo.Aresta;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * @file AlgoritmoFluxoMaximo.java
 * @brief Fluxo máximo entre dois postes, usando Edmonds-Karp (Ford-Fulkerson
 *        com busca em largura), tratando "lambda" de cada aresta como a
 *        capacidade de distribuição (ex: capacidade de tubulação/linha).
 *
 * Como a rede é não-direcionada, cada aresta {u,v,lambda} vira duas arestas
 * residuais (u->v e v->u), ambas com capacidade lambda. Postes inativos são
 * ignorados por completo.
 */
public class FluxoMaximo<TIPO extends Comparable<TIPO>> {

    private final Map<TIPO, Vertice<TIPO>> vertice;
    private final List<Aresta<TIPO>> aresta;

    public FluxoMaximo(Map<TIPO, Vertice<TIPO>> vertice, List<Aresta<TIPO>> aresta) {
        this.vertice = vertice;
        this.aresta = aresta;
    }

    /** Calcula o fluxo máximo entre "origem" e "destino". */
    public int executar(TIPO origem, TIPO destino) {
        if (!vertice.containsKey(origem) || !vertice.containsKey(destino)) {
            System.out.println("Erro: origem ou destino não existem no grafo");
            return -1;
        }
        if (!vertice.get(origem).getAtivo() || !vertice.get(destino).getAtivo()) {
            System.out.println("Aviso: origem ou destino está com o poste inativo, fluxo máximo = 0.");
            return 0;
        }

        Map<TIPO, Map<TIPO, Integer>> capResidual = construirCapacidadeResidual();
        int fluxoTotal = 0;

        Map<TIPO, TIPO> pai;
        while ((pai = bfsCaminhoAumentante(capResidual, origem, destino)) != null) {
            // encontra o gargalo do caminho encontrado
            int fluxoCaminho = Integer.MAX_VALUE;
            TIPO v = destino;
            while (!v.equals(origem)) {
                TIPO u = pai.get(v);
                fluxoCaminho = Math.min(fluxoCaminho, capResidual.get(u).get(v));
                v = u;
            }

            // atualiza as capacidades residuais ao longo do caminho
            v = destino;
            while (!v.equals(origem)) {
                TIPO u = pai.get(v);
                capResidual.get(u).merge(v, -fluxoCaminho, Integer::sum);
                capResidual.computeIfAbsent(v, k -> new HashMap<>()).merge(u, fluxoCaminho, Integer::sum);
                v = u;
            }

            fluxoTotal += fluxoCaminho;
        }

        return fluxoTotal;
    }

    private Map<TIPO, Map<TIPO, Integer>> construirCapacidadeResidual() {
        Map<TIPO, Map<TIPO, Integer>> cap = new HashMap<>();
        for (TIPO id : vertice.keySet()) {
            if (vertice.get(id).getAtivo()) {
                cap.put(id, new HashMap<>());
            }
        }
        for (Aresta<TIPO> a : aresta) {
            TIPO u = a.getU().getNome();
            TIPO v = a.getV().getNome();
            int lambda = a.getLambda();
            if (a.getAtivo() && cap.containsKey(u) && cap.containsKey(v)) {
                cap.get(u).merge(v, lambda, Integer::sum);
                cap.get(v).merge(u, lambda, Integer::sum);
            }
        }
        return cap;
    }

    /** Busca um caminho aumentante de "origem" até "destino" via BFS. */
    private Map<TIPO, TIPO> bfsCaminhoAumentante(Map<TIPO, Map<TIPO, Integer>> capResidual, TIPO origem, TIPO destino) {
        Map<TIPO, TIPO> pai = new HashMap<>();
        Set<TIPO> visitados = new HashSet<>();
        Queue<TIPO> fila = new LinkedList<>();

        visitados.add(origem);
        fila.add(origem);

        while (!fila.isEmpty()) {
            TIPO atual = fila.poll();
            if (atual.equals(destino)) {
                return pai;
            }
            Map<TIPO, Integer> vizinhos = capResidual.getOrDefault(atual, Collections.emptyMap());
            for (Map.Entry<TIPO, Integer> entry : vizinhos.entrySet()) {
                TIPO vizinho = entry.getKey();
                int capacidade = entry.getValue();
                if (capacidade > 0 && !visitados.contains(vizinho)) {
                    visitados.add(vizinho);
                    pai.put(vizinho, atual);
                    fila.add(vizinho);
                }
            }
        }
        return visitados.contains(destino) ? pai : null;
    }
}