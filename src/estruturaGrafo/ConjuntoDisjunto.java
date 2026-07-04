package estruturaGrafo;
import java.util.HashMap;
import java.util.Map;

public class ConjuntoDisjunto<TIPO extends Comparable<TIPO>> {
    private Map<TIPO, TIPO> pai;
    private Map<TIPO, Integer> rank;

    public ConjuntoDisjunto(){
        this.pai = new HashMap<>();
        this.rank = new HashMap<>();
    }

    public void criarConjunto(TIPO elemento){
        pai.put(elemento, elemento);
        rank.put(elemento, 0);
    }

    public TIPO encontrar(TIPO elemento){
        if(!pai.get(elemento).equals(elemento)){
            pai.put(elemento, encontrar(pai.get(elemento)));
        }
        return pai.get(elemento);
    }

    public boolean unir(TIPO a, TIPO b){
        TIPO raizA = encontrar(a);
        TIPO raizB = encontrar(b);

        if(raizA.equals(raizB)){
            return false;
        }

        int rankA = rank.get(raizA);
        int rankB = rank.get(raizB);

        if(rankA < rankB){
            pai.put(raizA, raizB);
        }else if(rankA > rankB){
            pai.put(raizB, raizA);
        }else{
            pai.put(raizB, raizA);
            rank.put(raizA, rankA +1);
        }
        return true;
    }
}
