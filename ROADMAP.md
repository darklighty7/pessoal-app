# Roadmap

Melhorias futuras para o app Pessoal, sem compromisso de prazo.

## Pendências conhecidas

- Corrigir o typo "foii" em Lucas 22:39: não foi encontrado no código-fonte, porque o
  texto da Bíblia não é versionado no repositório — é baixado em runtime a partir do
  JSON do projeto `thiagobodruk/biblia` (ver `BibliaRepositorio.kt`). Se o typo existir,
  provavelmente está na fonte remota ou em um banco já baixado no celular. Precisa
  localizar o versículo real (app ou fonte remota) antes de poder corrigir.

## Ideias futuras

- Novos hábitos e tarefas além de oração, leitura e treino.
- Mais classes híbridas conforme combinações de atributos forem surgindo no uso real.
- Ajustar constantes de `Regras.kt` depois de algumas semanas de uso real.
- Histórico/gráfico de evolução dos atributos ao longo do tempo, não só o estado atual.
- Backup/exportação do banco de dados (Status, registros e progresso de treino).
- Suporte a mais de uma versão da Bíblia (hoje só NVI).
