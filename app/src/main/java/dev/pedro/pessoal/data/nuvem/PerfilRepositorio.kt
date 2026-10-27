package dev.pedro.pessoal.data.nuvem

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dev.pedro.pessoal.domain.model.Classe
import dev.pedro.pessoal.domain.model.PerfilUsuario
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Perfil do personagem no Firestore: `usuarios/{uid}`.
 *
 * O Firestore guarda uma cópia local do que já leu, então o perfil continua aparecendo
 * sem internet; o que mudar offline sobe sozinho quando a conexão volta.
 */
class PerfilRepositorio(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    private fun documento(uid: String) = db.collection(COLECAO).document(uid)

    suspend fun buscar(uid: String): PerfilUsuario? =
        documento(uid).get().await().toObject(PerfilUsuario::class.java)

    fun observar(uid: String): Flow<PerfilUsuario?> = callbackFlow {
        val registro = documento(uid).addSnapshotListener { instantaneo, erro ->
            if (erro != null) {
                close(erro)
                return@addSnapshotListener
            }
            trySend(instantaneo?.toObject(PerfilUsuario::class.java))
        }
        awaitClose { registro.remove() }
    }

    /**
     * Primeiro passo: o nome e o número de chegada. Não mexe em `admin`: esse campo é dado
     * pelo console do Firebase, e um `set` com merge preserva o que já estiver lá.
     */
    suspend fun salvarNome(
        uid: String,
        email: String,
        nome: String,
        numeroJogador: Int = ContadorRepositorio.SEM_NUMERO,
    ) {
        val dados = buildMap {
            put("uid", uid)
            put("email", email)
            put("nomePersonagem", nome.trim())
            put("criadoEm", System.currentTimeMillis())
            // Sem número, é melhor não gravar nada: o merge preservaria um número que já
            // estivesse lá, e um zero por cima apagaria a ordem de chegada.
            if (numeroJogador != ContadorRepositorio.SEM_NUMERO) put("numeroJogador", numeroJogador)
        }
        documento(uid).set(dados, SetOptions.merge()).await()
    }

    /** Último passo da entrada: a classe escolhida depois da abertura. */
    suspend fun salvarClasse(uid: String, classe: Classe) {
        documento(uid).set(mapOf("classeInicial" to classe.name), SetOptions.merge()).await()
    }

    private companion object {
        const val COLECAO = "usuarios"
    }
}
