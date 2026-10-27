# Pessoal

App Android pessoal de melhoria de hábitos, estilo RPG. Kotlin + Jetpack Compose.

## Estrutura

- `domain/model` — Atributo, Classe, Rank, Título, Status, tarefas
- `domain/engine/Regras.kt` — todos os números do sistema (XP, ganhos, perdas, bônus)
- `domain/engine/MotorPontuacao.kt` — regras de evolução (funções puras, testáveis)
- `ui/theme` — cores neon, fontes Chakra Petch e IBM Plex Sans
- `ui/status` — tela de Status e gráfico em losango
- `ui/navegacao` — `Raiz` (login, criação do personagem ou app) e a barra inferior
- `ui/auth` — telas de login e de criação do personagem
- `ui/biblia` — leitura, grifos e `BuscaLivro` (busca de passagem por nome do livro)
- `data/auth` e `data/nuvem` — Firebase Auth e perfil do personagem no Firestore

## Firebase

O arquivo `app/google-services.json` **não vai para o repositório**: ele carrega as chaves do
projeto e cada um usa o seu. Para rodar o app, baixe o seu no console do Firebase e coloque em
`app/`. As regras do Firestore estão em `firestore.rules`.

## Testes

Clique com o botão direito em `app/src/test/.../MotorPontuacaoTest.kt` → Run.

Fontes sob licença SIL Open Font License.
