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
 * @file AlgoritmoAreasAfetadas.java
 * @brief Identifica quais postes ficam sem atendimento a partir de uma fonte
 *        de distribuição, considerando o estado atual de "ativo" de cada poste.
 *
 * Um poste é considerado afetado se:
 *  (a) ele mesmo está marcado como inativo, OU
 *  (b) está ativo, mas não há mais nenhum caminho de postes ativos ligando
 *      ele até a fonte (a falha de outro poste no meio do caminho isolou
 *      essa área).
 */
public class AreasAfetadas<TIPO extends Comparable<TIPO>> {

    private final Map<TIPO, Vertice<TIPO>> vertice;
    private final List<Aresta<TIPO>> aresta;

    public AreasAfetadas(Map<TIPO, Vertice<TIPO>> vertice, List<Aresta<TIPO>> aresta) {
        this.vertice = vertice;
        this.aresta = aresta;
    }

    /**
     * @param fonte vértice de origem da distribuição (estação/subestação principal)
     * @return lista de postes sem atendimento
     */
    public List<TIPO> executar(TIPO fonte) {
        List<TIPO> afetados = new ArrayList<>();

        Vertice<TIPO> fonteVertice = vertice.get(fonte);
        if (fonteVertice == null) {
            System.out.println("Erro: fonte [" + fonte + "] não existe no grafo");
            return afetados;
        }
        if (!fonteVertice.getAtivo()) {
            // a propria fonte caiu: todo o resto fica sem atendimento
            for (TIPO id : vertice.keySet()) {
                if (!id.equals(fonte)) {
                    afetados.add(id);
                }
            }
            return afetados;
        }

        Map<TIPO, List<TIPO>> adj = construirListaAdjacencia(); // já filtra postes inativos
        Set<TIPO> alcancaveis = new HashSet<>();
        Queue<TIPO> fila = new LinkedList<>();
        alcancaveis.add(fonte);
        fila.add(fonte);

        while (!fila.isEmpty()) {
            TIPO atual = fila.poll();
            for (TIPO vizinho : adj.getOrDefault(atual, Collections.emptyList())) {
                if (!alcancaveis.contains(vizinho)) {
                    alcancaveis.add(vizinho);
                    fila.add(vizinho);
                }
            }
        }

        for (TIPO id : vertice.keySet()) {
            if (id.equals(fonte)) {
                continue;
            }
            boolean posteAtivo = vertice.get(id).getAtivo();
            if (!posteAtivo || !alcancaveis.contains(id)) {
                afetados.add(id);
            }
        }
        return afetados;
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