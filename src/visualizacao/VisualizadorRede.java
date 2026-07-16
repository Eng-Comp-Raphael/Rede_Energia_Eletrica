package visualizacao;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;

import visualizacao.PintorConexoes.TipoVertice;
import estruturaGrafo.Grafo;
import estruturaGrafo.Aresta;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter; // NOVO
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.BufferedReader; // NOVO
import java.io.File; // NOVO
import java.io.FileReader; // NOVO
import java.io.FileWriter; // NOVO
import java.io.PrintWriter; // NOVO
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class VisualizadorRede extends JFrame {

    private JXMapViewer mapViewer;
    private PintorConexoes pintorConexoes;
    private Grafo<String> grafoRede;
    private boolean autoConectarAtivado = true; // controla se novos vertices se conectam automaticamente ao ultimo

    // criado

    private enum Modo {
        NAVEGAR, CRIAR_VERTICE, LIGAR_VERTICES, REMOVER_VERTICE, ALTERNAR_FALHA
    }

    private Modo modoAtual = Modo.NAVEGAR;
    private String verticeOrigemLigacao = null;
    private Timer timerAnimacao;
    private TipoVertice tipoVerticeSelecionado = TipoVertice.POSTE;
    private JComboBox<String> comboVisualizacao;

    public VisualizadorRede() {
        super("Simulador de Distribuição de Rede");
        setSize(1300, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        System.setProperty("http.agent", "SimuladorDeRede/1.0");
        grafoRede = new Grafo<String>();

        mapViewer = new JXMapViewer();
        OSMTileFactoryInfo info = new OSMTileFactoryInfo() {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                return super.getTileUrl(x, y, zoom).replace("http://", "https://");
            }
        };
        mapViewer.setTileFactory(new DefaultTileFactory(info));
        mapViewer.setZoom(7);
        mapViewer.setAddressLocation(new GeoPosition(-12.6736, -39.1028));

        pintorConexoes = new PintorConexoes(mapViewer);
        mapViewer.setOverlayPainter(pintorConexoes);

        PanMouseInputListener panListener = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(panListener);
        mapViewer.addMouseMotionListener(panListener);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));

        // ==========================================
        // BARRA DE FERRAMENTAS
        // ==========================================
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton btnArquivo = new JButton("📁 Arquivo ▼");
        JButton btnTestes = new JButton("🧪 Testes ▼");
        JToggleButton btnNavegar = new JToggleButton("🖐 Navegar", true);
        JToggleButton btnCriarVertice = new JToggleButton("📍 Add: Poste ▼");
        JToggleButton btnLigar = new JToggleButton("🔗 Ligar Vértices");
        JToggleButton btnRemover = new JToggleButton("🗑️ Remover");
        JToggleButton btnFalha = new JToggleButton("💥 Simular Falha");
        JButton btnAlgoritmos = new JButton("⚙️ Algoritmos ▼");
        JToggleButton btnAnimacao = new JToggleButton("⚡ Pausar");
        JToggleButton btnAutoConectar = new JToggleButton("🔗 Auto-Conectar", true); // NOVO
        JToggleButton btnLegenda = new JToggleButton("☰ Legenda", true);

        comboVisualizacao = new JComboBox<>(new String[] { "Visão: Grafo Completo", "Visão: Resultado de Busca" });
        comboVisualizacao.addActionListener(e -> {
            boolean mostrarSubgrafo = comboVisualizacao.getSelectedIndex() == 1;
            pintorConexoes.setMostrarApenasDestaques(mostrarSubgrafo);
            mapViewer.repaint();
        });
        comboVisualizacao.setEnabled(false);

        ButtonGroup grupoFerramentas = new ButtonGroup();
        grupoFerramentas.add(btnNavegar);
        grupoFerramentas.add(btnCriarVertice);
        grupoFerramentas.add(btnLigar);
        grupoFerramentas.add(btnRemover);
        grupoFerramentas.add(btnFalha);

        // Menu Arquivo (Salvar/Carregar)
        JPopupMenu popupArquivo = new JPopupMenu();
        JMenuItem itemSalvar = new JMenuItem("💾 Salvar Rede...");
        JMenuItem itemCarregar = new JMenuItem("📂 Carregar Rede...");
        JMenuItem itemLimparRede = new JMenuItem("❌ Limpar Tudo (Reset)");

        itemSalvar.addActionListener(e -> salvarRede());
        itemCarregar.addActionListener(e -> carregarRede());

        // Configuração da ação do botão de limpeza manual
        itemLimparRede.addActionListener(e -> {
            grafoRede.limparGrafo();          // Limpa as estruturas do Grafo
            pintorConexoes.limparTudo();      // Limpa os elementos visuais do mapa
            recalcularEnergia();              // Recalcula o fluxo elétrico (zerando-o)
            mapViewer.repaint();              // Atualiza a renderização da tela
            JOptionPane.showMessageDialog(this, "O simulador foi reiniciado e toda a rede foi limpa com sucesso!");
        });

        popupArquivo.add(itemSalvar);
        popupArquivo.add(itemCarregar);
        popupArquivo.addSeparator();         
        popupArquivo.add(itemLimparRede);
        btnArquivo.addActionListener(e -> popupArquivo.show(btnArquivo, 0, btnArquivo.getHeight()));

        // Configurando o Popup Menu dos Testes
        JPopupMenu popupTestes = new JPopupMenu();
        JMenuItem itemTesteAuto = new JMenuItem("🤖 Rodar Cenário Automático (AmbienteTeste)");
        JMenuItem itemTestePers = new JMenuItem("🛠️ Testar Rede Desenhada (Personalizado)");

        // Instancia o novo Gerenciador
        GerenciadorTestes gerenciadorTestes = new GerenciadorTestes(this);

        itemTesteAuto.addActionListener(e -> gerenciadorTestes.iniciarTesteAutomatico());
        itemTestePers.addActionListener(e -> gerenciadorTestes.executarTestePersonalizado());

        popupTestes.add(itemTesteAuto);
        popupTestes.add(itemTestePers);
        btnTestes.addActionListener(e -> popupTestes.show(btnTestes, 0, btnTestes.getHeight()));

        popupTestes.add(itemTesteAuto);
        popupTestes.add(itemTestePers);
        btnTestes.addActionListener(e -> popupTestes.show(btnTestes, 0, btnTestes.getHeight()));

        // Menu Adicionar Vértice
        JPopupMenu popupVertices = new JPopupMenu();
        JMenuItem itemPoste = new JMenuItem("Poste");
        JMenuItem itemCasa = new JMenuItem("Casa");
        JMenuItem itemSub = new JMenuItem("Subestação");
        itemPoste.addActionListener(e -> atualizarFerramentaVertice(btnCriarVertice, TipoVertice.POSTE, "Poste"));
        itemCasa.addActionListener(e -> atualizarFerramentaVertice(btnCriarVertice, TipoVertice.CASA, "Casa"));
        itemSub.addActionListener(
                e -> atualizarFerramentaVertice(btnCriarVertice, TipoVertice.SUBESTACAO, "Subestação"));
        popupVertices.add(itemPoste);
        popupVertices.add(itemCasa);
        popupVertices.add(itemSub);

        // Menu Algoritmos
        JPopupMenu popupAlgoritmos = new JPopupMenu();
        JMenuItem itemAGM = new JMenuItem("1. Árvore Geradora Mínima (AGM)");
        JMenuItem itemPontes = new JMenuItem("2. Pontes (Conexões Críticas)");
        JMenuItem itemDFS = new JMenuItem("3. Busca em Profundidade (DFS)");
        JMenuItem itemBFS = new JMenuItem("4. Busca em Largura (BFS)");
        JMenuItem itemFluxo = new JMenuItem("5. Fluxo Máximo (Capacidade)");
        JMenuItem itemLimpar = new JMenuItem("❌ Limpar Destaques e Voltar ao Normal");
        JMenuItem itemDiagnostico = new JMenuItem("🔍 Diagnóstico: Arestas Duplicadas");

        itemAGM.addActionListener(e -> executarAGM());
        itemPontes.addActionListener(e -> executarPontes());
        itemDFS.addActionListener(e -> executarBusca("DFS"));
        itemBFS.addActionListener(e -> executarBusca("BFS"));
        itemFluxo.addActionListener(e -> executarFluxoMaximo());
        itemDiagnostico.addActionListener(e -> grafoRede.printArestasDuplicadas());
        itemLimpar.addActionListener(e -> {
            pintorConexoes.limparDestaquesAlgoritmos();
            comboVisualizacao.setSelectedIndex(0);
            comboVisualizacao.setEnabled(false);
            mapViewer.repaint();
        });

        popupAlgoritmos.add(itemAGM);
        popupAlgoritmos.add(itemPontes);
        popupAlgoritmos.add(itemDFS);
        popupAlgoritmos.add(itemBFS);
        popupAlgoritmos.add(itemFluxo);
        popupAlgoritmos.add(itemDiagnostico); // NOVO
        popupAlgoritmos.addSeparator();
        popupAlgoritmos.add(itemLimpar);

        btnAutoConectar.addActionListener(e -> autoConectarAtivado = btnAutoConectar.isSelected());
        btnLegenda.addActionListener(e -> {
            pintorConexoes.setMostrarLegenda(btnLegenda.isSelected());
            mapViewer.repaint();
        });
        btnNavegar.addActionListener(e -> limparSelecao(Modo.NAVEGAR));
        btnCriarVertice.addActionListener(e -> {
            limparSelecao(Modo.CRIAR_VERTICE);
            popupVertices.show(btnCriarVertice, 0, btnCriarVertice.getHeight());
        });
        btnLigar.addActionListener(e -> limparSelecao(Modo.LIGAR_VERTICES));
        btnRemover.addActionListener(e -> limparSelecao(Modo.REMOVER_VERTICE));
        btnFalha.addActionListener(e -> {
            limparSelecao(Modo.ALTERNAR_FALHA);
            gerenciadorTestes.executarTestePersonalizado();
        });

        btnAlgoritmos.addActionListener(e -> popupAlgoritmos.show(btnAlgoritmos, 0, btnAlgoritmos.getHeight()));
        btnAnimacao.addActionListener(e -> {
            if (timerAnimacao.isRunning()) {
                timerAnimacao.stop();
                btnAnimacao.setText("⚡ Iniciar");
                btnAnimacao.setSelected(true);
            } else {
                timerAnimacao.start();
                btnAnimacao.setText("⚡ Pausar");
                btnAnimacao.setSelected(false);
            }
        });

        toolBar.add(btnArquivo);
        toolBar.add(btnTestes);
        toolBar.addSeparator();
        toolBar.add(btnNavegar);
        toolBar.addSeparator();
        toolBar.add(btnCriarVertice);
        toolBar.add(btnAutoConectar);
        toolBar.addSeparator();
        toolBar.add(btnLigar);
        toolBar.addSeparator();
        toolBar.add(btnRemover);
        toolBar.addSeparator();
        toolBar.add(btnFalha);
        toolBar.addSeparator();
        toolBar.add(btnAlgoritmos);
        toolBar.addSeparator();
        toolBar.add(comboVisualizacao);
        toolBar.addSeparator();
        toolBar.add(btnAnimacao);
        toolBar.addSeparator();
        toolBar.add(btnLegenda);
        add(toolBar, BorderLayout.NORTH);

        // ==========================================
        // LÓGICA DE CLIQUES NO MAPA
        // ==========================================
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1)
                    return;
                Point2D pontoTela = e.getPoint();

                if (modoAtual == Modo.CRIAR_VERTICE) {
                    String verticeExistente = pintorConexoes.getVerticeProximo(pontoTela, mapViewer, 20);

                    if (verticeExistente != null) {
                        // Clicou em cima de um vertice JA existente: nao cria um novo,
                        // so conecta a ele (se o auto-conectar estiver ligado)
                        String verticeAnterior = grafoRede.getUltimoVerticeAdicionado();
                        if (autoConectarAtivado && verticeAnterior != null
                                && !verticeAnterior.equals(verticeExistente)) {
                            GeoPosition posAnterior = pintorConexoes.getVertices().get(verticeAnterior).posicao;
                            GeoPosition posExistente = pintorConexoes.getVertices().get(verticeExistente).posicao;
                            int pesoConexao = (int) calcularDistancia(posAnterior.getLatitude(),
                                    posAnterior.getLongitude(),
                                    posExistente.getLatitude(), posExistente.getLongitude());

                            grafoRede.addAresta(verticeAnterior, verticeExistente, pesoConexao);
                            pintorConexoes.adicionarAresta(verticeAnterior, verticeExistente, pesoConexao);
                        }

                        // continua a cadeia a partir do vertice que voce acabou de clicar
                        grafoRede.setUltimoVerticeAdicionado(verticeExistente);
                        recalcularEnergia();
                        return;
                    }

                    // Nenhum vertice existente aqui -> cria um novo, como antes
                    GeoPosition geo = mapViewer.convertPointToGeoPosition(pontoTela);
                    String prefixo = (tipoVerticeSelecionado == TipoVertice.SUBESTACAO) ? "Sub_"
                            : (tipoVerticeSelecionado == TipoVertice.CASA) ? "Casa_" : "Poste_";
                    String nome = prefixo + (System.currentTimeMillis() % 10000);

                    String verticeAnterior = grafoRede.getUltimoVerticeAdicionado();
                    int pesoConexao = 1;
                    if (verticeAnterior != null && pintorConexoes.getVertices().containsKey(verticeAnterior)) {
                        GeoPosition posAnterior = pintorConexoes.getVertices().get(verticeAnterior).posicao;
                        pesoConexao = (int) calcularDistancia(posAnterior.getLatitude(), posAnterior.getLongitude(),
                                geo.getLatitude(), geo.getLongitude());
                    }

                    pintorConexoes.adicionarVertice(nome, geo, tipoVerticeSelecionado);
                    grafoRede.addVerticeAutoConectado(nome, pesoConexao, autoConectarAtivado);

                    if (verticeAnterior != null && autoConectarAtivado) {
                        pintorConexoes.adicionarAresta(verticeAnterior, nome, pesoConexao);
                    }

                    recalcularEnergia();

                } else if (modoAtual == Modo.LIGAR_VERTICES) {
                    String clicado = pintorConexoes.getVerticeProximo(pontoTela, mapViewer, 20);
                    if (clicado != null) {
                        if (verticeOrigemLigacao == null) {
                            verticeOrigemLigacao = clicado;
                            pintorConexoes.setVerticeDestaque(clicado);
                        } else {
                            if (!clicado.equals(verticeOrigemLigacao)) {
                                GeoPosition p1 = pintorConexoes.getVertices().get(verticeOrigemLigacao).posicao;
                                GeoPosition p2 = pintorConexoes.getVertices().get(clicado).posicao;
                                int dist = (int) calcularDistancia(p1.getLatitude(), p1.getLongitude(),
                                        p2.getLatitude(), p2.getLongitude());

                                pintorConexoes.adicionarAresta(verticeOrigemLigacao, clicado, dist);
                                grafoRede.addAresta(verticeOrigemLigacao, clicado, dist);
                            }
                            verticeOrigemLigacao = null;
                            pintorConexoes.setVerticeDestaque(null);
                            recalcularEnergia();
                        }
                    }
                } else if (modoAtual == Modo.REMOVER_VERTICE) {
                    String clicado = pintorConexoes.getVerticeProximo(pontoTela, mapViewer, 20);
                    if (clicado != null) {
                        pintorConexoes.removerVertice(clicado);
                        try {
                            grafoRede.removerVertice(clicado);
                        } catch (Exception ex) {
                        }
                        recalcularEnergia();
                    }
                }  else if (modoAtual == Modo.ALTERNAR_FALHA) {
                    String clicado = pintorConexoes.getVerticeProximo(pontoTela, mapViewer, 20);
                    if (clicado != null) {
                        boolean ativoAtual = grafoRede.posteAtivo(clicado);
                        grafoRede.setPosteAtivo(clicado, !ativoAtual);
                        recalcularEnergia();
                        
                        // >>> ADICIONE ESTA LINHA AQUI <<<
                        gerenciadorTestes.registrarFalhaManualNoMapa(clicado, !ativoAtual);
                    }
                }
            }
        });

        add(mapViewer, BorderLayout.CENTER);
        timerAnimacao = new Timer(16, e -> {
            pintorConexoes.atualizarAnimacao();
            mapViewer.repaint();
        });
        timerAnimacao.start();
    }

    private void limparSelecao(Modo novoModo) {
        this.modoAtual = novoModo;
        this.verticeOrigemLigacao = null;
        pintorConexoes.setVerticeDestaque(null);
        mapViewer.repaint();
    }

    private void atualizarFerramentaVertice(JToggleButton btn, TipoVertice tipo, String nomeTipo) {
        this.tipoVerticeSelecionado = tipo;
        btn.setText("📍 Add: " + nomeTipo + " ▼");
        btn.setSelected(true);
        limparSelecao(Modo.CRIAR_VERTICE);
    }

    // ==========================================
    // SISTEMA DE SALVAMENTO (I/O)
    // ==========================================

    private void salvarRede() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Rede...");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Rede (.txt)", "txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            if (!arquivo.getName().toLowerCase().endsWith(".txt")) {
                arquivo = new File(arquivo.getParentFile(), arquivo.getName() + ".txt");
            }

            try (PrintWriter out = new PrintWriter(new FileWriter(arquivo))) {
                out.println("[VERTICES]");
                for (PintorConexoes.VerticeVis v : pintorConexoes.getVertices().values()) {
                    out.println(v.nome + ";" + v.posicao.getLatitude() + ";" + v.posicao.getLongitude() + ";"
                            + v.tipo.name());
                }
                out.println("[ARESTAS]");
                for (Aresta<String> a : grafoRede.getArestas()) {
                    out.println(a.getU().getNome() + ";" + a.getV().getNome() + ";" + a.getLambda());
                }
                JOptionPane.showMessageDialog(this, "Rede salva com sucesso em:\n" + arquivo.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar arquivo: " + ex.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void carregarRede() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Carregar Rede...");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Rede (.txt)", "txt"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();

            try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
                // Zera o Grafo (Lógica) e o Pintor (Visual)
                grafoRede.limparGrafo();
                pintorConexoes.limparTudo();

                String linha;
                boolean lendoArestas = false;

                while ((linha = br.readLine()) != null) {
                    if (linha.equals("[VERTICES]")) {
                        lendoArestas = false;
                        continue;
                    }
                    if (linha.equals("[ARESTAS]")) {
                        lendoArestas = true;
                        continue;
                    }
                    if (linha.trim().isEmpty())
                        continue;

                    String[] partes = linha.split(";");
                    if (!lendoArestas) {
                        // Linha: Nome;Latitude;Longitude;TIPO
                        String nome = partes[0];
                        double lat = Double.parseDouble(partes[1]);
                        double lon = Double.parseDouble(partes[2]);
                        TipoVertice tipo = TipoVertice.valueOf(partes[3]);

                        pintorConexoes.adicionarVertice(nome, new GeoPosition(lat, lon), tipo);
                        grafoRede.addVertice(nome);
                    } else {
                        // Linha: Origem;Destino;Peso
                        String u = partes[0];
                        String v = partes[1];
                        int peso = Integer.parseInt(partes[2]);

                        pintorConexoes.adicionarAresta(u, v, peso);
                        grafoRede.addAresta(u, v, peso);
                    }
                }

                recalcularEnergia();
                mapViewer.repaint();
                JOptionPane.showMessageDialog(this, "Rede carregada com sucesso!");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao carregar arquivo.\nCertifique-se que o formato está correto.\nErro: "
                                + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // MOTOR DE DISTRIBUIÇÃO DE ENERGIA (Tempo Real)
    // ==========================================
    public void recalcularEnergia() {
        Set<String> ativas = new HashSet<>();
        Queue<String> fila = new LinkedList<>();
        // NOVO: Mapa para registrar a distância de cada nó até a subestação
        Map<String, Integer> distancias = new HashMap<>(); 

        for (Map.Entry<String, PintorConexoes.VerticeVis> entry : pintorConexoes.getVertices().entrySet()) {
            if (entry.getValue().tipo == TipoVertice.SUBESTACAO && grafoRede.posteAtivo(entry.getKey())) {
                ativas.add(entry.getKey());
                fila.add(entry.getKey());
                distancias.put(entry.getKey(), 0); // A distância da fonte de energia é 0
            }
        }

        List<Aresta<String>> arestasGrafo = grafoRede.getArestas();
        Map<String, List<String>> adj = new HashMap<>();
        for (String v : pintorConexoes.getVertices().keySet())
            adj.put(v, new ArrayList<>());
        for (Aresta<String> a : arestasGrafo) {
            adj.get(a.getU().getNome()).add(a.getV().getNome());
            adj.get(a.getV().getNome()).add(a.getU().getNome());
        }

        // BFS - Distribuição de energia e cálculo de distâncias
        while (!fila.isEmpty()) {
            String atual = fila.poll();
            int distAtual = distancias.get(atual); // Distância do nó atual

            for (String vizinho : adj.get(atual)) {
                if (!ativas.contains(vizinho) && grafoRede.posteAtivo(vizinho)) {
                    ativas.add(vizinho);
                    fila.add(vizinho);
                    // O vizinho recebe a distância do nó atual + 1
                    distancias.put(vizinho, distAtual + 1); 
                }
            }
        }

        Set<String> semEnergiaV = new HashSet<>();
        Set<String> inativosManuais = new HashSet<>();
        Set<String> semEnergiaA = new HashSet<>();

        for (String v : pintorConexoes.getVertices().keySet()) {
            if (!grafoRede.posteAtivo(v)) {
                inativosManuais.add(v);
                semEnergiaV.add(v);
            } else if (!ativas.contains(v)) {
                semEnergiaV.add(v);
            }
        }

        for (Aresta<String> a : arestasGrafo) {
            String u = a.getU().getNome();
            String v = a.getV().getNome();
            if (semEnergiaV.contains(u) || semEnergiaV.contains(v)) {
                semEnergiaA.add(u + "-" + v);
                semEnergiaA.add(v + "-" + u);
            }
        }

        // NOVO: Passamos o mapa de distâncias como parâmetro
        pintorConexoes.setEstadoEnergia(semEnergiaV, semEnergiaA, inativosManuais, distancias);
        mapViewer.repaint();
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000;
    }

    private String solicitarVertice(String titulo) {
        Set<String> chaves = pintorConexoes.getVertices().keySet();
        if (chaves.isEmpty())
            return null;
        return (String) JOptionPane.showInputDialog(this, "Selecione o vértice:", titulo, JOptionPane.QUESTION_MESSAGE,
                null, chaves.toArray(), chaves.toArray()[0]);
    }

    private void forcarVisaoSubgrafo() {
        comboVisualizacao.setEnabled(true);
        comboVisualizacao.setSelectedIndex(1);
    }

    private void executarAGM() {
        if (pintorConexoes.getVertices().isEmpty())
            return;
        pintorConexoes.limparDestaquesAlgoritmos();
        pintorConexoes.setCorDestaque(Color.GREEN);

        Grafo<String> agm = grafoRede.AGM(grafoRede);
        List<Aresta<String>> arestasAGM = agm.getArestas();
        for (Aresta<String> a : arestasAGM)
            pintorConexoes.destacarAresta(a.getU().getNome(), a.getV().getNome());
        forcarVisaoSubgrafo();
        JOptionPane.showMessageDialog(this, "AGM gerada! O mapa agora exibe apenas a Árvore Geradora Mínima.");
    }

    private void executarPontes() {
        if (pintorConexoes.getVertices().isEmpty())
            return;
        pintorConexoes.limparDestaquesAlgoritmos();
        pintorConexoes.setCorDestaque(Color.ORANGE);

        List<Aresta<String>> pontes = grafoRede.encontrarPontes();
        if (pontes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma ponte (conexão crítica) encontrada.");
        } else {
            for (Aresta<String> p : pontes)
                pintorConexoes.destacarAresta(p.getU().getNome(), p.getV().getNome());
            forcarVisaoSubgrafo();
            JOptionPane.showMessageDialog(this,
                    pontes.size() + " conexões críticas encontradas e isoladas no subgrafo.");
        }
    }

    private void executarBusca(String tipo) {
        String inicio = solicitarVertice("Escolha o nó de início da " + tipo);
        if (inicio == null)
            return;

        pintorConexoes.limparDestaquesAlgoritmos();

        List<String> ordem = tipo.equals("DFS") ? grafoRede.dfs(inicio) : grafoRede.bfs(inicio);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ordem.size(); i++) {
            sb.append(i + 1).append(" ➔ ").append(ordem.get(i)).append("\n");
            pintorConexoes.definirOrdemVisita(ordem.get(i), i + 1);
        }
        forcarVisaoSubgrafo();

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(300, 250));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("<html><b>Ordem de visitação " + tipo + ":</b><br>Total de nós alcançados: " + ordem.size()
                + "</html>"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(new JLabel("<html><i>O subgrafo percorrido foi isolado no mapa.</i></html>"), BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Resultado da Busca " + tipo, JOptionPane.INFORMATION_MESSAGE);
        mapViewer.repaint();
    }

    private void executarFluxoMaximo() {
        String origem = solicitarVertice("Origem do Fluxo (Fonte)");
        if (origem == null)
            return;
        String destino = solicitarVertice("Destino do Fluxo (Sumidouro)");
        if (destino == null)
            return;

        int fluxo = grafoRede.fluxoMaximo(origem, destino);
        JOptionPane.showMessageDialog(this, "A Capacidade Máxima de Distribuição entre " + origem + " e " + destino
                + " é: " + fluxo + " unidades.");
    }

    public Grafo<String> getGrafoRede() {
        return grafoRede;
    }

    public PintorConexoes getPintorConexoes() {
        return pintorConexoes;
    }

    public JXMapViewer getMapViewer() {
        return mapViewer;
    }
}
