# Simulador de Rede de Energia Elétrica

Aplicação Java/Swing para montar, visualizar e analisar uma rede de distribuição de energia sobre um mapa. A topologia é representada por um **grafo não direcionado e ponderado**: vértices representam elementos da rede e arestas representam cabos. O peso (`lambda`) de uma aresta é a distância geográfica aproximada entre os dois pontos, em metros.

Além do grafo, cada vértice também é a raiz de uma árvore binária que pode guardar casas vinculadas àquele poste. Essa estrutura permite simular tanto problemas na malha elétrica quanto falhas locais de atendimento.

> O diagrama de classes do projeto está em [diagrama_classes.puml](diagrama_classes.puml).

## Sumário

- [Requisitos e execução](#requisitos-e-execução)
- [Modelo da rede](#modelo-da-rede)
- [Como usar o simulador](#como-usar-o-simulador)
- [Algoritmos implementados](#algoritmos-implementados)
- [Classes e responsabilidades](#classes-e-responsabilidades)
- [API da classe Grafo](#api-da-classe-grafo)
- [Persistência](#persistência)
- [Observações técnicas](#observações-técnicas)

## Requisitos e execução

- JDK 8 ou superior;
- as bibliotecas presentes em `lib/`:
  - `jxmapviewer2-2.6.jar` para o mapa OpenStreetMap;
  - `commons-logging-1.2.jar`, dependência do componente de mapa.

No PowerShell, a partir da raiz do projeto:

```powershell
javac -encoding UTF-8 -cp "lib/*" -d out (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object FullName)
java -cp "out;lib/*" App
```

O mapa usa blocos do OpenStreetMap, portanto a visualização completa depende de acesso à internet. A lógica do grafo e os algoritmos continuam independentes do mapa.

## Modelo da rede

| Conceito | Representação no código | Significado |
| --- | --- | --- |
| Subestação | `TipoVertice.SUBESTACAO` | Fonte visual de energia; o recálculo parte de todas as subestações ativas existentes no mapa. |
| Poste | `TipoVertice.POSTE` | Ponto intermediário da distribuição. |
| Casa | `TipoVertice.CASA` | Ponto consumidor exibido no mapa. Na estrutura genérica também pode existir como `Nodo` na árvore de um poste. |
| Vértice | `Vertice<TIPO>` | Nó lógico do grafo, identificado por um valor comparável. |
| Cabo/conexão | `Aresta<TIPO>` | Ligação não direcionada entre dois vértices; possui peso `lambda` e estado ativo/inativo. |
| Falha | `ativo = false` | Pode ocorrer em um vértice/poste ou em uma aresta/cabo. Algoritmos que respeitam falhas ignoram o componente inativo. |

Na interface, todos os tipos visuais (subestação, poste e casa) são adicionados ao `Grafo<String>` como vértices. O tipo é mantido pelo `PintorConexoes` para definir a aparência no mapa.

## Como usar o simulador

### Barra de ferramentas

| Controle | Ação |
| --- | --- |
| **Arquivo** | Abre opções para salvar a topologia, carregar um arquivo `.txt` ou limpar toda a rede. |
| **Testes** | Executa o cenário automático pronto ou abre o teste personalizado da rede desenhada. |
| **Navegar** | Volta ao modo de navegação do mapa: arrastar move o mapa e a roda do mouse altera o zoom. |
| **Add: Poste** | Abre a escolha entre **Poste**, **Casa** e **Subestação**. Depois, clique no mapa para inserir o elemento escolhido. |
| **Auto-Conectar** | Quando ativo, o novo vértice é conectado ao último vértice inserido. Clicar em um vértice existente também pode continuar a cadeia a partir dele. |
| **Ligar Vértices** | Clique no vértice de origem e depois no de destino; o sistema cria um cabo com distância calculada pela fórmula de Haversine. |
| **Remover** | Clique em um elemento para removê-lo, junto com todos os cabos incidentes. |
| **Simular Falha** | Alterna o estado ativo/inativo do componente clicado. Um componente inativo é exibido em vermelho e perde atendimento. Também abre o painel de teste personalizado. |
| **Algoritmos** | Abre os comandos de AGM, pontes, DFS, BFS, fluxo máximo, diagnóstico de duplicatas e limpeza de destaques. |
| **Visão** | Alterna entre o grafo inteiro e somente os elementos destacados pelo último algoritmo executado. Só é habilitado após um resultado visual. |
| **Pausar / Iniciar** | Pausa ou reinicia a animação dos pontos amarelos que representam energia em circulação. |
| **Legenda** | Mostra ou oculta a legenda do mapa. |

### Menus

**Arquivo**

- **Salvar Rede**: grava vértices, coordenadas, tipos e arestas.
- **Carregar Rede**: substitui a rede atual pelo conteúdo de um arquivo salvo.
- **Limpar Tudo (Reset)**: remove a topologia e todos os elementos desenhados.

**Testes**

- **Rodar Cenário Automático**: cria uma rede-base e conduz um roteiro visual de etapas.
- **Testar Rede Desenhada**: abre uma janela para escolher componentes, aplicar/reparar falhas e consultar o relatório da rede.

**Algoritmos**

- **Árvore Geradora Mínima (AGM)**: destaca a rede de menor custo de conexão.
- **Pontes**: destaca cabos críticos.
- **Busca em Profundidade (DFS)** e **Busca em Largura (BFS)**: solicitam o vértice inicial, numeram a ordem de visita e isolam visualmente o subgrafo percorrido.
- **Fluxo Máximo**: solicita origem e destino e informa a capacidade máxima calculada.
- **Diagnóstico: Arestas Duplicadas**: imprime o resultado no console da IDE.
- **Limpar Destaques**: remove marcações de algoritmos e retorna à visão normal.

### Cores e animações do mapa

- Roxo: subestação.
- Laranja: poste.
- Azul: casa.
- Cinza escuro: conexão normal.
- Amarelo animado: energia em circulação.
- Vermelho tracejado e raio: falha ou área sem energia.
- Verde: AGM; laranja: pontes; a busca também mostra a numeração `#1`, `#2`, etc.

## Algoritmos implementados

| Algoritmo | Classe | Estratégia | Resultado | Complexidade |
| --- | --- | --- | --- | --- |
| Busca em largura | `BFS` | Fila e conjunto de visitados. | Ordem de visita por camadas a partir de uma origem. | `O(V + E)` |
| Busca em profundidade | `DFS` | Recursão e conjunto de visitados. | Ordem de visita seguindo um ramo antes de retroceder. | `O(V + E)` |
| Áreas afetadas | `AreasAfetadas` | BFS a partir de uma fonte, sobre a sub-rede ativa. | Vértices inativos ou não alcançáveis pela fonte. | `O(V + E)` |
| Caminho alternativo | `CaminhoAlternativo` | Dijkstra sem fila de prioridade. | Menor caminho ponderado entre origem e destino, ou lista vazia. | `O(V² + E)` |
| Fluxo máximo | `FluxoMaximo` | Edmonds-Karp (Ford-Fulkerson com BFS) em rede residual. | Capacidade máxima entre origem e destino. | `O(VE²)` |
| Pontes | `Pontes` | DFS de Tarjan com tempos `disc` e valores `low`. | Cabos cuja remoção aumenta o número de componentes. | `O(V + E)` |
| AGM | `AGM` | Kruskal, ordenação por peso e Union-Find. | Arestas da árvore/floresta geradora mínima. | `O(E log E)` |
| Pontos prioritários | `PontosPrioritarios` | Combina pontes, grau de conexão e comprimento. | Rankings para manutenção preventiva. | depende da consulta; ordenações em `O(E log E)` |

### Regras comuns aos algoritmos

`BFS`, `DFS`, `AreasAfetadas`, `CaminhoAlternativo`, `FluxoMaximo` e `Pontes` constroem sua própria lista de adjacência a partir das arestas ativas e ignoram vértices inativos. O grafo é tratado como não direcionado.

#### BFS e DFS

As buscas servem para conhecer os elementos alcançáveis no estado atual da rede. A ordem pode variar entre execuções equivalentes, pois os vértices são armazenados em `HashMap`, que não garante ordem de iteração.

#### Áreas afetadas

Se a fonte estiver inativa, todos os outros vértices são retornados como afetados. Caso contrário, uma BFS encontra os vértices ainda alimentados; os inativos e os não alcançados são classificados como sem atendimento.

#### Caminho alternativo

Usa `lambda` como custo/distância e retorna a rota de menor soma de pesos. Só funciona com pesos não negativos, premissa válida para distâncias. Não há item de menu específico para ele; é disponibilizado pela API do `Grafo` e utilizado por cenários de análise.

#### Fluxo máximo

Cada cabo não direcionado gera capacidade residual nos dois sentidos, ambas iguais a `lambda`. Assim, neste projeto `lambda` tem dois papéis: distância nos recursos visuais/AGM/caminho e capacidade no cálculo de fluxo. Isso é uma simplificação didática; em uma aplicação real, distância e capacidade deveriam ser atributos distintos.

#### Pontes

Uma ponte não possui rota alternativa: se o cabo for removido, a rede ativa se fragmenta. A implementação impede a adição de cabos duplicados, importante porque arestas paralelas alterariam a interpretação de ponte.

#### AGM — Árvore Geradora Mínima

Kruskal ordena os cabos por `lambda` crescente e adiciona um cabo apenas se ele não formar ciclo. `ConjuntoDisjunto` usa compressão de caminho e união por rank. Se a rede estiver desconexa, o resultado é uma **floresta geradora mínima**, não uma única árvore.

## Classes e responsabilidades

### Inicialização

| Classe | Responsabilidade |
| --- | --- |
| `App` | Ponto de entrada. O método `main` cria e torna visível `VisualizadorRede`. |

### Estrutura de dados (`estruturaGrafo`)

| Classe | Responsabilidade e principais operações |
| --- | --- |
| `Nodo<TIPO>` | Nó da árvore binária: armazena `nome`, filhos esquerdo/direito e estado `ativo`. Expõe construtor e getters/setters desses campos. |
| `Vertice<TIPO>` | Estende `Nodo`; é simultaneamente um vértice do grafo e a raiz da árvore de casas. `add` insere uma casa por comparação BST; `printInsert` percorre pré-ordem; `printOrdem` percorre em ordem. |
| `Aresta<TIPO>` | Cabo entre `u` e `v`, com peso `lambda` e estado `ativo`. Possui construtor e getters/setters para os extremos, peso e estado. |
| `ConjuntoDisjunto<TIPO>` | Union-Find usado por Kruskal. `criarConjunto` inicia um conjunto; `encontrar` localiza a raiz com compressão de caminho; `unir` une conjuntos por rank e informa se a união ocorreu. |
| `Grafo<TIPO>` | Fachada e repositório da topologia. Mantém `Map<TIPO, Vertice<TIPO>>`, lista de arestas, fonte principal e último vértice inserido. Delega os algoritmos às classes do pacote `algoritmos`. |

### Algoritmos (`algoritmos`)

| Classe | Função pública |
| --- | --- |
| `BFS<TIPO>` | `executar(inicio)`: devolve a ordem da busca em largura. |
| `DFS<TIPO>` | `executar(inicio)`: devolve a ordem da busca em profundidade. |
| `AreasAfetadas<TIPO>` | `executar(fonte)`: devolve vértices sem atendimento a partir da fonte. |
| `CaminhoAlternativo<TIPO>` | `executar(origem, destino)`: devolve o menor caminho ativo pelo peso. |
| `FluxoMaximo<TIPO>` | `executar(origem, destino)`: devolve a capacidade máxima. |
| `Pontes<TIPO>` | `executar()`: devolve as conexões críticas da topologia ativa. |
| `AGM<TIPO>` | `executar(vertices, arestas)`: devolve as arestas selecionadas por Kruskal. |
| `PontosPrioritarios<TIPO>` | `pontesCriticas`, `postesMaisFragilizados(topN)` e `cabosMaisLongos(topN)`: produz indicadores para manutenção. |

### Visualização e testes (`visualizacao`)

| Classe | Responsabilidade |
| --- | --- |
| `VisualizadorRede` | Janela principal. Configura mapa, barra de ferramentas, interação por cliques, persistência, recálculo visual de energia e execução dos algoritmos disponíveis na interface. |
| `PintorConexoes` | Implementa `Painter<JXMapViewer>`. Guarda os dados visuais (`VerticeVis` e `ConexaoVis`), desenha nós/cabos/legenda, anima a energia e registra destaques, falhas e ordem de visita. |
| `PintorConexoes.VerticeVis` | Estrutura visual pública com nome, posição geográfica e tipo do elemento. |
| `PintorConexoes.ConexaoVis` | Estrutura interna com origem, destino e peso exibido para uma conexão. |
| `GerenciadorTestes` | Controla o roteiro de teste automático, a janela de teste personalizado, o registro de falhas manuais e a geração do relatório de impacto. |
| `ModeloTesteAutomatico` | Classe utilitária que cria uma rede pré-definida nas coordenadas da região-base e a conecta no grafo e no pintor. |

### Métodos importantes da visualização

- `VisualizadorRede.recalcularEnergia()`: executa uma BFS multi-origem a partir de todas as subestações visuais ativas; atualiza os elementos atendidos, sem energia e com falha manual.
- `VisualizadorRede.calcularDistancia(...)`: aplica Haversine para estimar a distância geográfica usada como peso do cabo.
- `PintorConexoes.adicionarVertice`, `adicionarAresta` e `removerVertice`: mantêm o espelho visual da topologia.
- `PintorConexoes.setEstadoEnergia(...)`: recebe o estado calculado pela janela para renderizar cores, raios e animações.
- `PintorConexoes.destacarAresta`, `destacarVertice` e `definirOrdemVisita`: armazenam os resultados de algoritmos a serem exibidos.
- `GerenciadorTestes.registrarFalhaManualNoMapa(...)`: sincroniza uma falha aplicada por clique com a janela de teste personalizado.

## API da classe `Grafo`

`Grafo` centraliza a manipulação da topologia e oferece uma API que pode ser usada sem a interface gráfica.

| Grupo | Método | Descrição |
| --- | --- | --- |
| Criação | `Grafo()` | Inicializa os mapas e listas vazios. |
| Criação | `addVertice(id)` | Insere um vértice sem criar conexão; o primeiro passa a ser a fonte principal. |
| Criação | `addVerticeAutoConectado(id, peso, conectar)` | Insere o vértice e, opcionalmente, o liga ao último inserido. Há sobrecargas com conexão obrigatória e peso padrão `1`. |
| Criação | `addAresta(origem, destino, lambda)` | Adiciona cabo entre dois vértices existentes; ignora duplicatas, inclusive no sentido inverso. |
| Criação | `addElementoVertice(poste, casa)` | Insere uma casa na BST pertencente ao poste. |
| Criação | `addVerticeExixtente(id, vertice)` | Insere uma referência de vértice já existente; é usado ao montar a AGM. O nome contém a grafia atual do código. |
| Estado | `get/setUltimoVerticeAdicionado` | Consulta ou altera o ponto usado pela auto-conexão. |
| Estado | `getFontePrincipal()` | Retorna o primeiro vértice criado como fonte lógica. |
| Estado | `setPosteAtivo(id, ativo)` / `posteAtivo(id)` | Altera ou consulta a disponibilidade de um vértice. |
| Estado | `setCasaAtiva(poste, casa, ativo)` / `casaAtiva(...)` | Altera ou consulta o estado local de uma casa na árvore de um poste. |
| Estado | `estaAtendida(poste, casa)` | Retorna verdadeiro somente se o poste e a casa estiverem ativos. |
| Estado | `marcarCurtoCircuito(id)` / `repararCurtoCircuito(id)` | Desliga/liga um poste e chama o recálculo de rotas no console. |
| Estado | `romperAresta(u, v)` / `repararAresta(u, v)` | Desliga/liga o cabo correspondente, sem remover seus extremos. |
| Remoção | `removerVertice(id)` | Exclui o vértice e todas as arestas incidentes; limpa referências internas se necessário. |
| Remoção | `limparGrafo()` | Remove vértices e arestas e reseta o último vértice adicionado. |
| Consulta | `listarCasasSemAtendimento(poste)` | Percorre a árvore e lista casas inativas ou todas elas se o poste caiu. |
| Consulta | `recalcularRotas()` | Analisa áreas afetadas a partir da fonte principal e escreve o resultado no console. |
| Busca | `dfs(inicio)` / `bfs(inicio)` | Delega para os algoritmos de busca e retorna a lista de visita. |
| Análise | `identificarAreasAfetadas(fonte)` | Retorna os vértices desconectados ou inativos. |
| Análise | `caminhoAlternativo(origem, destino)` | Retorna a rota ativa de menor distância. |
| Análise | `fluxoMaximo(origem, destino)` | Retorna a capacidade máxima da rede ativa. |
| Análise | `encontrarPontes()` | Retorna os cabos críticos da topologia ativa. |
| Análise | `AGM(grafoOriginal)` | Cria e devolve um novo grafo com a árvore/floresta geradora mínima. |
| Análise | `printPontosPrioritarios()` | Imprime pontes, postes de menor grau e cabos mais longos. |
| Simulação | `simularQuedaSubestacao(id)` | Desativa a subestação indicada e imprime áreas afetadas. |
| Simulação | `simularRompimentoTubulacao(u, v)` | Rompe o cabo e imprime as áreas afetadas usando `u` como referência. |
| Depuração | `printVertices`, `printArestas`, `printGrafo` | Imprimem a estrutura atual no console. |
| Depuração | `printArestasDuplicadas()` | Varre a lista e relata conexões repetidas. |
| Depuração | `printAtendimento`, `printDFS`, `printBFS`, `printAreasAfetadas`, `printFluxoMaximo`, `printPontes`, `printCaminhoAlternativo` | Versões voltadas ao console das consultas correspondentes. |
| Acesso | `getArestas()` / `getVerticesMap()` | Expõem as coleções usadas pela interface e pelos algoritmos. |

## Persistência

O salvamento usa texto separado por ponto e vírgula:

```text
[VERTICES]
Nome;Latitude;Longitude;TIPO
[ARESTAS]
Origem;Destino;Peso
```

Exemplo:

```text
[VERTICES]
Sub_100; -12.6736; -39.1028; SUBESTACAO
Poste_200; -12.6740; -39.1030; POSTE
[ARESTAS]
Sub_100;Poste_200;58
```

Ao carregar, o programa recria os elementos visuais e lógicos e então recalcula a energia.

## Observações técnicas

Esta seção registra pontos relevantes identificados na revisão do código atual.

- O estado de falha (`ativo`) de vértices e arestas **não é salvo** no arquivo. Após carregar, todos os componentes voltam ativos.
- `limparGrafo()` não redefine explicitamente `fontePrincipal`; isso pode deixar uma referência antiga até que uma nova fonte seja definida/removida. Para um reset completo, o ideal é definir também `fontePrincipal = null`.
- `PontosPrioritarios.postesMaisFragilizados` contabiliza arestas ativas, mas não filtra vértices inativos; o ranking deve ser interpretado como indicador topológico, não como diagnóstico do estado operacional.
- A implementação de `Pontes` devolve novas `Aresta`s com peso `0`, pois para o destaque visual importam os extremos. Portanto, o peso retornado por uma ponte não representa o peso original do cabo.
- A fonte usada pelo backend (`fontePrincipal`) é o primeiro vértice inserido; já o recálculo visual usa todas as subestações visuais ativas. Em uma evolução do domínio, convém unificar essas duas regras.
- `HashMap` não garante ordem de iteração. Por isso, a ordem entre vizinhos equivalentes em BFS, DFS, AGM e relatórios pode mudar.
- Para maior precisão de domínio, recomenda-se separar `distanciaMetros` e `capacidade` na `Aresta`, evitando reutilizar `lambda` para objetivos diferentes.
