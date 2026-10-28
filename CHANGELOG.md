# Changelog

Formato baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e versionamento semântico.

## [1.12.1] - 2026-10-27

### Adicionado
- **O título sugere o atributo.** Escrever "Correr" move o seletor para Força; "Ler a
  Bíblia", para Espírito; "Estudar inglês", para Inteligência. `SugestaoAtributo` compara
  começos de palavra, então "estud" cobre estudar, estudo e estudando sem uma entrada para
  cada conjugação, e ignora acento e caixa.
- Quando dois começos batem, vence o mais longo, que é o mais específico: em "Ler a Bíblia",
  "bibli" ganha de "ler". Sem reconhecer nada, a sugestão não devolve nada — um palpite
  fraco seria pior que silêncio, porque ela move a escolha da pessoa.
- A linha abaixo dos atributos diz "sugerido pelo título" enquanto a sugestão estiver
  valendo. É o que separa sugerir de decidir por alguém.
- Tocar num atributo trava a escolha: dali em diante o título não mexe mais nela. Editar um
  lembrete que já existe começa travado — quem escolheu Espírito para "Ler a Bíblia" não
  quer ver isso mudar ao corrigir uma vírgula.
- `SugestaoAtributoTest` cobre o óbvio de cada atributo, as sobreposições e o silêncio.

## [1.12.0] - 2026-10-27

Lembretes e exercícios criados pela própria pessoa, e a separação entre o dono do app e
quem cria uma conta.

### Adicionado
- **Lembretes próprios**: título, descrição, horário e dias. O aviso sai 30 minutos antes,
  a mesma lógica do karatê — o lembrete existe para dar tempo de se preparar, não para
  avisar quando já é tarde.
- Cada lembrete escolhe **qual atributo treina**, e cumpri-lo rende XP mais +0,10 nele. Sem
  isso, criar "Estudar inglês" daria Disciplina, e o app inteiro existe para os atributos
  refletirem o que a pessoa faz de verdade.
- **Exercícios próprios**: nome, séries, repetições ou segundos, e dias. Aparecem na aba de
  Treinos, abaixo do treino fixo quando há um, e sozinhos quando não há.
- `ui/itens` com os dois formulários e o seletor de dias da semana.
- Room na versão 4, com as tabelas `lembrete` e `exercicio` — ambas já com `usuarioId`.

### Alterado
- **`Habito` ganhou `doDono`.** Só a leitura da Bíblia vale para todos; oração, karatê,
  treino e mobilidade são a agenda do Pedro. Quem cria uma conta monta a própria.
- `Habito.doDia` agora pede o `admin`, que desceu para o `Status` local: o fechamento do dia
  precisa dele e não fala com a nuvem.
- **Os lembretes criados contam no fechamento do dia** igual aos fixos, com sequência e
  falhas seguidas. Sem isso, quem monta a própria rotina teria um dia perfeito só com a
  Bíblia, e tudo o que escolheu fazer não valeria nada.
- `fecharDiasPendentes` não faz mais nada enquanto não souber de quem é o dia. Fechar antes
  marcaria o dia como resolvido sem contar os lembretes da pessoa, e isso não se desfaz.
- `marcar` e a marcação dos criados passaram a dividir o mesmo caminho interno: a tabela de
  registros guarda o hábito por nome, então basta a chave ser diferente.

### Notas
- O alarme dos lembretes criados usa id a partir de 1000, bem acima dos cinco fixos, e leva
  título, texto, dias e horário dentro da própria intenção — assim o receptor mostra o aviso
  e marca o próximo sem abrir o banco, que é trabalho pesado demais para um BroadcastReceiver.
- Os exercícios criados não contam séries nem liberam o botão de concluir: isso pertence ao
  treino fixo. Eles são o conteúdo do treino, não a pontuação. Quem quiser que treinar valha
  pontos cria um lembrete de Força, que é onde a conta acontece.
- As tabelas antigas (status, registro, dia fechado, progresso) continuam sem dono. Enquanto
  houver uma conta por aparelho não aparece; duas contas no mesmo celular dividiriam o
  progresso. É a próxima migração.

