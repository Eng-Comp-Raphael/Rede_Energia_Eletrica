/**
 * @file App.java
 * @authors Raphael Batista ...
 * @date 2026
 * @brief Testes simples
 */

public class App { // testes
    public static void main(String[] args) throws Exception {

        // Inicia a janela visual
        visualizacao.VisualizadorRede janela = new visualizacao.VisualizadorRede();
        janela.setVisible(true);
        
        // Opcional: Adicionar arestas programaticamente (os vértices você pode clicar
        // no mapa para criar)
        // janela.conectarPostes("Poste_1", "Poste_2");
    }
}