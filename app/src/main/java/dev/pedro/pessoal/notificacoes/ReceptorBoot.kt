package dev.pedro.pessoal.notificacoes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Reagenda tudo depois que o telefone reinicia, senão os alarmes se perdem. */
class ReceptorBoot : BroadcastReceiver() {
    override fun onReceive(contexto: Context, intencao: Intent) {
        if (intencao.action != Intent.ACTION_BOOT_COMPLETED) return
        Notificacoes.criarCanais(contexto)
        Agendador.agendarTodos(contexto)
    }
}
