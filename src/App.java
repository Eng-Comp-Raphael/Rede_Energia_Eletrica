/**
 * @file App.java
 * @author Raphael Batista 
 * @date 2026
 * @brief Testes simples
 */

public class App { // testes
    public static void main(String[] args) throws Exception {
        Grafo<String> grafo = new Grafo<String>();
        grafo.addVertice("A");
        grafo.addVertice("B");
        grafo.addVertice("C");
        grafo.addElementoVertice("A", "1");
        grafo.addElementoVertice("A", "3");
        grafo.addElementoVertice("A", "0");
        grafo.addElementoVertice("A", "10");
        grafo.addElementoVertice("B", "11");
        grafo.addElementoVertice("B", "5");
        grafo.addElementoVertice("B", "15");
        grafo.addElementoVertice("C", "7");
        grafo.addElementoVertice("C", "9");
        grafo.addAresta("A", "B", 1);
        grafo.addAresta("A", "C", 5);
        grafo.addAresta("B", "C", 3);
        grafo.addAresta("C", "A", 10);
        grafo.printGrafo();
    }
}
