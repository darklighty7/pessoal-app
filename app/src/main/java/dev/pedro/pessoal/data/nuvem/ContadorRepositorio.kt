package dev.pedro.pessoal.data.nuvem

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * O número do jogador, em ordem de chegada: `contadores/jogadores`.
 *
 * O número sai de uma transação, não de uma contagem da coleção `usuarios` — contar
 * documentos exigiria ler a coleção inteira, e cada pessoa só enxerga o próprio perfil.
 *
 * A transação não funciona sem internet: o Firestore precisa confirmar o valor com o
 * servidor antes de gravar. Quando falha, [proximoNumero] devolve [SEM_NUMERO] e a
 * abertura mostra só o nome do jogador.
 */
class ContadorRepositorio(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    suspend fun proximoNumero(): Int {
        val documento = db.collection(COLECAO).document(DOCUMENTO)
        return db.runTransaction { transacao ->
            val atual = transacao.get(documento)
            val total = atual.getLong(CAMPO) ?: 0L
            val novo = total + 1
            if (atual.exists()) {
                transacao.update(documento, CAMPO, novo)
            } else {
                transacao.set(documento, mapOf(CAMPO to novo))
            }
            novo.toInt()
        }.await()
    }

    companion object {
        /** Devolvido quando o contador não pôde ser lido. */
        const val SEM_NUMERO = 0

        private const val COLECAO = "contadores"
        private const val DOCUMENTO = "jogadores"
        private const val CAMPO = "total"
    }
}
