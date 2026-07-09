package visualizacao;

import org.jxmapviewer.viewer.GeoPosition;
import estruturaGrafo.Grafo;
import visualizacao.PintorConexoes.TipoVertice;

public class ModeloTesteAutomatico {

    // Coordenadas baseadas no centro geográfico de Cruz das Almas
    public static final double LAT_BASE = -12.6736;
    public static final double LON_BASE = -39.1028;

    /**
     * Constrói uma infraestrutura elétrica macro que compreende toda a cidade
     * de Cruz das Almas, contendo subestações redundantes, anéis de transmissão
     * e ramificações complexas para testes massivos de fluxo e algoritmos.
     */
    public static void construirRedeBase(Grafo<String> grafo, PintorConexoes pintor, VisualizadorRede interfacePrincipal) {
        
        // ============================================================
        // 1. CRIAÇÃO DAS SUBESTAÇÕES E MATRIZES DE ENERGIA (4 CANTOS)
        // ============================================================
        // Subestação Principal (Centro)
        adicionarNoSimulado(grafo, pintor, "Sub_Centro", LAT_BASE, LON_BASE, TipoVertice.SUBESTACAO);
        // Subestação Noroeste (Coplan)
        adicionarNoSimulado(grafo, pintor, "Sub_Coplan", -12.6670, -39.1120, TipoVertice.SUBESTACAO);
        // Subestação Norte (Campus Universitário UFRB)
        adicionarNoSimulado(grafo, pintor, "Sub_UFRB", -12.6585, -39.1050, TipoVertice.SUBESTACAO);
        // Subestação Leste (Expansão Urbana / Inocoop)
        adicionarNoSimulado(grafo, pintor, "Sub_Inocoop", -12.6750, -39.0910, TipoVertice.SUBESTACAO);


        // ============================================================
        // 2. TRONCOS PRINCIPAIS E BAIRROS EXISTENTES (MANTIDOS P/ TESTES)
        // ============================================================
        // Tronco Av. Getúlio Vargas
        adicionarNoSimulado(grafo, pintor, "P_Vargas1", -12.6710, -39.1060, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "P_Vargas2", -12.6690, -39.1090, TipoVertice.POSTE);

        // Bairro Primavera (Nordeste)
        adicionarNoSimulado(grafo, pintor, "P_Prim1", -12.6700, -39.1000, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "P_Prim2", -12.6680, -39.0980, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "Casa_Primavera", -12.6670, -39.0970, TipoVertice.CASA);

        // Bairro Assembleia (Oeste)
        adicionarNoSimulado(grafo, pintor, "P_Ass1", -12.6720, -39.1080, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "P_Ass2", -12.6740, -39.1100, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "Casa_Assembleia", -12.6750, -39.1110, TipoVertice.CASA);

        // Bairro Alberto Passos (Sudeste)
        adicionarNoSimulado(grafo, pintor, "P_Alb1", -12.6760, -39.1000, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "Casa_Alberto", -12.6780, -39.0980, TipoVertice.CASA);


        // ============================================================
        // 3. EXPANSÃO: NOVOS SETORES COMPREENSIVOS DA CIDADE
        // ============================================================
        // --- Setor Norte: Campus UFRB ---
        adicionarNoSimulado(grafo, pintor, "P_Ufrb1", -12.6610, -39.1040, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "P_Ufrb2", -12.6635, -39.1045, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "Casa_Campus", -12.6600, -39.1025, TipoVertice.CASA);

        // --- Setor Leste: Inocoop e Cajá ---
        adicionarNoSimulado(grafo, pintor, "P_Inocoop1", -12.6725, -39.0930, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "P_Inocoop2", -12.6765, -39.0900, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "Casa_InocoopResidencial", -12.6775, -39.0885, TipoVertice.CASA);

        // --- Setor Sul: Miradouro ---
        adicionarNoSimulado(grafo, pintor, "P_Mira1", -12.6820, -39.1020, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "P_Mira2", -12.6860, -39.1015, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "Casa_Miradouro", -12.6880, -39.1010, TipoVertice.CASA);

        // --- Setor Sudoeste: Tabela e Reta ---
        adicionarNoSimulado(grafo, pintor, "P_Tab1", -12.6800, -39.1110, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "P_Tab2", -12.6830, -39.1150, TipoVertice.POSTE);
        adicionarNoSimulado(grafo, pintor, "Casa_Tabela", -12.6850, -39.1170, TipoVertice.CASA);


        // ============================================================
        // 4. ANEL DE TRANSMISSÃO MACRO (INTERCONEXÃO DE SUBESTAÇÕES)
        // ============================================================
        // Cria circuitos redundantes de altíssima confiabilidade entre as fontes
        ligarNoSimulado(grafo, pintor, "Sub_Centro", "Sub_Inocoop", 1300);
        ligarNoSimulado(grafo, pintor, "Sub_Inocoop", "Sub_UFRB", 1900);
        ligarNoSimulado(grafo, pintor, "Sub_UFRB", "Sub_Coplan", 1200);
        ligarNoSimulado(grafo, pintor, "Sub_Coplan", "Sub_Centro", 950);


        // ============================================================
        // 5. CABEAMENTO E ACIONAMENTO DA REDE DE DISTRIBUIÇÃO (ARESTAS)
        // ============================================================
        // Tronco Central (Coplan <-> Centro via Av. Getúlio Vargas)
        ligarNoSimulado(grafo, pintor, "Sub_Centro", "P_Vargas1", 350);
        ligarNoSimulado(grafo, pintor, "P_Vargas1", "P_Vargas2", 300);
        ligarNoSimulado(grafo, pintor, "P_Vargas2", "Sub_Coplan", 250);

        // Alimentação Primavera (Nordeste)
        ligarNoSimulado(grafo, pintor, "Sub_Centro", "P_Prim1", 300);
        ligarNoSimulado(grafo, pintor, "P_Prim1", "P_Prim2", 280);
        ligarNoSimulado(grafo, pintor, "P_Prim2", "Casa_Primavera", 50);

        // Alimentação Assembleia (Oeste)
        ligarNoSimulado(grafo, pintor, "P_Vargas1", "P_Ass1", 200);
        ligarNoSimulado(grafo, pintor, "P_Ass1", "P_Ass2", 250);
        ligarNoSimulado(grafo, pintor, "P_Ass2", "Casa_Assembleia", 40);

        // Alimentação Alberto Passos (Sudeste)
        ligarNoSimulado(grafo, pintor, "Sub_Centro", "P_Alb1", 320);
        ligarNoSimulado(grafo, pintor, "P_Alb1", "Casa_Alberto", 60);

        // Rede do Campus (Sub_UFRB descendo em direção à Getúlio Vargas)
        ligarNoSimulado(grafo, pintor, "Sub_UFRB", "P_Ufrb1", 280);
        ligarNoSimulado(grafo, pintor, "P_Ufrb1", "P_Ufrb2", 270);
        ligarNoSimulado(grafo, pintor, "P_Ufrb1", "Casa_Campus", 110);
        ligarNoSimulado(grafo, pintor, "P_Ufrb2", "P_Vargas1", 520); // Link redundante Campus -> Tronco

        // Rede Leste (Inocoop conectando ao Centro)
        ligarNoSimulado(grafo, pintor, "Sub_Inocoop", "P_Inocoop1", 340);
        ligarNoSimulado(grafo, pintor, "P_Inocoop1", "P_Inocoop2", 450);
        ligarNoSimulado(grafo, pintor, "P_Inocoop2", "Casa_InocoopResidencial", 150);
        ligarNoSimulado(grafo, pintor, "P_Inocoop1", "P_Prim1", 610); // Malha entre Inocoop e Primavera

        // Rede Sul (Miradouro ligando ao Alberto Passos)
        ligarNoSimulado(grafo, pintor, "Sub_Centro", "P_Mira1", 910);
        ligarNoSimulado(grafo, pintor, "P_Mira1", "P_Mira2", 440);
        ligarNoSimulado(grafo, pintor, "P_Mira2", "Casa_Miradouro", 220);
        ligarNoSimulado(grafo, pintor, "P_Mira1", "P_Alb1", 380); // Redundância Sul

        // Rede Sudoeste (Tabela fechando o loop com Assembleia e Miradouro)
        ligarNoSimulado(grafo, pintor, "Sub_Coplan", "P_Tab1", 1400);
        ligarNoSimulado(grafo, pintor, "P_Tab1", "P_Tab2", 400);
        ligarNoSimulado(grafo, pintor, "P_Tab2", "Casa_Tabela", 230);
        ligarNoSimulado(grafo, pintor, "P_Tab1", "P_Ass2", 490);  // Interconexão Tabela -> Assembleia
        ligarNoSimulado(grafo, pintor, "P_Tab2", "P_Mira2", 1100); // Grande linha de malha perimetral sul


        // ============================================================
        // 6. ROTAS COMPLEMENTARES DE REDUNDÂNCIA (MALHA COMPLETA)
        // ============================================================
        ligarNoSimulado(grafo, pintor, "P_Prim1", "P_Vargas1", 400); 
        ligarNoSimulado(grafo, pintor, "P_Ass2", "P_Alb1", 600); 


        // ============================================================
        // 7. AJUSTE GEOGRÁFICO DE CÂMERA (SEM ALTERAR O ZOOM)
        // ============================================================
        // Centraliza a visão perfeitamente no meio do mapa urbano de Cruz das Almas
        interfacePrincipal.getMapViewer().setAddressLocation(new GeoPosition(-12.6710, -39.1050));
    }

    // --- Métodos utilitários encapsulados ---
    
    private static void adicionarNoSimulado(Grafo<String> grafo, PintorConexoes pintor, String nome, double lat, double lon, TipoVertice tipo) {
        pintor.adicionarVertice(nome, new GeoPosition(lat, lon), tipo);
        grafo.addVertice(nome);
    }

    private static void ligarNoSimulado(Grafo<String> grafo, PintorConexoes pintor, String u, String v, int peso) {
        pintor.adicionarAresta(u, v, peso);
        grafo.addAresta(u, v, peso);
    }
}