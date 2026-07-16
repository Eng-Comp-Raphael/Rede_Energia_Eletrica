package visualizacao;

import estruturaGrafo.Grafo;
import estruturaGrafo.Aresta;
import visualizacao.PintorConexoes.TipoVertice;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class GerenciadorTestes {

    private VisualizadorRede interfacePrincipal;
    private Grafo<String> grafo;
    private PintorConexoes pintor;

    // --- Componentes do Teste Automático ---
    private JDialog janelaEtapas;
    private JTextArea textoEtapa;
    private JButton btnContinue;
    private JButton btnVoltar;
    private int etapaAtual = 0;

    // --- Componentes do Teste Personalizado ---
    private JDialog janelaPersonalizada;
    private JTextArea logPersonalizado;
    private JComboBox<String> seletorComponente;

    public GerenciadorTestes(VisualizadorRede interfacePrincipal) {
        this.interfacePrincipal = interfacePrincipal;
        this.grafo = interfacePrincipal.getGrafoRede();
        this.pintor = interfacePrincipal.getPintorConexoes();
    }

    // ==========================================
    // TRUQUE: INTERCEPTADOR DE CONSOLE
    // ==========================================
    private String capturarSaidaConsole(Runnable acao) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream consoleAntigo = System.out;
        
        System.setOut(ps);
        acao.run();
        System.out.flush();
        System.setOut(consoleAntigo);
        
        return baos.toString();
    }

    // ==========================================
    // 1. TESTE AUTOMÁTICO (Máquina de Estados)
    // ==========================================

    public void iniciarTesteAutomatico() {
        if (janelaEtapas == null) {
            criarJanelaEtapas();
        }
        posicionarJanela(janelaEtapas);
        janelaEtapas.setVisible(true);
        resetarTeste();
    }

    private void criarJanelaEtapas() {
        janelaEtapas = new JDialog(interfacePrincipal, "Simulação Automática", false);
        Dimension tamanhoTela = Toolkit.getDefaultToolkit().getScreenSize();
        janelaEtapas.setSize(400, tamanhoTela.height / 2); 
        janelaEtapas.setLayout(new BorderLayout());
        janelaEtapas.setResizable(true);
        janelaEtapas.setAlwaysOnTop(true);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        painelBotoes.setBackground(Color.DARK_GRAY);

        JButton btnReset = new JButton("🔄 Resetar");
        btnVoltar = new JButton("⬅️ Voltar");
        btnContinue = new JButton("Continuar ➡️");

        btnReset.setFocusable(false);
        btnVoltar.setFocusable(false);
        btnContinue.setFocusable(false);

        painelBotoes.add(btnReset);
        painelBotoes.add(btnVoltar);
        painelBotoes.add(btnContinue);

        textoEtapa = new JTextArea();
        textoEtapa.setEditable(false);
        textoEtapa.setWrapStyleWord(true);
        textoEtapa.setLineWrap(true);
        textoEtapa.setFont(new Font("SansSerif", Font.BOLD, 13));
        textoEtapa.setMargin(new Insets(15, 15, 15, 15));
        textoEtapa.setBackground(new Color(240, 240, 245));

        janelaEtapas.add(painelBotoes, BorderLayout.NORTH);
        janelaEtapas.add(new JScrollPane(textoEtapa), BorderLayout.CENTER);

        btnContinue.addActionListener(e -> {
            if (etapaAtual < 4) {
                etapaAtual++;
                renderizarEstadoAtual();
            }
        });
        btnVoltar.addActionListener(e -> {
            if (etapaAtual > 1) {
                etapaAtual--;
                renderizarEstadoAtual();
            }
        });
        btnReset.addActionListener(e -> resetarTeste());
    }

    private void resetarTeste() {
        etapaAtual = 1;
        renderizarEstadoAtual();
    }

    private void renderizarEstadoAtual() {
        pintor.limparTudo();
        grafo.limparGrafo();

        if (etapaAtual >= 1) {
            ModeloTesteAutomatico.construirRedeBase(grafo, pintor, interfacePrincipal);
        }

        if (etapaAtual == 2) {
            // Derrubando AS DUAS subestações para simular falha total (raios vermelhos)
            grafo.setPosteAtivo("Sub_Centro", false);
            grafo.setPosteAtivo("Sub_Coplan", false);
            textoEtapa.setText("ETAPA 2: Queda da Subestação!\n\nA fonte primária parou. Todo o sistema deve estar com raios vermelhos e sem energia.");
        } else if (etapaAtual == 3) {
            // Religando as subestações
            grafo.setPosteAtivo("Sub_Centro", true);
            grafo.setPosteAtivo("Sub_Coplan", true);
            textoEtapa.setText("ETAPA 3: Sistema Normalizado.\n\nA subestação foi religada e a energia voltou a fluir normalmente.");
        } else if (etapaAtual == 4) {
            // Interrompendo um nó crítico da nova rede
            grafo.setPosteAtivo("P_Vargas1", false);
            
            String outputCaminho = capturarSaidaConsole(() -> grafo.printCaminhoAlternativo("Sub_Centro", "Casa_Primavera"));
            String outputPrioridades = capturarSaidaConsole(() -> grafo.printPontosPrioritarios());
            
            textoEtapa.setText("ETAPA 4: Conexão interrompida no setor P_Vargas1.\nA energia encontrou o caminho alternativo.\n\n"
                    + "=== RELATÓRIO DO SISTEMA ===\n\n" 
                    + outputCaminho + "\n" + outputPrioridades);
            textoEtapa.setCaretPosition(0);
        } else {
            textoEtapa.setText("ETAPA 1: Rede base construída com sucesso.\n\nToda a rede está energizada (bolinhas amarelas fluidas).");
        }

        interfacePrincipal.recalcularEnergia();
        btnVoltar.setEnabled(etapaAtual > 1);
        btnContinue.setEnabled(etapaAtual < 4);
    }

    // ==========================================
    // 2. TESTE PERSONALIZADO (Painel Interativo)
    // ==========================================

    public void executarTestePersonalizado() {
        if (pintor.getVertices().isEmpty()) {
            JOptionPane.showMessageDialog(interfacePrincipal, "A rede está vazia! Desenhe uma rede ou carregue um arquivo primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (janelaPersonalizada == null) {
            criarJanelaPersonalizada();
        }
        
        logPersonalizado.setText("Pronto para simular a rede atual.\nSelecione uma ação acima ou clique em um poste no mapa com a ferramenta de falha.");
        atualizarComponentesDisponiveis();
        posicionarJanela(janelaPersonalizada);
        janelaPersonalizada.setVisible(true);
    }

    private void criarJanelaPersonalizada() {
        janelaPersonalizada = new JDialog(interfacePrincipal, "Painel de Testes Livres", false);
        Dimension tamanhoTela = Toolkit.getDefaultToolkit().getScreenSize();
        janelaPersonalizada.setSize(450, tamanhoTela.height / 2);
        janelaPersonalizada.setLayout(new BorderLayout());
        janelaPersonalizada.setAlwaysOnTop(true);

        // Painel Superior com fundo ESCURO (padrão mantido)
        JPanel painelAcoes = new JPanel(new GridLayout(7, 1, 5, 5));
        painelAcoes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painelAcoes.setBackground(Color.DARK_GRAY);

        JButton btnEstresse = new JButton("💥 Derrubar Nó Aleatório");
        seletorComponente = new JComboBox<>();
        JButton btnDerrubarSelecionado = new JButton("Derrubar componente selecionado");
        JButton btnReligarSelecionado = new JButton("Religar componente selecionado");
        JButton btnCaminhoAuto = new JButton("🛣️ Auto: Subestação ➔ Mais Distante"); // NOVO BOTÃO
        JButton btnCaminho = new JButton("🛣️ Rota Específica (Origem ➔ Destino)");
        JButton btnPrioridade = new JButton("⚠️ Analisar Pontos Prioritários");

        painelAcoes.add(btnEstresse);
        painelAcoes.add(seletorComponente);
        painelAcoes.add(btnDerrubarSelecionado);
        painelAcoes.add(btnReligarSelecionado);
        painelAcoes.add(btnCaminhoAuto);
        painelAcoes.add(btnCaminho);
        painelAcoes.add(btnPrioridade);

        // Área de Log agora usa o estilo CLARO (igual ao teste automático)
        logPersonalizado = new JTextArea();
        logPersonalizado.setEditable(false);
        logPersonalizado.setFont(new Font("SansSerif", Font.BOLD, 13));
        logPersonalizado.setLineWrap(true);
        logPersonalizado.setWrapStyleWord(true);
        logPersonalizado.setBackground(new Color(240, 240, 245));
        logPersonalizado.setForeground(Color.BLACK);
        logPersonalizado.setMargin(new Insets(15, 15, 15, 15));

        janelaPersonalizada.add(painelAcoes, BorderLayout.NORTH);
        janelaPersonalizada.add(new JScrollPane(logPersonalizado), BorderLayout.CENTER);

        // --- Eventos dos Botões ---

        btnEstresse.addActionListener(e -> {
            List<String> chaves = new ArrayList<>(pintor.getVertices().keySet());
            String noSorteado = chaves.get(new Random().nextInt(chaves.size()));
            boolean estadoAtual = grafo.posteAtivo(noSorteado);
            
            grafo.setPosteAtivo(noSorteado, !estadoAtual);
            interfacePrincipal.recalcularEnergia();
            
            String acao = !estadoAtual ? "RELIGADO" : "DERRUBADO";
            logPersonalizado.append("\n\n[ESTRESSE] Componente '" + noSorteado + "' foi " + acao + "!");
            if (estadoAtual) {
                logPersonalizado.append("\n\n" + gerarRelatorioDeFalha());
            }
            logPersonalizado.setCaretPosition(logPersonalizado.getDocument().getLength());
        });

        btnDerrubarSelecionado.addActionListener(e -> alterarFalhaDoComponenteSelecionado(false));
        btnReligarSelecionado.addActionListener(e -> alterarFalhaDoComponenteSelecionado(true));

        // NOVO: Cálculo Automático (Subestação -> Fim da Linha)
        // NOVO: Cálculo Automático (Todas as Subestações -> Poste Mais Distante)
        btnCaminhoAuto.addActionListener(e -> {
            List<String> subestacoes = new ArrayList<>();
            for (Map.Entry<String, PintorConexoes.VerticeVis> entry : pintor.getVertices().entrySet()) {
                if (entry.getValue().tipo == TipoVertice.SUBESTACAO && grafo.posteAtivo(entry.getKey())) {
                    subestacoes.add(entry.getKey());
                }
            }

            if (subestacoes.isEmpty()) {
                logPersonalizado.append("\n\n[ERRO] Nenhuma subestação ativa foi encontrada no mapa!");
                return;
            }

            logPersonalizado.append("\n\n[ROTA AUTOMÁTICA] Traçando caminhos das subestações aos pontos de distribuição (Postes) mais distantes:");
            
            for (String sub : subestacoes) {
                List<String> ordemBfs = grafo.bfs(sub);
                
                if (ordemBfs != null && !ordemBfs.isEmpty()) {
                    String maisDistante = null;
                    
                    // Varre a lista da BFS de trás pra frente
                    for (int i = ordemBfs.size() - 1; i >= 0; i--) {
                        String noAtual = ordemBfs.get(i);
                        PintorConexoes.VerticeVis vis = pintor.getVertices().get(noAtual);
                        
                        // Seleciona o primeiro nó encontrado que NÃO SEJA UMA CASA
                        if (vis != null && vis.tipo != TipoVertice.CASA) {
                            maisDistante = noAtual;
                            break;
                        }
                    }
                    
                    if (maisDistante != null && !maisDistante.equals(sub)) {
                        // Variável final para usar dentro do lambda do capturarSaidaConsole
                        final String destinoCritico = maisDistante; 
                        String resultadoConsole = capturarSaidaConsole(() -> grafo.printCaminhoAlternativo(sub, destinoCritico));
                        
                        logPersonalizado.append("\n\nOrigem: " + sub + " ➔ Destino Crítico: " + destinoCritico + "\n" + resultadoConsole);
                    } else {
                        logPersonalizado.append("\n\nA Subestação " + sub + " está isolada ou só tem ramificações diretas para casas.");
                    }
                }
            }
            logPersonalizado.setCaretPosition(logPersonalizado.getDocument().getLength());
        });

        btnCaminho.addActionListener(e -> {
            String origem = solicitarVertice("Origem da Rota:");
            if (origem == null) return;
            String destino = solicitarVertice("Destino da Rota:");
            if (destino == null) return;

            String resultadoConsole = capturarSaidaConsole(() -> grafo.printCaminhoAlternativo(origem, destino));
            logPersonalizado.append("\n\n[BUSCA ROTA] " + origem + " -> " + destino + ":\n" + resultadoConsole);
            logPersonalizado.setCaretPosition(logPersonalizado.getDocument().getLength());
        });

        btnPrioridade.addActionListener(e -> {
            String resultadoConsole = capturarSaidaConsole(() -> grafo.printPontosPrioritarios());
            logPersonalizado.append("\n\n[ANÁLISE PRIORITÁRIA]:\n" + resultadoConsole);
            logPersonalizado.setCaretPosition(logPersonalizado.getDocument().getLength());
        });
    }

    // ==========================================
    // 3. COMUNICAÇÃO COM O MAPA (Cliques Manuais)
    // ==========================================
    
    /**
     * Chamado pelo VisualizadorRede quando o usuário clica com a ferramenta de Simular Falha
     */
    private void alterarFalhaDoComponenteSelecionado(boolean religado) {
        String vertice = (String) seletorComponente.getSelectedItem();
        if (vertice == null) {
            return;
        }

        grafo.setPosteAtivo(vertice, religado);
        interfacePrincipal.recalcularEnergia();
        registrarFalhaManualNoMapa(vertice, religado);
    }

    private void atualizarComponentesDisponiveis() {
        String selecionado = (String) seletorComponente.getSelectedItem();
        seletorComponente.removeAllItems();
        for (String vertice : pintor.getVertices().keySet()) {
            seletorComponente.addItem(vertice);
        }
        seletorComponente.setSelectedItem(selecionado);
    }

    public void registrarFalhaManualNoMapa(String vertice, boolean religado) {
        if (janelaPersonalizada == null || !janelaPersonalizada.isVisible()) {
            executarTestePersonalizado(); // Garante que a janela abra se o usuário clicou direto no mapa
        }
        
        String acao = religado ? "RELIGADO" : "DERRUBADO";
        logPersonalizado.append("\n\n----------------------------------");
        logPersonalizado.append("\n[MAPA] Componente '" + vertice + "' foi " + acao + "!");
        
        if (!religado) {
            logPersonalizado.append("\n\n" + gerarRelatorioDeFalha());
        }
        
        logPersonalizado.setCaretPosition(logPersonalizado.getDocument().getLength());
    }

    /**
     * Relatório enxuto: relaciona somente itens diretamente atingidos por uma falha.
     * Assim, a análise não repete toda a rede nem lista prioridades sem relação com o
     * componente derrubado.
     */
    private String gerarRelatorioDeFalha() {
        Set<String> componentesEmFalha = new HashSet<>();
        for (String id : pintor.getVertices().keySet()) {
            if (!grafo.posteAtivo(id)) {
                componentesEmFalha.add(id);
            }
        }

        if (componentesEmFalha.isEmpty()) {
            return "[RELATÓRIO DE IMPACTO]\nNenhum componente está em falha.";
        }

        List<Aresta<String>> cabosAfetados = new ArrayList<>();
        Map<String, Integer> grau = new HashMap<>();
        for (String id : pintor.getVertices().keySet()) {
            grau.put(id, 0);
        }
        for (Aresta<String> cabo : grafo.getArestas()) {
            String origem = cabo.getU().getNome();
            String destino = cabo.getV().getNome();
            if (cabo.getAtivo()) {
                grau.merge(origem, 1, Integer::sum);
                grau.merge(destino, 1, Integer::sum);
            }
            if (!cabo.getAtivo() || componentesEmFalha.contains(origem) || componentesEmFalha.contains(destino)) {
                cabosAfetados.add(cabo);
            }
        }

        StringBuilder relatorio = new StringBuilder("[RELATÓRIO DE IMPACTO]\n");
        List<String> pontosCriticos = new ArrayList<>();
        for (String id : componentesEmFalha) {
            if (ehPontoCritico(id)) {
                pontosCriticos.add(id);
            }
        }
        pontosCriticos.sort(String::compareTo);
        if (!pontosCriticos.isEmpty()) {
            relatorio.append("\nPontos críticos em falha:\n");
            for (String id : pontosCriticos) {
                relatorio.append("• ").append(id).append('\n');
            }
        }

        List<String> fragilizados = new ArrayList<>(componentesEmFalha);
        fragilizados.sort(Comparator.comparingInt(id -> grau.getOrDefault(id, 0)));
        relatorio.append("\nPontos mais frágeis em falha:\n");
        for (String id : fragilizados) {
            relatorio.append("• ").append(id).append(" (grau ")
                    .append(grau.getOrDefault(id, 0)).append(")\n");
        }

        cabosAfetados.sort((a, b) -> Integer.compare(b.getLambda(), a.getLambda()));
        if (!cabosAfetados.isEmpty()) {
            relatorio.append("\nCabos mais longos afetados:\n");
            for (Aresta<String> cabo : cabosAfetados) {
                relatorio.append("• [").append(cabo.getU().getNome()).append("] - [")
                        .append(cabo.getV().getNome()).append("] (")
                        .append(cabo.getLambda()).append("m)\n");
            }
        }
        return relatorio.toString();
    }

    /** Verifica se retirar o vértice aumenta o número de componentes da rede. */
    private boolean ehPontoCritico(String candidato) {
        return contarComponentes(null) < contarComponentes(candidato);
    }

    private int contarComponentes(String verticeIgnorado) {
        Map<String, List<String>> adjacencias = new HashMap<>();
        for (String id : pintor.getVertices().keySet()) {
            if (!id.equals(verticeIgnorado)) {
                adjacencias.put(id, new ArrayList<>());
            }
        }
        for (Aresta<String> cabo : grafo.getArestas()) {
            if (!cabo.getAtivo()) {
                continue;
            }
            String origem = cabo.getU().getNome();
            String destino = cabo.getV().getNome();
            if (adjacencias.containsKey(origem) && adjacencias.containsKey(destino)) {
                adjacencias.get(origem).add(destino);
                adjacencias.get(destino).add(origem);
            }
        }

        Set<String> visitados = new HashSet<>();
        int componentes = 0;
        for (String origem : adjacencias.keySet()) {
            if (!visitados.add(origem)) {
                continue;
            }
            componentes++;
            List<String> pendentes = new ArrayList<>();
            pendentes.add(origem);
            for (int i = 0; i < pendentes.size(); i++) {
                for (String vizinho : adjacencias.get(pendentes.get(i))) {
                    if (visitados.add(vizinho)) {
                        pendentes.add(vizinho);
                    }
                }
            }
        }
        return componentes;
    }

    // ==========================================
    // 4. MÉTODOS AUXILIARES
    // ==========================================

    private void posicionarJanela(JDialog janela) {
        //Dimension tamanhoTela = Toolkit.getDefaultToolkit().getScreenSize();
        Point loc = interfacePrincipal.getLocationOnScreen();
        
        int x = loc.x + 20;
        int y = Math.max(0, loc.y + interfacePrincipal.getHeight() - janela.getHeight() - 40);
        
        janela.setLocation(x, y);
    }
    
    /*private void adicionarNoSimulado(String nome, double lat, double lon, TipoVertice tipo) {
        pintor.adicionarVertice(nome, new GeoPosition(lat, lon), tipo);
        grafo.addVertice(nome);
    }

    private void ligarNoSimulado(String u, String v, int peso) {
        pintor.adicionarAresta(u, v, peso);
        grafo.addAresta(u, v, peso);
    }*/

    private String solicitarVertice(String mensagem) {
        Object[] chaves = pintor.getVertices().keySet().toArray();
        return (String) JOptionPane.showInputDialog(interfacePrincipal, mensagem, "Entrada", JOptionPane.QUESTION_MESSAGE, null, chaves, chaves[0]);
    }
}
