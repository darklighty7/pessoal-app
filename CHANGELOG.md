# Changelog

Formato baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e versionamento semântico.

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
- Ícone de lupa em `Icones`.
- Testes da busca em `BuscaLivroTest`: normalização de acentos, leitura da referência e
  ordem dos resultados.

### Alterado
- O cabeçalho "CAPÍTULO DE ..." agora volta para a lista de livros quando tocado. Antes, depois
  de escolher um livro só dava para voltar fechando a folha.
- A busca só procura nomes de livros, não palavras dentro dos versículos.

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
