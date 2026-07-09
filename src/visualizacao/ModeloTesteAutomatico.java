package visualizacao;

import org.jxmapviewer.viewer.GeoPosition;
import estruturaGrafo.Grafo;
import visualizacao.PintorConexoes.TipoVertice;

public class ModeloTesteAutomatico {

    // Coordenadas baseadas no centro de Cruz das Almas
    public static final double LAT_BASE = -12.6736;
    public static final double LON_BASE = -39.1028;

    /**
     * Constrói uma rede complexa e realista sobre o mapa de Cruz das Almas.
     * Possui 2 Subestações, rotas redundantes e distribuição por bairros.
     */
    public static void construirRedeBase(Grafo<String> grafo, PintorConexoes pintor, VisualizadorRede interfacePrincipal) {
        
        // ==========================================
        // 1. CRIAÇÃO DOS VÉRTICES (NÓS)
        // ==========================================

        // --- SUBESTAÇÕES ---
        // Subestação principal no Centro
        adicionarNoSimulado(grafo, pintor, "Sub_Centro", LAT_BASE, LON_BASE, TipoVertice.SUBESTACAO);
        // Subestação secundária perto da Coplan (noroeste)
        adicionarNoSimulado(grafo, pintor, "Sub_Coplan", -12.6670, -39.1120, TipoVertice.SUBESTACAO);

        // --- TRONCO PRINCIPAL (Av. Getúlio Vargas) ---
        adicionarNoSimulado(grafo, pintor, "P_Vargas1", -12.6710, -39.1060, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "P_Vargas2", -12.6690, -39.1090, TipoVertice.POSTE);

        // --- RAMAL: Bairro Primavera (Nordeste) ---
        adicionarNoSimulado(grafo, pintor, "P_Prim1", -12.6700, -39.1000, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "P_Prim2", -12.6680, -39.0980, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "Casa_Primavera", -12.6670, -39.0970, TipoVertice.CASA);

        // --- RAMAL: Bairro Assembleia (Oeste/Sudoeste) ---
        adicionarNoSimulado(grafo, pintor, "P_Ass1", -12.6720, -39.1080, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "P_Ass2", -12.6740, -39.1100, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "Casa_Assembleia", -12.6750, -39.1110, TipoVertice.CASA);

        // --- RAMAL: Bairro Alberto Passos (Sudeste) ---
        adicionarNoSimulado(grafo, pintor, "P_Alb1", -12.6760, -39.1000, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "Casa_Alberto", -12.6780, -39.0980, TipoVertice.CASA);


        // ==========================================
        // 2. CRIAÇÃO DAS ARESTAS (CONEXÕES E PESOS)
        // ==========================================

        // Conectando o Tronco Principal (As duas subestações se encontram aqui)
        ligarNoSimulado(grafo, pintor, "Sub_Centro", "P_Vargas1", 350);
        ligarNoSimulado(grafo, pintor, "P_Vargas1", "P_Vargas2", 300);
        ligarNoSimulado(grafo, pintor, "P_Vargas2", "Sub_Coplan", 250); // Tie-line (Redundância de fonte)

        // Alimentando Primavera
        ligarNoSimulado(grafo, pintor, "Sub_Centro", "P_Prim1", 300);
        ligarNoSimulado(grafo, pintor, "P_Prim1", "P_Prim2", 280);
        ligarNoSimulado(grafo, pintor, "P_Prim2", "Casa_Primavera", 50);

        // Alimentando Assembleia
        ligarNoSimulado(grafo, pintor, "P_Vargas1", "P_Ass1", 200);
        ligarNoSimulado(grafo, pintor, "P_Ass1", "P_Ass2", 250);
        ligarNoSimulado(grafo, pintor, "P_Ass2", "Casa_Assembleia", 40);

        // Alimentando Alberto Passos
        ligarNoSimulado(grafo, pintor, "Sub_Centro", "P_Alb1", 320);
        ligarNoSimulado(grafo, pintor, "P_Alb1", "Casa_Alberto", 60);

        // ==========================================
        // 3. ROTAS DE REDUNDÂNCIA (MALHA / LOOPS)
        // ==========================================
        // Essas conexões são vitais para o algoritmo de Caminho Alternativo funcionar 
        // caso uma rota principal (ex: P_Vargas1) seja rompida.
        
        // Loop Norte: Conecta Primavera ao Tronco Principal
        ligarNoSimulado(grafo, pintor, "P_Prim1", "P_Vargas1", 400); 
        
        // Loop Sul: Conecta Assembleia ao Alberto Passos contornando o centro
        ligarNoSimulado(grafo, pintor, "P_Ass2", "P_Alb1", 600); 


        // ==========================================
        // 4. AJUSTE DE CÂMERA
        // ==========================================
        // Afasta um pouco o zoom (6) e centraliza entre Coplan e Centro para ver a cidade toda
        interfacePrincipal.getMapViewer().setZoom(6);
        interfacePrincipal.getMapViewer().setAddressLocation(new GeoPosition(-12.6710, -39.1050));
    }

    // --- Métodos utilitários mantidos ---
    
    private static void adicionarNoSimulado(Grafo<String> grafo, PintorConexoes pintor, String nome, double lat, double lon, TipoVertice tipo) {
        pintor.adicionarVertice(nome, new GeoPosition(lat, lon), tipo);
        grafo.addVertice(nome);
    }

    private static void ligarNoSimulado(Grafo<String> grafo, PintorConexoes pintor, String u, String v, int peso) {
        pintor.adicionarAresta(u, v, peso);
        grafo.addAresta(u, v, peso);
    }
}