## [1.11.3] - 2026-10-27

### Corrigido
- **A variante `otimizado` não abria.** Ela minifica com R8, e o projeto não tinha nenhum
  arquivo de regras: o `proguardFiles` listava só o padrão do Android. O Firestore monta o
  perfil por reflexão — `toObject(PerfilUsuario::class.java)` lê os nomes dos campos e chama
  o construtor sem argumentos —, então o R8 renomeava tudo e a leitura quebrava no aparelho.
- A variante nasceu na v1.4.1, antes de o Firebase existir. Funcionava; parou de funcionar
  quando o login entrou na v1.5.0, e ninguém rodou ela desde então.
- Entrou `app/proguard-rules.pro`, guardando o pacote `domain.model` inteiro: o perfil, por
  causa da reflexão, e as enumerações, que são gravadas pelo nome e lidas com `valueOf` tanto
  no Room quanto no Firestore.

## [1.11.2] - 2026-10-27

### Corrigido
- O build `otimizado` parava no `lintVitalOtimizado` com
  `InvalidFragmentVersionForActivityResult`: o `firebase-auth` arrasta uma versão antiga do
  `androidx.fragment`, e `registerForActivityResult` exige 1.3.0 ou mais novo. O erro só
  aparecia em release, porque o lint fatal não roda no debug.
- A correção é uma restrição de versão, não uma dependência nova: o app não usa fragment
  nenhum — é uma Activity só, tudo em Compose —, então o piso sobe para quem puxar a
  biblioteca sem ela entrar no APK por conta própria.

## [1.11.1] - 2026-10-27

Primeira compilação do projeto desde a v1.5.0. Um erro só apareceu.

### Corrigido
- `SessaoViewModel`: o `catch` do fluxo de perfil ainda emitia `semPersonagem`, nome que
  deixou de existir quando a entrada virou três passos na 1.11.0. Passou a emitir `semNome`.
  O renomear anterior pegou as outras ocorrências e deixou esta porque ela está dentro de
  uma lambda, longe do bloco que mudou.

## [1.11.0] - 2026-10-27

A entrada virou três passos, e a abertura entrou no meio deles.

### Alterado
- **Nova ordem: login → nome → abertura → classe → app.** Antes, nome e classe estavam na
  mesma tela e a abertura vinha depois das duas. O problema era de sentido: a pessoa
  escolhia um caminho antes de o sistema dizer qual é a missão. Agora ela dá o nome, ouve o
  compromisso, e só então decide por onde começar — a escolha passa a ser resposta a algo.
- **`NomeTela`** (nova) pede só o nome, centrada como a de login para as duas parecerem a
  mesma sala. **`ClasseTela`** (saiu de `CriarPersonagemTela`) traz só as classes, com o
  nome e o número do jogador no topo — quem a abertura acabou de anunciar.
- **O contador de jogadores passou a rodar ao gravar o nome**, não ao criar o personagem
  inteiro. É quando o jogador nasce, e é o que permite à abertura dizer nome e número.
- `PerfilUsuario.classeInicial` nasce vazia em vez de `"APRENDIZ"`. Aprendiz é uma escolha
  legítima, e com o padrão antigo não dava para distinguir de quem ainda não escolheu.
  Entraram `temNome`, `temClasse` e `completo`, que é a conjunção das duas.
- `PerfilRepositorio` e `Repositorio` ganharam um método por passo: `salvarNome`/`definirNome`
  e `salvarClasse`/`definirClasse`, no lugar do antigo `definirPersonagem`.
- `EstadoSessao` agora é `SemNome` → `BoasVindas` → `SemClasse` → `Pronto`.

### Notas
- A marca de abertura pendente vive só na memória, de propósito: se o app morrer no meio da
  cutscene, a pessoa cai direto na escolha da classe em vez de ficar presa numa abertura
  que não termina. Ela não volta a rodar, e isso é aceitável — o personagem já existe.
- O bônus da classe continua só para quem está no estado inicial, agora conferido dentro
  de `definirClasse`.

