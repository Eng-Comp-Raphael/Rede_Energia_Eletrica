package estruturaGrafo;
/**
 * @file Aresta.java
 * @author Raphael Batista 
 * @date 2026
 * @brief Criação de arestas
 */

public class Aresta<TIPO extends Comparable<TIPO>> {

    private Vertice<TIPO> u;
    private Vertice<TIPO> v;
    private int lambda;

    public Aresta(Vertice<TIPO> u, Vertice<TIPO> v, int lambda){
        this.u = u;
        this.v = v;
        this.lambda = lambda;
    }

    public Vertice<TIPO> getU() {
        return u;
    }

    public void setU(Vertice<TIPO> u) {
        this.u = u;
    }

    public Vertice<TIPO> getV() {
        return v;
    }

    public void setV(Vertice<TIPO> v) {
        this.v = v;
    }

    public int getLambda() {
        return lambda;
    }

    public void setLambda(int lambda) {
        this.lambda = lambda;
    }
    
}
