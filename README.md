# Pessoal

App Android pessoal de melhoria de hábitos, estilo RPG. Kotlin + Jetpack Compose.

## Estrutura

- `domain/model` — Atributo, Classe, Rank, Título, Status, tarefas
- `domain/engine/Regras.kt` — todos os números do sistema (XP, ganhos, perdas, bônus)
- `domain/engine/MotorPontuacao.kt` — regras de evolução (funções puras, testáveis)
- `ui/theme` — cores neon, fontes Chakra Petch e IBM Plex Sans
- `ui/status` — tela de Status e gráfico em losango
- `ui/navegacao` — barra inferior (Status, Lembretes, Treinos)

## Testes

Clique com o botão direito em `app/src/test/.../MotorPontuacaoTest.kt` → Run.

Fontes sob licença SIL Open Font License.
