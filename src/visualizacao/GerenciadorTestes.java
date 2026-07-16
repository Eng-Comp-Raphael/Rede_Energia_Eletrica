package visualizacao;

import estruturaGrafo.Grafo;
import estruturaGrafo.Aresta;
import visualizacao.PintorConexoes.TipoVertice;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

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

    // --- Componentes do Relatório (janela própria, separada do Painel de Testes) ---
    private JDialog janelaRelatorio;
    private JTextArea areaRelatorio;

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
        janelaPersonalizada.setSize(400, Math.min(600, tamanhoTela.height - 120));
        janelaPersonalizada.setMinimumSize(new Dimension(360, 420));
        janelaPersonalizada.setLayout(new BorderLayout());
        janelaPersonalizada.setAlwaysOnTop(true);

        // Painel Superior com fundo ESCURO (padrão mantido), agora organizado em
        // seções temáticas para reduzir a altura ocupada e facilitar a leitura.
        JPanel painelAcoes = new JPanel();
        painelAcoes.setLayout(new BoxLayout(painelAcoes, BoxLayout.Y_AXIS));
        painelAcoes.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        painelAcoes.setBackground(Color.DARK_GRAY);

        seletorComponente = new JComboBox<>();
        JButton btnEstresse = new JButton("💥 Derrubar Nó Aleatório");
        JButton btnDerrubarSelecionado = new JButton("⬇ Derrubar");
        JButton btnReligarSelecionado = new JButton("⬆ Religar");
        JButton btnCaminhoAuto = new JButton("🛣️ Auto: Subestação ➔ Mais Distante");
        JButton btnCaminho = new JButton("🛣️ Rota Específica (Origem ➔ Destino)");
        JButton btnPrioridade = new JButton("⚠️ Analisar Pontos Prioritários");

        JPanel secaoComponente = criarSecao("Componente Selecionado");
        adicionarNaSecao(secaoComponente, seletorComponente);
        secaoComponente.add(Box.createVerticalStrut(6));
        JPanel linhaDerrubarReligar = new JPanel(new GridLayout(1, 2, 6, 0));
        linhaDerrubarReligar.setOpaque(false);
        linhaDerrubarReligar.add(btnDerrubarSelecionado);
        linhaDerrubarReligar.add(btnReligarSelecionado);
        adicionarNaSecao(secaoComponente, linhaDerrubarReligar);

        JPanel secaoTestes = criarSecao("Testes Rápidos");
        adicionarNaSecao(secaoTestes, btnEstresse);

        JPanel secaoRotas = criarSecao("Análise de Rotas e Prioridades");
        adicionarNaSecao(secaoRotas, btnCaminhoAuto);
        secaoRotas.add(Box.createVerticalStrut(6));
        adicionarNaSecao(secaoRotas, btnCaminho);
        secaoRotas.add(Box.createVerticalStrut(6));
        adicionarNaSecao(secaoRotas, btnPrioridade);

        painelAcoes.add(secaoComponente);
        painelAcoes.add(Box.createVerticalStrut(8));
        painelAcoes.add(secaoTestes);
        painelAcoes.add(Box.createVerticalStrut(8));
        painelAcoes.add(secaoRotas);

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
                notificarFalha(noSorteado);
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
            logPersonalizado.append("\n\n[PONTOS PRIORITÁRIOS PARA MANUTENÇÃO]:\n" + gerarSecoesImpacto());
            logPersonalizado.setCaretPosition(logPersonalizado.getDocument().getLength());
        });
    }

    /** Cria um bloco temático com título, usado para agrupar botões relacionados no painel. */
    private JPanel criarSecao(String titulo) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(new Color(60, 60, 60));
        painel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(90, 90, 90)),
                titulo, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 11), new Color(220, 220, 220)));
        painel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return painel;
    }

    /** Adiciona um componente a uma seção fazendo-o ocupar toda a largura disponível. */
    private void adicionarNaSecao(JPanel secao, JComponent componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        componente.setMaximumSize(new Dimension(Integer.MAX_VALUE, componente.getPreferredSize().height));
        secao.add(componente);
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
        // O painel não abre mais sozinho ao simular: só registra no log se ele já
        // estiver aberto (acesse-o pelo botão "🛠️ Painel de Testes" quando quiser).
        if (janelaPersonalizada != null && janelaPersonalizada.isVisible()) {
            String acao = religado ? "RELIGADO" : "DERRUBADO";
            logPersonalizado.append("\n\n----------------------------------");
            logPersonalizado.append("\n[MAPA] Componente '" + vertice + "' foi " + acao + "!");
            if (!religado) {
                logPersonalizado.append("\n\n" + gerarRelatorioDeFalha());
            }
            logPersonalizado.setCaretPosition(logPersonalizado.getDocument().getLength());
        }

        if (!religado) {
            notificarFalha(vertice);
        }
    }

    // ==========================================
    // 4. ALERTAS DE FALHA (SOM + AVISO DE FALHA CRÍTICA)
    // ==========================================

    /** Só toca alerta sonoro (e acende o ícone lateral) quando a falha é crítica. */
    private void notificarFalha(String vertice) {
        if (ehFalhaCritica(vertice)) {
            Toolkit.getDefaultToolkit().beep();
            sinalizarFalhaCritica();
        }
    }

    /**
     * Uma falha é considerada crítica quando, além do próprio componente, outros pontos da
     * rede perdem energia por consequência (efeito cascata) - ou seja, quando o poste
     * derrubado realmente fez outras conexões caírem junto.
     */
    private boolean ehFalhaCritica(String vertice) {
        int impactoIndireto = 0;
        for (String id : pintor.getVerticesSemEnergia()) {
            if (!id.equals(vertice)) {
                impactoIndireto++;
            }
        }
        return impactoIndireto > 0;
    }

    /**
     * Se o relatório já estiver aberto, só atualiza o conteúdo dele (o som já avisa a falha).
     * Caso contrário, acende o ícone piscante na lateral da janela principal; clicar nele abre
     * o relatório completo.
     */
    private void sinalizarFalhaCritica() {
        if (janelaRelatorio != null && janelaRelatorio.isVisible()) {
            atualizarConteudoRelatorio();
        } else {
            interfacePrincipal.ativarAlertaCritico(this::abrirRelatorioCompleto);
        }
    }

    // ==========================================
    // 5. RELATÓRIOS (IMPACTO DE FALHA E RELATÓRIO COMPLETO)
    // ==========================================

    /**
     * Relatório enxuto: relaciona somente itens diretamente atingidos por uma falha.
     * Assim, a análise não repete toda a rede nem lista prioridades sem relação com o
     * componente derrubado.
     */
    private String gerarRelatorioDeFalha() {
        return "=== RELATÓRIO DE IMPACTO ===\n\n" + gerarSecoesImpacto();
    }

    /** Monta o relatório de impacto: postes em falha por região, gravidade de cada um e prioridade de reparo. */
    private String gerarSecoesImpacto() {
        List<String> postesEmFalha = new ArrayList<>();
        for (String id : pintor.getVertices().keySet()) {
            if (!grafo.posteAtivo(id)) {
                postesEmFalha.add(id);
            }
        }

        if (postesEmFalha.isEmpty()) {
            return "Nenhum poste está em falha no momento.\n";
        }

        Map<String, String> regiaoDoPoste = calcularRegiaoPorSubestacaoMaisProxima();
        Map<String, List<String>> falhasPorRegiao = new TreeMap<>();
        for (String id : postesEmFalha) {
            String regiao = regiaoDoPoste.getOrDefault(id, "Sem região definida");
            falhasPorRegiao.computeIfAbsent(regiao, r -> new ArrayList<>()).add(id);
        }
        for (List<String> postesDaRegiao : falhasPorRegiao.values()) {
            postesDaRegiao.sort(String::compareTo);
        }

        // Ordem final usada no detalhamento e na prioridade: agrupado por região,
        // e dentro de cada região em ordem alfabética.
        List<String> postesEmFalhaOrdenados = new ArrayList<>();
        for (List<String> postesDaRegiao : falhasPorRegiao.values()) {
            postesEmFalhaOrdenados.addAll(postesDaRegiao);
        }

        StringBuilder relatorio = new StringBuilder();
        relatorio.append("1. Falhas por região:\n");
        for (Map.Entry<String, List<String>> entrada : falhasPorRegiao.entrySet()) {
            relatorio.append("\n   Região: ").append(entrada.getKey()).append('\n');
            for (String id : entrada.getValue()) {
                relatorio.append("     • ").append(id).append('\n');
            }
        }

        Map<String, Integer> grauDaFalha = new HashMap<>();
        for (String id : postesEmFalhaOrdenados) {
            grauDaFalha.put(id, calcularGrauDaFalha(id));
        }

        relatorio.append("\n2. Detalhamento:\n");
        for (String id : postesEmFalhaOrdenados) {
            List<Aresta<String>> cabosProximos = new ArrayList<>();
            for (Aresta<String> cabo : grafo.getArestas()) {
                if (cabo.getU().getNome().equals(id) || cabo.getV().getNome().equals(id)) {
                    cabosProximos.add(cabo);
                }
            }

            int quedasJunto = grauDaFalha.get(id);
            relatorio.append("\n   ⚡ ").append(id).append('\n');
            relatorio.append("      Grau da falha: ").append(classificarGrauDaFalha(quedasJunto))
                    .append(" (").append(quedasJunto)
                    .append(quedasJunto == 1 ? " poste cai junto)\n" : " postes caem juntos)\n");

            if (cabosProximos.isEmpty()) {
                relatorio.append("      Fios próximos: nenhum (poste isolado)\n");
            } else {
                relatorio.append("      Fios próximos:\n");
                for (Aresta<String> cabo : cabosProximos) {
                    relatorio.append("        - [").append(cabo.getU().getNome()).append("] - [")
                            .append(cabo.getV().getNome()).append("] (").append(cabo.getLambda()).append("m)\n");
                }
            }

            List<String> criticosConectados = new ArrayList<>();
            for (Aresta<String> cabo : cabosProximos) {
                String vizinho = cabo.getU().getNome().equals(id) ? cabo.getV().getNome() : cabo.getU().getNome();
                if (pintor.getPontosCriticos().contains(vizinho)) {
                    criticosConectados.add(vizinho);
                }
            }
            if (!criticosConectados.isEmpty()) {
                criticosConectados.sort(String::compareTo);
                relatorio.append("      ⚠ Pontos críticos conectados: ")
                        .append(String.join(", ", criticosConectados)).append('\n');
            }
        }

        if (postesEmFalhaOrdenados.size() > 1) {
            List<String> prioridade = new ArrayList<>(postesEmFalhaOrdenados);
            prioridade.sort((a, b) -> {
                int cmp = Integer.compare(grauDaFalha.get(b), grauDaFalha.get(a));
                return cmp != 0 ? cmp : a.compareTo(b);
            });
            relatorio.append("\n3. Prioridade de reparo sugerida:\n");
            for (int i = 0; i < prioridade.size(); i++) {
                String id = prioridade.get(i);
                relatorio.append("   ").append(i + 1).append("º ").append(id)
                        .append(" (").append(grauDaFalha.get(id)).append(" postes afetados)\n");
            }
        }

        return relatorio.toString();
    }

    private String classificarGrauDaFalha(int postesQueCaemJunto) {
        if (postesQueCaemJunto == 0) {
            return "BAIXO";
        }
        if (postesQueCaemJunto <= 3) {
            return "MODERADO";
        }
        return "ALTO";
    }

    /**
     * Simula "e se só ESTE poste tivesse caído": reativa temporariamente os demais postes
     * que estão fora do ar, refaz a energização a partir das subestações ativas e conta
     * quantos outros postes ficam sem energia por causa exclusivamente deste poste. Assim
     * dá pra saber o grau de cada falha mesmo quando várias acontecem ao mesmo tempo.
     */
    private int calcularGrauDaFalha(String posteFalho) {
        List<String> outrosInativos = new ArrayList<>();
        for (String id : pintor.getVertices().keySet()) {
            if (!id.equals(posteFalho) && !grafo.posteAtivo(id)) {
                outrosInativos.add(id);
            }
        }
        for (String id : outrosInativos) {
            grafo.setPosteAtivo(id, true);
        }

        Map<String, List<String>> adjacencia = new HashMap<>();
        for (String id : pintor.getVertices().keySet()) {
            adjacencia.put(id, new ArrayList<>());
        }
        for (Aresta<String> cabo : grafo.getArestas()) {
            if (!cabo.getAtivo()) {
                continue;
            }
            String origem = cabo.getU().getNome();
            String destino = cabo.getV().getNome();
            adjacencia.get(origem).add(destino);
            adjacencia.get(destino).add(origem);
        }

        Set<String> alcancaveis = new HashSet<>();
        Queue<String> fila = new LinkedList<>();
        for (Map.Entry<String, PintorConexoes.VerticeVis> entry : pintor.getVertices().entrySet()) {
            if (entry.getValue().tipo == TipoVertice.SUBESTACAO && grafo.posteAtivo(entry.getKey())) {
                alcancaveis.add(entry.getKey());
                fila.add(entry.getKey());
            }
        }
        while (!fila.isEmpty()) {
            String atual = fila.poll();
            for (String vizinho : adjacencia.get(atual)) {
                if (!alcancaveis.contains(vizinho) && grafo.posteAtivo(vizinho)) {
                    alcancaveis.add(vizinho);
                    fila.add(vizinho);
                }
            }
        }

        int quedasPorCausaDele = 0;
        for (String id : pintor.getVertices().keySet()) {
            if (id.equals(posteFalho)) {
                continue;
            }
            if (!grafo.posteAtivo(id) || !alcancaveis.contains(id)) {
                quedasPorCausaDele++;
            }
        }

        for (String id : outrosInativos) {
            grafo.setPosteAtivo(id, false);
        }

        return quedasPorCausaDele;
    }

    /**
     * Agrupa cada poste da rede pela subestação mais próxima (menor número de saltos na
     * topologia, ignorando o estado ativo/inativo). É essa subestação que representa a
     * "região/bairro" do poste no relatório - o mesmo agrupamento natural de uma rede de
     * distribuição real, onde cada bairro é alimentado por uma subestação específica.
     */
    private Map<String, String> calcularRegiaoPorSubestacaoMaisProxima() {
        Map<String, List<String>> adjacencia = new HashMap<>();
        for (String id : pintor.getVertices().keySet()) {
            adjacencia.put(id, new ArrayList<>());
        }
        for (Aresta<String> cabo : grafo.getArestas()) {
            String origem = cabo.getU().getNome();
            String destino = cabo.getV().getNome();
            adjacencia.get(origem).add(destino);
            adjacencia.get(destino).add(origem);
        }

        List<String> subestacoes = new ArrayList<>();
        for (Map.Entry<String, PintorConexoes.VerticeVis> entry : pintor.getVertices().entrySet()) {
            if (entry.getValue().tipo == TipoVertice.SUBESTACAO) {
                subestacoes.add(entry.getKey());
            }
        }
        subestacoes.sort(String::compareTo);

        Map<String, String> regiaoDoPoste = new HashMap<>();
        Map<String, Integer> menorDistancia = new HashMap<>();

        for (String subestacao : subestacoes) {
            Map<String, Integer> distancias = new HashMap<>();
            Queue<String> fila = new LinkedList<>();
            distancias.put(subestacao, 0);
            fila.add(subestacao);

            while (!fila.isEmpty()) {
                String atual = fila.poll();
                for (String vizinho : adjacencia.get(atual)) {
                    if (!distancias.containsKey(vizinho)) {
                        distancias.put(vizinho, distancias.get(atual) + 1);
                        fila.add(vizinho);
                    }
                }
            }

            for (Map.Entry<String, Integer> entrada : distancias.entrySet()) {
                String poste = entrada.getKey();
                int distancia = entrada.getValue();
                if (!menorDistancia.containsKey(poste) || distancia < menorDistancia.get(poste)) {
                    menorDistancia.put(poste, distancia);
                    regiaoDoPoste.put(poste, subestacao);
                }
            }
        }

        return regiaoDoPoste;
    }

    /** Relatório completo: resumo geral da rede + impacto atual + prioridades gerais de manutenção. */
    private String gerarRelatorioCompleto() {
        StringBuilder sb = new StringBuilder();
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        sb.append("============================================\n");
        sb.append(" RELATÓRIO COMPLETO DA REDE DE ENERGIA\n");
        sb.append(" Gerado em: ").append(timestamp).append('\n');
        sb.append("============================================\n\n");

        int totalComponentes = pintor.getVertices().size();
        int totalConexoes = grafo.getArestas().size();
        int derrubadosManual = 0;
        for (String id : pintor.getVertices().keySet()) {
            if (!grafo.posteAtivo(id)) {
                derrubadosManual++;
            }
        }

        sb.append("1. RESUMO GERAL\n");
        sb.append("--------------------------------------------\n");
        sb.append("Total de componentes: ").append(totalComponentes).append('\n');
        sb.append("Total de conexões: ").append(totalConexoes).append('\n');
        sb.append("Componentes em falha: ").append(derrubadosManual).append('\n');
        sb.append("Componentes sem energia pelo efeito cascata: ")
                .append(pintor.getVerticesSemEnergia().size()).append("\n\n");

        sb.append("2. PONTOS PRIORITÁRIOS PARA MANUTENÇÃO\n");
        sb.append("--------------------------------------------\n");
        sb.append(gerarSecoesImpacto());

        return sb.toString();
    }

    /**
     * Abre o relatório em sua própria janela, independente do Painel de Testes Livres
     * (não é mais um popup preso a ele). Usa as mesmas cores (moldura escura + conteúdo
     * claro) só para manter a identidade visual entre as duas janelas.
     */
    public void abrirRelatorioCompleto() {
        if (janelaRelatorio == null) {
            criarJanelaRelatorio();
        }
        atualizarConteudoRelatorio();
        posicionarJanelaDireita(janelaRelatorio);
        janelaRelatorio.setVisible(true);
        janelaRelatorio.toFront();
    }

    private void criarJanelaRelatorio() {
        janelaRelatorio = new JDialog(interfacePrincipal, "Relatório da Rede", false);
        Dimension tamanhoTela = Toolkit.getDefaultToolkit().getScreenSize();
        janelaRelatorio.setSize(560, Math.min(650, tamanhoTela.height - 120));
        janelaRelatorio.setMinimumSize(new Dimension(420, 360));
        janelaRelatorio.setLayout(new BorderLayout());
        janelaRelatorio.setAlwaysOnTop(true);

        areaRelatorio = new JTextArea();
        areaRelatorio.setEditable(false);
        areaRelatorio.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaRelatorio.setBackground(new Color(240, 240, 245));
        areaRelatorio.setForeground(Color.BLACK);
        areaRelatorio.setMargin(new Insets(12, 12, 12, 12));

        JPanel painelConteudo = new JPanel(new BorderLayout());
        painelConteudo.setBackground(Color.DARK_GRAY);
        painelConteudo.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        painelConteudo.add(new JScrollPane(areaRelatorio), BorderLayout.CENTER);

        JButton btnAtualizar = new JButton("🔄 Atualizar");
        btnAtualizar.addActionListener(e -> atualizarConteudoRelatorio());
        JButton btnSalvar = new JButton("💾 Salvar em Arquivo...");
        btnSalvar.addActionListener(e -> salvarRelatorioEmArquivo(areaRelatorio.getText()));

        JPanel painelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        painelInferior.setBackground(Color.DARK_GRAY);
        painelInferior.add(btnAtualizar);
        painelInferior.add(btnSalvar);

        janelaRelatorio.add(painelConteudo, BorderLayout.CENTER);
        janelaRelatorio.add(painelInferior, BorderLayout.SOUTH);
    }

    private void atualizarConteudoRelatorio() {
        areaRelatorio.setText(gerarRelatorioCompleto());
        areaRelatorio.setCaretPosition(0);
    }

    private void salvarRelatorioEmArquivo(String conteudo) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório...");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivo de Texto (.txt)", "txt"));

        if (fileChooser.showSaveDialog(janelaRelatorio) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            if (!arquivo.getName().toLowerCase().endsWith(".txt")) {
                arquivo = new File(arquivo.getParentFile(), arquivo.getName() + ".txt");
            }
            try (PrintWriter out = new PrintWriter(new FileWriter(arquivo))) {
                out.print(conteudo);
                JOptionPane.showMessageDialog(janelaRelatorio, "Relatório salvo em:\n" + arquivo.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(janelaRelatorio, "Erro ao salvar arquivo: " + ex.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // 6. MÉTODOS AUXILIARES
    // ==========================================

    private void posicionarJanela(JDialog janela) {
        //Dimension tamanhoTela = Toolkit.getDefaultToolkit().getScreenSize();
        Point loc = interfacePrincipal.getLocationOnScreen();

        int x = loc.x + 20;
        int y = Math.max(0, loc.y + interfacePrincipal.getHeight() - janela.getHeight() - 40);

        janela.setLocation(x, y);
    }

    /** Ancora no canto inferior direito, para não sobrepor o Painel de Testes Livres (canto esquerdo). */
    private void posicionarJanelaDireita(JDialog janela) {
        Point loc = interfacePrincipal.getLocationOnScreen();

        int x = loc.x + interfacePrincipal.getWidth() - janela.getWidth() - 20;
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
