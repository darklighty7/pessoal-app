# Roadmap

Melhorias futuras para o app Forja, sem compromisso de prazo.

## Concluído

- v1.9.0: nome final (Forja), ícone, letreiro, brasa e splash.
- v1.4.2: ícones das notificações (pequeno próprio e grande com fundo preto).
- v1.4.1: ajustes de desempenho (transição entre abas, esmaecimento da lista) e variante de build `otimizado`.
- v1.4.0: tamanho do texto e zoom por pinça na leitura da Bíblia.
- v1.3.0: grifos (marca-texto) nos versículos, com 5 cores e lista de grifos.
- v1.2.0: redesign visual das telas (Status, Lembretes, Treinos, Leitura, seletor de livros e barra de navegação).

## Pendências conhecidas

- Desempenho: a auditoria da v1.4.1 não achou trabalho pesado na thread principal. O maior fator provável é rodar o APK de debug; medir de novo com a variante `otimizado`. Ficaram de fora, por impacto baixo ou sem medição:
  - `SeloRank.kt` refaz caminhos e gradientes a cada frame (hoistar os `Path` com `drawWithCache`; as seis animações infinitas rodam mesmo nos ranks que usam só duas).
  - O cronômetro da leitura emite um `EstadoBiblia` novo a cada segundo (tirar `segundos` do estado e coletar em um painel próprio).
  - `MainActivity.onCreate` faz cerca de 17 chamadas de Binder (canais e 5 alarmes) antes do primeiro frame; mover para fora da thread principal.
  - Estado inicial "falso" (nível 1, tudo pendente) até o banco responder na abertura do app.
- A importação da Bíblia não é atômica: se o app fechar no meio, `COUNT(*) > 0` marca como pronta uma Bíblia incompleta. Envolver livros e versículos em uma única transação.
- Possível: ao trocar de capítulo a lista pode abrir na mesma posição de rolagem (a chave dos itens é só o número do versículo); rolar ao topo ao mudar de capítulo.
- Compilar e testar no celular o redesign v1.2.0, os grifos v1.3.0 e o tamanho/zoom do texto v1.4.0 e os ajustes de desempenho v1.4.1, e ajustar detalhes (nada disso foi compilado nem testado antes do pull request).

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
