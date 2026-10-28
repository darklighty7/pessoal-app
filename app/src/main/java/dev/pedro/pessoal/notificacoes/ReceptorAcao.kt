package dev.pedro.pessoal.notificacoes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.pedro.pessoal.data.AppDatabase
import dev.pedro.pessoal.data.Repositorio
import dev.pedro.pessoal.domain.model.Habito
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Trata o toque nos botões da notificação: marca o hábito no banco sem abrir o app.
 * A leitura da Bíblia é a exceção, porque ela é paga por tempo; ali o botão só abre o app.
 */
class ReceptorAcao : BroadcastReceiver() {
    override fun onReceive(contexto: Context, intencao: Intent) {
        if (intencao.action != Acoes.MARCAR) return
        val habito = intencao.getStringExtra(Acoes.EXTRA_HABITO)
            ?.let { runCatching { Habito.valueOf(it) }.getOrNull() } ?: return
        val idNotificacao = intencao.getIntExtra(Acoes.EXTRA_ID, -1)

        // O broadcast termina antes da gravação; goAsync segura o processo até acabar.
        val pendente = goAsync()
        val aplicacao = contexto.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (habito != Habito.BIBLIA) {
                    val banco = AppDatabase.obter(aplicacao)
                    Repositorio(banco.statusDao(), banco.registroDao(), banco.itensDao())
                        .marcar(habito, LocalDate.now(), concluido = true)
                }
                if (idNotificacao >= 0) Notificacoes.fechar(aplicacao, idNotificacao)
            } finally {
                pendente.finish()
            }
        }
    }
}
