# Forja

App Android pessoal de melhoria de hábitos, estilo RPG. Kotlin + Jetpack Compose.

A marca é um "F" cheio e inclinado, em degradê do verde ao azul, com uma fagulha — o
losango verde — adiante da barra do meio. O mesmo losango marca a ponta da barra de XP.
A palavra "forja" é texto em Chakra Petch, não desenho: veja `ui/componentes/MarcaForja.kt`.
O `applicationId` continua `dev.pedro.pessoal`: mudar isso faria o Android tratar como
outro app.

## Estrutura

- `domain/model` — Atributo, Classe, Rank, Título, Status, tarefas
- `domain/engine/Regras.kt` — todos os números do sistema (XP, ganhos, perdas, bônus)
- `domain/engine/MotorPontuacao.kt` — regras de evolução (funções puras, testáveis)
- `ui/theme` — paleta da marca, fontes Chakra Petch e IBM Plex Sans
- `ui/componentes/Brasa.kt` — o losango da marca, aceso ou apagado
- `ui/componentes/MarcaForja.kt` — o F mais a palavra, para as telas de entrada
- `ui/status` — tela de Status e gráfico em losango
- `ui/navegacao` — `Raiz` (login, criação do personagem ou app) e a barra inferior
- `ui/auth` — login, nome do personagem e escolha da classe (três passos, nesta ordem)
- `ui/itens` — formulários dos lembretes e exercícios que a pessoa cria
- `ui/config` — configurações, com a seção da conta e a seção só do administrador
- `ui/biblia` — leitura, grifos e `BuscaLivro` (busca de passagem por nome do livro)
- `ui/boasvindas` — a abertura que roda uma vez, logo depois de criar o personagem
- `data/auth` e `data/nuvem` — Firebase Auth, perfil do personagem e contador de jogadores no Firestore

## Firebase

O arquivo `app/google-services.json` **não vai para o repositório**: ele carrega as chaves do
projeto e cada um usa o seu. Para rodar o app, baixe o seu no console do Firebase e coloque em
`app/`. As regras do Firestore estão em `firestore.rules` — republique sempre que o arquivo mudar.

## Bíblia (YouVersion)

O texto da NVI vem da YouVersion Platform, sob a licença Fast-track da Biblica. O app não guarda
texto bíblico: só a estrutura (livros, código USFM e quantos versículos cada capítulo tem). A
chave do app na plataforma **não vai para o repositório** — ponha a sua em `local.properties`,
que também fica fora do Git:

```
YVP_APP_KEY=a-sua-chave
```

Sem ela o app compila e abre, mas o leitor não carrega o texto: a preparação para em "Falta a
chave da YouVersion". O `local.properties` precisa ainda do `sdk.dir`, que o Android Studio
escreve sozinho ao abrir o projeto; num clone novo pela linha de comando ele não existe e o
Gradle para em "SDK location not found".

## Testes

Clique com o botão direito em `app/src/test/.../MotorPontuacaoTest.kt` → Run.

Fontes sob licença SIL Open Font License.