## [1.10.0] - 2026-10-27

A classe escolhida na criação passa a render pontos de verdade, em vez de ser um caso
especial dentro do motor.

### Adicionado
- **Bônus de classe inicial** (`Regras.BONUS_CLASSE_INICIAL`, +0,60). Escolher Guerreiro na
  criação começa o personagem com 0,60 de Força; Sacerdote, de Espírito, e assim por diante.
- `MotorPontuacao.atributosIniciais(classe)` decide a distribuição, e
  `Classe.atributosDe(classe)` dá o caminho inverso de `pura` e `hibrida`.
- Cada cartão da tela de criação mostra o que a escolha rende, lido do próprio motor — o
  texto não tem como descolar da regra se o número mudar.
- `BonusClasseInicialTest` cobre os quatro casos puros, o Aprendiz, a igualdade dos totais
  e a relação entre o bônus e a margem.

### Por quê
- O valor precisa ser maior que `MARGEM_CLASSE` (0,50), senão o motor continuaria lendo
  Aprendiz no primeiro dia e a escolha não apareceria. 0,60 é o menor número redondo acima
  disso: três dias de Espírito ou quatro treinos de Força, que o uso de verdade ultrapassa
  em cerca de duas semanas.
- **Aprendiz recebe o mesmo total**, espalhado em +0,15 por atributo. Escolher "sem
  inclinação" não pode custar pontos, senão vira a opção de quem não prestou atenção. Como
  a diferença entre o maior e o menor continua zero, o motor segue lendo Aprendiz.
- As híbridas levam o bônus cheio nos dois atributos, e não metade em cada: com metade, a
  diferença ficaria dentro da margem e o motor leria Aprendiz. Elas não aparecem na criação,
  mas a conta precisa fechar se um dia aparecerem.

### Cuidado tomado
- O bônus só vale para um personagem no estado inicial — nível 1, zero de XP e os quatro
  atributos no valor de partida. Antes do login o app era de um usuário só, então pode
  existir um personagem com progresso de verdade passando pela tela de criação, e os
  atributos dele são dele.

## [1.9.1] - 2026-10-27

A marca da 1.9.0 foi refeita antes de chegar a qualquer aparelho. O "F" deixou de ser um
traço e virou área cheia, inclinada 12°, em degradê do verde ao azul, com a fagulha adiante
da barra do meio.

### Alterado
- **Ícone do app** (`ic_launcher_foreground.xml`): F cheio com degradê `#39E58C` → `#3EA8FF`
  e fagulha verde. A inclinação está assada nas coordenadas do caminho, porque um grupo de
  VectorDrawable faz escala, rotação e translação, mas não cisalhamento.
- **Tamanho dentro do ícone**: a marca ocupa 51,8 dos 108 do quadro (48%), proporção normal
  de ícone do Android. O ponto mais distante do centro fica a 35,2, e o recorte redondo só
  começa a cortar aos 36 — nada é perdido em nenhum formato de máscara.
- **Monocromático e notificação** são o mesmo desenho sem degradê. Não é escolha: no Android
  13+ com ícones temáticos o sistema repinta tudo com a cor do papel de parede, e o ícone
  pequeno é só silhueta. A marca passa a ter duas formas oficiais, a colorida e a chapada.
- **A letra cheia aguenta os 24dp.** Era o que o traço fino não fazia: na barra de status
  virava um risco. Agora o F continua F e a fagulha continua um losango ao lado dele.
- **O letreiro deixou de ser desenho.** Saiu `forja_wordmark.xml`; entraram
  `ic_marca_forja.xml` (só o F) e `ui/componentes/MarcaForja.kt`, que põe a palavra "forja"
  em Chakra Petch ao lado dele. O texto acompanha qualquer altura sem um vetor por tamanho,
  e o tamanho sai da densidade e não em `sp`, para o logotipo não crescer com o ajuste de
  fonte do sistema e passar por cima do F.
- Login usa a marca a 40dp; criação do personagem, a 24dp.

