package dev.pedro.pessoal.notificacoes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.pedro.pessoal.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Reagenda tudo depois que o telefone reinicia, senão os alarmes se perdem. */
class ReceptorBoot : BroadcastReceiver() {
    override fun onReceive(contexto: Context, intencao: Intent) {
        if (intencao.action != Intent.ACTION_BOOT_COMPLETED) return
        Notificacoes.criarCanais(contexto)
        Agendador.agendarTodos(contexto)

        // Os lembretes criados pela pessoa moram no banco, e ler banco leva tempo demais
        // para o prazo de um BroadcastReceiver. O goAsync segura o processo vivo enquanto
        // a leitura acontece fora da thread principal.
        val resultado = goAsync()
        val aplicacao = contexto.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val lembretes = AppDatabase.obter(aplicacao).itensDao().todosOsLembretes()
                // Reagenda os de todas as contas: depois do boot ninguém entrou ainda, e um
                // alarme a mais de uma conta que não está em uso é melhor que um a menos.
                Agendador.agendarPessoais(aplicacao, lembretes.map { it.paraDominio() })
            } finally {
                resultado.finish()
            }
        }
    }
}
