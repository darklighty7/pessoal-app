# Changelog

Formato baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e versionamento semântico.

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