### Notas
- O degradê no ícone exige API 24; o mínimo do app é 26.
- A tela de abertura reaproveita o `ic_launcher_foreground`, e a API SplashScreen mostra os
  dois terços centrais. Com a marca a 48%, ela aparece inteira — só um pouco pequena. Se
  ficar discreta demais no aparelho, dá para dar um drawable próprio à abertura.

## [1.9.0] - 2026-10-27

O app agora se chama **Forja**. O símbolo é um losango verde — a brasa — que aparece em três
lugares: termina a barra do "f" no ícone, é o pingo do "j" no letreiro e marca a ponta da
barra de XP.

### Adicionado
- Ícone adaptativo novo: o "f" de traço arredondado com a brasa na ponta da barra, sobre
  `#111A2B`. Camada monocromática própria (`ic_launcher_monochrome.xml`), porque o Android 13+
  tinge tudo numa cor só e a brasa precisa virar parte da silhueta em vez de sumir.
- `forja_wordmark.xml`, o letreiro desenhado à mão, no login e na criação do personagem.
- `ui/componentes/Brasa.kt`: o losango reutilizável, aceso (verde cheio) ou apagado (só
  contorno). Sem animação, com o `Path` guardado em `remember`.
- `BarraNeon` ganhou o parâmetro `brasa`, que põe o losango na ponta do preenchimento. Usado
  só na barra de XP da tela de Status.
- Tela de abertura pela API SplashScreen (`androidx.core:core-splashscreen:1.0.1`), sem
  atraso de propósito.

### Alterado
- `app_name` passou de "Pessoal" para "Forja"; o tema virou `Theme.Forja`.
- Paleta com os valores da marca, mantendo os nomes que já existiam no código: `Fundo`
  `#0A0F1C`, `Superficie` `#111A2B`, `Trilho` `#1C2638`, `Texto` `#E8EEF7`, `Neon` `#3EA8FF`,
  `Ganho` `#39E58C`. Trocar os valores em vez de acrescentar cores novas evita ficar com dois
  azuis e dois verdes disputando o mesmo papel. De novo mesmo, só `BrasaApagada` `#3A4659`.
- Ícones de notificação redesenhados com a marca; a cor de destaque passou de azul para a brasa.
- A tela de login troca o escudo e a palavra "PESSOAL" pelo letreiro, com o lema
  "Forje-se todo dia" no lugar da frase que repetia o que o botão já dizia.
- No letreiro, as letras depois do "f" andaram 5 unidades para a direita: o vão entre "f" e "o"
  era 4 e os outros 9.

### Não mudou
- `applicationId` e `namespace` continuam `dev.pedro.pessoal`. Mudar faria o Android instalar
  como outro app, perdendo os dados locais, e invalidaria o `google-services.json`.
- O check dos hábitos continua sendo um certinho. Trocar por losango é mudança de interface,
  não de marca, e fica para depois de ver no aparelho.
- Nenhuma regra de XP, atributo ou rank foi tocada. Os selos de rank continuam como estavam.

## [1.8.0] - 2026-10-27

A abertura de quem acabou de criar o personagem. Começa como terminal — o sistema ligando e
reconhecendo quem chegou — e termina em caixa de diálogo de RPG, onde a missão é anunciada.

### Adicionado
- **Abertura de boas-vindas** (`ui/boasvindas/BoasVindasTela.kt`). O terminal escreve letra por
  letra, identifica o jogador pelo número de chegada e pelo nome, e dá lugar a uma caixa de
  diálogo com a mensagem da missão em quatro páginas. Um toque pula a digitação; a última página
  só sai pelo botão "Aceitar a missão", porque é o aceite.
- **Número do jogador** (`data/nuvem/ContadorRepositorio.kt`). Vem de uma transação em
  `contadores/jogadores`, não de uma contagem da coleção `usuarios` — contar documentos exigiria
  ler a coleção inteira, e cada pessoa só enxerga o próprio perfil. Gravado em
  `usuarios/{uid}.numeroJogador`.
