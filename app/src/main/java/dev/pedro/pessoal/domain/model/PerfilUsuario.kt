package dev.pedro.pessoal.domain.model

/**
 * O usuário dono do personagem, como fica gravado no Firestore (coleção `usuarios`,
 * um documento por uid do Firebase Auth).
 *
 * Os valores padrão existem porque o Firestore precisa de um construtor sem argumentos
 * para transformar o documento de volta em objeto.
 *
 * [admin] libera os hábitos fixos do dono do app (Bíblia, oração, karatê, treino e
 * mobilidade). Um usuário comum recebe só a Bíblia como fixo e cria o resto.
 * Este campo nunca é escrito pelo app: só pelo console do Firebase.
 *
 * [numeroJogador] é a ordem de chegada, usada na abertura. Zero quer dizer que o
 * contador não pôde ser lido na hora de criar a conta.
 */
data class PerfilUsuario(
    val uid: String = "",
    val email: String = "",
    val nomePersonagem: String = "",
    val classeInicial: String = Classe.APRENDIZ.name,
    val admin: Boolean = false,
    val numeroJogador: Int = 0,
    val criadoEm: Long = 0L,
) {
    val classe: Classe
        get() = runCatching { Classe.valueOf(classeInicial) }.getOrDefault(Classe.APRENDIZ)

    /** Só vale como personagem criado quando já tem nome. */
    val completo: Boolean get() = nomePersonagem.isNotBlank()
}
