import estruturaGrafo.Grafo;

public class AmbienteTeste {
    public static void main(String[] args) {
        Grafo<String> rede = new Grafo<>();

        rede.addVertice("Sub1");
        rede.addVertice("P1"); rede.addAresta("Sub1", "P1", 100);
        rede.addVertice("P2"); rede.addAresta("P1", "P2", 80);
        rede.addVertice("P3"); rede.addAresta("P2", "P3", 60);
        rede.addVertice("P4"); rede.addAresta("Sub1", "P4", 120); rede.addAresta("P4", "P3", 90); // rota alternativa (loop)
        rede.addVertice("Casa1"); rede.addAresta("P3", "Casa1", 20);
        rede.addVertice("P5"); rede.addAresta("Sub1", "P5", 150);
        
        rede.printGrafo();

        // 1. Simular queda de subestacao
        rede.simularQuedaSubestacao("Sub1");

        // reset pra proximo teste
        rede.setPosteAtivo("Sub1", true);

        // 1b. Simular rompimento de tubulacao (aresta especifica)
        rede.simularRompimentoTubulacao("P1", "P2");

        // 2. Identificar bairros/postes afetados (ja usado acima via printAreasAfetadas)

        // 3. Encontrar caminho alternativo apos o rompimento
        rede.printCaminhoAlternativo("Sub1", "P3");

        rede.repararAresta("P1", "P2");

        // 4. Sugerir pontos prioritarios
        rede.printPontosPrioritarios();
    }
}