- Regra nova no `firestore.rules` para `contadores/jogadores`: quem está logado pode ler e somar
  um, mas não escolher o valor, não diminuir e não acrescentar outros campos.
- Indicador de espera no botão "Começar" da criação do personagem, que agora aguarda o contador.

### Notas
- A abertura roda uma vez, na sessão em que o personagem é criado. Quem entra noutro aparelho vai
  direto para o app; se o aplicativo for fechado no meio da abertura, ela não volta.
- Sem internet a transação falha, o jogador fica sem número e a abertura mostra só o nome. A
  espera é limitada a 8 segundos para a tela não travar.
- `versionCode` e `versionName` do `build.gradle.kts` estavam parados em 1 e "0.1"; agora
  acompanham a versão (8 e "1.8.0").

## [1.7.0] - 2026-10-27

Leitura mais limpa. A tela da Bíblia tinha cerca de 270dp de moldura — mais de um terço do
aparelho — antes do primeiro versículo.

### Alterado
- **O topo recolhe ao descer.** Rolando para baixo, o cabeçalho vira uma linha de 48dp com a
  abreviação do livro, a barra, o tempo e o play. Ao subir, ou no começo do capítulo, ele volta
  inteiro. A lupa e o marcador são navegação e anotação: não precisam ficar à mão enquanto se lê.
- **Quatro linhas no topo viraram duas.** Saiu o subtítulo "Toque para escolher outro capítulo"
  (a lupa já diz que dá para buscar) e o rótulo "LENDO/PARADO" (o botão já mostra o estado pela
  cor). O tempo e a meta viraram uma coisa só ao lado da barra: `3:05 / 10 min`. Cumprida a meta,
  a barra fica verde e o texto passa a `12:30 · +1 XP/min`, que é o que ainda se ganha lendo mais.
- **Rodapé com os três controles iguais.** Antes eram três tratamentos lado a lado: setas soltas,
  um quadrado com borda e um texto. Agora os três são o mesmo círculo de 40dp (`BotaoRedondo`).
  A linha dura de 1dp saiu — a lista já esmaece no fim. Altura: 64dp → 52dp.
- O título do capítulo continua abrindo o índice; a abreviação do cabeçalho fino também.

### Adicionado
- `BotaoRedondo` em `componentes`, o círculo usado no rodapé e no play do cabeçalho fino.

### Notas
- Somando o topo e o rodapé, sobram cerca de 120dp — umas quatro linhas de versículo a mais.
- A rolagem é observada por `snapshotFlow`, não por recomposição a cada pixel, e movimentos
  menores que 24px são ignorados para o cabeçalho não piscar com o dedo parado.

## [1.6.0] - 2026-10-27

Busca de passagem na Bíblia e retomada da leitura de onde parou.

### Adicionado
- **Busca no índice da Bíblia**: um campo no topo da folha filtra os livros enquanto você
  digita. Aceita o jeito que a gente escreve de verdade — "1joao", "1 João", "1João 3",
  "Jo 3:16", "SALMO 119" — sem depender de acento, de maiúscula ou de espaço depois do
  número do livro.
- Quando a busca já traz o capítulo ("1João 3"), tocar no livro abre direto nele, sem passar
  pela grade de capítulos. Com versículo ("Jo 3:16"), a lista ainda rola até ele.
- **Retoma de onde parou**: o livro e o capítulo abertos ficam guardados no aparelho, e a
  Bíblia reabre naquele ponto em vez de Gênesis 1. Na primeira vez continua em Gênesis 1.
- **Botão de lupa** no cabeçalho da leitura, à esquerda do botão de grifos: abre o índice já
  com o campo de busca focado e o teclado aberto. O título do capítulo continua abrindo o
  índice sem focar o campo.
- Testes da busca em `BuscaLivroTest`: normalização de acentos, leitura da referência e
  ordem dos resultados.

### Alterado
- O cabeçalho "CAPÍTULO DE ..." agora volta para a lista de livros quando tocado. Antes, depois
  de escolher um livro só dava para voltar fechando a folha.
