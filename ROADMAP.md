# Roadmap

Melhorias futuras para o app Pessoal, sem compromisso de prazo.

## Concluído

- v1.4.0: tamanho do texto e zoom por pinça na leitura da Bíblia.
- v1.3.0: grifos (marca-texto) nos versículos, com 5 cores e lista de grifos.
- v1.2.0: redesign visual das telas (Status, Lembretes, Treinos, Leitura, seletor de livros e barra de navegação).

## Pendências conhecidas

- Escolher o nome final do app e criar um ícone novo com símbolo próprio.
- Otimizar o desempenho (o app trava em alguns momentos): reduzir as animações dos selos E, D e C (`SeloRank.kt`) usando `remember` para caminhos e gradientes, e revisar recomposições.
- Testar o redesign v1.2.0 e os grifos da v1.3.0 no celular e ajustar detalhes (o build não foi validado antes do pull request).

- Corrigir o typo "foii" em Lucas 22:39: não foi encontrado no código-fonte, porque o
  texto da Bíblia não é versionado no repositório — é baixado em runtime a partir do
  JSON do projeto `thiagobodruk/biblia` (ver `BibliaRepositorio.kt`). Se o typo existir,
  provavelmente está na fonte remota ou em um banco já baixado no celular. Precisa
  localizar o versículo real (app ou fonte remota) antes de poder corrigir.

## Ideias futuras

- Grifos: anotações curtas por versículo e exportar a lista de grifos.
- Novos hábitos e tarefas além de oração, leitura e treino.
- Mais classes híbridas conforme combinações de atributos forem surgindo no uso real.
- Ajustar constantes de `Regras.kt` depois de algumas semanas de uso real.
- Histórico/gráfico de evolução dos atributos ao longo do tempo, não só o estado atual.
- Backup/exportação do banco de dados (Status, registros e progresso de treino).
- Suporte a mais de uma versão da Bíblia (hoje só NVI).
