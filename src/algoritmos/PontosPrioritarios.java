package algoritmos;

import estruturaGrafo.Vertice;
import estruturaGrafo.Aresta;

import java.util.*;

/** @brief Combina pontes + grau de conectividade + comprimento do cabo para
 *         sugerir onde priorizar manutencao preventiva. */
public class PontosPrioritarios<TIPO extends Comparable<TIPO>> {

    private final Map<TIPO, Vertice<TIPO>> vertice;
    private final List<Aresta<TIPO>> aresta;

    public PontosPrioritarios(Map<TIPO, Vertice<TIPO>> vertice, List<Aresta<TIPO>> aresta) {
        this.vertice = vertice;
        this.aresta = aresta;
    }

    public List<Aresta<TIPO>> pontesCriticas() {
        return new Pontes<>(vertice, aresta).executar();
    }

    /** Top-N postes com menor grau de conexao (1 conexao = maior risco de isolar area). */
    public List<TIPO> postesMaisFragilizados(int topN) {
        Map<TIPO, Integer> grau = new HashMap<>();
        for (TIPO id : vertice.keySet()) grau.put(id, 0);
        for (Aresta<TIPO> a : aresta) {
            if (!a.getAtivo()) continue;
            grau.merge(a.getU().getNome(), 1, Integer::sum);
            grau.merge(a.getV().getNome(), 1, Integer::sum);
        }
        List<TIPO> ranking = new ArrayList<>(vertice.keySet());
        ranking.sort(Comparator.comparingInt(grau::get)); // menor grau primeiro
        return ranking.subList(0, Math.min(topN, ranking.size()));
    }

    /** Top-N cabos mais longos (maior tempo/custo de reparo, maior risco). */
    public List<Aresta<TIPO>> cabosMaisLongos(int topN) {
        List<Aresta<TIPO>> ordenado = new ArrayList<>(aresta);
        ordenado.sort((a, b) -> Integer.compare(b.getLambda(), a.getLambda()));
        return ordenado.subList(0, Math.min(topN, ordenado.size()));
    }
}