- A busca só procura nomes de livros, não palavras dentro dos versículos.
- O nome do capítulo no cabeçalho passou a ter no máximo duas linhas. Com três botões ao lado
  sobra menos largura, e "Cântico dos Cânticos 8" empurraria o texto da leitura para baixo.

### Desempenho
- Os nomes dos 66 livros são normalizados uma vez, quando a lista carrega, e não 132 vezes a
  cada tecla digitada: filtrar ficou ~25x mais barato (0,44 ms → 0,02 ms por palavra digitada,
  medido fora do aparelho). Nunca chegaria a travar, mas era trabalho jogado fora a cada tecla.
- O salto até o versículo da busca é direto, não animado: em Salmo 119 a animação percorreria
  os 176 versículos até chegar lá.
- A rolagem observa só o versículo alvo. Antes observava a lista inteira de versículos, que a
  tela compara a cada tique do cronômetro.

## [1.5.0] - 2026-10-27

Login por e-mail e senha, e personagem com nome e classe. Primeira parte da virada do app
para vários usuários: antes o personagem era um só, fixo no aparelho.

### Adicionado
- **Login e cadastro por e-mail e senha** (Firebase Auth). O token de acesso é emitido e
  renovado pelo próprio Firebase a cada login; `AutenticacaoRepositorio.token()` lê o token
  atual para quando for preciso chamar algum serviço de fora.
- **Criação do personagem**: nome e classe inicial, na primeira entrada depois do cadastro.
  As classes oferecidas são o Aprendiz e as quatro puras; as híbridas e o Mestre seguem sendo
  conquistadas pelo motor.
- **Perfil na nuvem** (Firestore, coleção `usuarios`): nome do personagem, classe escolhida e
  o campo `admin`. O app nunca escreve `admin` — quem dá esse acesso é o console do Firebase.
- `firestore.rules` com as regras de acesso: cada pessoa só lê e escreve o que é dela.
- Testes da classe escolhida em `ClasseEscolhidaTest`.

### Alterado
- `Status` ganhou `classeEscolhida`, e `MotorPontuacao.atualizarClasse` passou a respeitá-la:
  enquanto os atributos estão parecidos (o que o cálculo lê como Aprendiz), vale a escolha da
  criação. Quando os atributos apontam uma classe de verdade, o motor assume como antes.
- Banco local na versão 3 (migração 2 → 3: nova coluna `classeEscolhida` em `status`).

### A fazer nesta linha
- Separar o banco local por usuário (`usuarioId` em `status`, `registro`, `dia_fechado` e
  `progresso_exercicio`). Hoje dois usuários no mesmo aparelho compartilhariam o progresso.
- Lembretes e exercícios criados pelo usuário; hábitos fixos do ADM separados dos do usuário comum.

## [1.4.2] - 2026-10-20

### Corrigido
- Notificações: o ícone pequeno era o desenho do launcher (escudo minúsculo no meio de um quadro de 108dp) e aparecia como uma mancha branca. Agora há um ícone próprio, com o escudo ocupando o quadro, e um **ícone grande com fundo preto** e o escudo neon, nas notificações de lembrete e do versículo. O Android desenha o ícone pequeno da barra de status sempre como silhueta, então o fundo preto só vale no ícone grande.

## [1.4.1] - 2026-10-20

Desempenho: ajustes depois de uma auditoria do "app travando e lento". Nada disso foi medido em aparelho; são correções de causas encontradas na leitura do código.

### Alterado
- Trocar de aba ficou mais rápido: o padrão do `NavHost` é um fade de 700 ms com as duas telas desenhadas ao mesmo tempo; agora o fade dura 120 ms.
- Leitura: o esmaecimento das pontas da lista deixou de ser invalidado a cada segundo do cronômetro, e o gradiente passou a ser criado uma vez por tamanho (o visual é o mesmo).
- O botão "Ler" não abre a Bíblia duas vezes se for tocado em sequência.

