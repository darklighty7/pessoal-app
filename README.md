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

## Publicação

O `release` sai minificado pelo R8 (`isMinifyEnabled` e `isShrinkResources`), com as regras de
`app/proguard-rules.pro`. O `otimizado` é esse mesmo release assinado com a chave de debug, para
instalar no celular sem a chave de publicação em mãos: `./gradlew installOtimizado`. Serve de
ensaio do app final — o APK de debug roda o Compose bem mais devagar e não representa o que vai
para a loja.

A chave de publicação **não vai para o repositório**, e nem o keystore nem as senhas estão aqui.
Gere a sua uma vez, de preferência fora da pasta do projeto (o repositório é público):

```
keytool -genkeypair -v -keystore ~/chaves/forja-release.jks -alias forja \
  -keyalg RSA -keysize 2048 -validity 10000
```

O `keytool` pergunta uma senha para o keystore, outra para a chave e os seus dados (nome,
organização, país). Guarde o arquivo e as senhas: **perder a chave impede atualizar o app na
Play** — o Google exige que toda versão nova venha assinada com a mesma chave.

Depois aponte para ela em `local.properties`, do mesmo jeito que a `YVP_APP_KEY`:

```
RELEASE_STORE_FILE=/home/voce/chaves/forja-release.jks
RELEASE_STORE_PASSWORD=a-senha-do-keystore
RELEASE_KEY_ALIAS=forja
RELEASE_KEY_PASSWORD=a-senha-da-chave
```

O caminho pode ser absoluto ou relativo à raiz do projeto. Faltando qualquer uma das quatro
propriedades, o `release` continua compilando, só sai **sem assinatura** — quem clona o
repositório apenas para compilar não precisa de chave nenhuma, e o APK sem assinatura não
instala no aparelho nem sobe para a loja.

Com a chave no lugar, o pacote da Play é o `bundleRelease`:

```
./gradlew bundleRelease
```

Antes de publicar, vale rodar a conferência completa:

```
./gradlew clean testDebugUnitTest assembleDebug lintRelease assembleRelease
```

## Testes

Clique com o botão direito em `app/src/test/.../MotorPontuacaoTest.kt` → Run.

Fontes sob licença SIL Open Font License.