### Adicionado
- Variante de build `otimizado` (R8 ligado, assinada com a chave de debug) para medir desempenho de verdade: `./gradlew installOtimizado`, ou escolher "otimizado" em *Build Variants*. O APK de debug roda o Compose bem mais devagar e não representa o app final. Faça um teste rápido das 3 abas, da Bíblia (ler e grifar) e dos lembretes nessa variante.

## [1.4.0] - 2026-10-20

Tamanho do texto e zoom na leitura da Bíblia.

### Adicionado
- Botão "Aa" na barra inferior da leitura (ao lado do nome da versão) abre a folha **Tamanho do texto**: botões A− e A+, controle deslizante, prévia ao vivo, o tamanho em porcentagem (100% = padrão) e "Voltar ao padrão".
- **Zoom por pinça**: aproxime ou afaste dois dedos sobre o texto. Com um dedo, rolar e selecionar versículos continuam como antes.
- O tamanho escolhido fica salvo no aparelho e vale na próxima vez que abrir a leitura (faixa de 14 a 34 sp, padrão 18 sp).
- O número do versículo e a entrelinha acompanham o tamanho do texto.
- Teste das regras do tamanho (faixa, passos, arredondamento).

### Corrigido
- A coluna do número do versículo tinha largura fixa, pensada para 2 dígitos, e podia quebrar o número dos versículos 100 a 176 do Salmo 119; agora ela se ajusta ao maior versículo do capítulo.

## [1.3.0] - 2026-10-20

Grifos (marca-texto) na leitura da Bíblia.

### Adicionado
- Tocar em um ou mais versículos seleciona; uma barra inferior oferece 5 cores (amarelo, verde, azul, rosa e laranja), a borracha para remover o grifo e o botão para cancelar a seleção.
- Versículos grifados ficam com fundo translúcido na cor escolhida; os grifos ficam salvos no celular.
- Botão "Meus grifos" no cabeçalho da leitura: lista todos os versículos grifados (mais recentes primeiro), com filtro por cor; tocar em um leva ao capítulo.
- Banco de dados na versão 2, com migração automática que só cria a tabela `grifo` (nenhum dado existente é alterado).
- Teste que protege a ordem das cores (o banco guarda a posição da cor).

## [1.2.0] - 2026-10-20

Redesign visual: telas mais limpas e minimalistas, sem alterar nenhuma regra do app.

### Alterado
- Novo tema escuro: fundo ardósia `#0B0E14`, cards `#131824` com bordas quase invisíveis, azul de destaque `#5AA7FF` e verde `#3DDC97` para itens concluídos.
- Títulos de tela em caixa normal, sem espaçamento largo entre as letras; rótulos em caixa normal.
- Cards com cantos de 20dp, botões de 14dp e áreas de toque de pelo menos 44dp.
- **Status:** selo de rank ao lado do nome, etiqueta com o material do rank, atributos com ícone em quadradinho azul suave e radar com preenchimento mais leve.
- **Lembretes:** cards sem borda verde; círculo de 32dp (verde com check quando concluído, apenas contorno quando pendente); barra do dia fica verde ao fechar 3/3.
- **Treinos:** botões de série maiores (48dp) e distribuídos na largura, botão inferior de 56dp e fade no fim da lista.
- **Leitura:** cronômetro numa linha só (sem card), texto maior com mais respiro entre versículos, rodapé com anterior/próximo e versão da Bíblia.
- **Seletor de livros:** folha com cantos superiores de 24dp, linhas de 48dp com divisórias e seta.
- Nova barra de navegação inferior, com indicador fino no item ativo.
- Fontes mantidas: Chakra Petch (títulos e números) e IBM Plex Sans (texto).

### Mantido
- Regras de XP e atributos (`Regras.kt`), banco Room, notificações, agendamentos e divisão de ganhos da leitura.
- Escala do radar (piso 500) e animações reduzidas dos selos de rank.

## [1.1.0]

- Divisão dos ganhos de leitura (10 min: 0,10 Espírito + 0,05 Inteligência; 11+ min: 0,10 Espírito + 0,10 Inteligência).
- Piso do gráfico radar de 100 para 500.
- Criação de `ROADMAP.md` e `VERSION.txt`.
