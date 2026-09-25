package dev.pedro.pessoal.notificacoes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.pedro.pessoal.domain.model.Habito
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Recebe o alarme, mostra a notificação certa e já reagenda para a próxima vez.
 */
class ReceptorAlarme : BroadcastReceiver() {
    override fun onReceive(contexto: Context, intencao: Intent) {
        val alarme = Alarme.porId(intencao.getIntExtra(Acoes.EXTRA_ID, -1)) ?: return
        Notificacoes.criarCanais(contexto)
        val hoje = LocalDate.now()

        when (alarme) {
            Alarme.MANHA -> Notificacoes.lembrete(
                contexto,
                alarme.id,
                titulo = "Bom dia, Pedro",
                texto = "Bíblia e oração para começar o dia.",
                habitos = listOf(Habito.BIBLIA, Habito.ORACAO),
            )

            Alarme.KARATE_AVISO -> Notificacoes.lembrete(
                contexto,
                alarme.id,
                titulo = "Karatê hoje",
                texto = "A aula começa às 19h. Hora de se arrumar.",
                habitos = emptyList(),
            )

            Alarme.KARATE_CONFIRMA -> Notificacoes.lembrete(
                contexto,
                alarme.id,
                titulo = "Como foi a aula?",
                texto = "Marque para somar os pontos do karatê.",
                habitos = listOf(Habito.KARATE),
            )

            Alarme.TREINO -> {
                val habito = if (Habito.TREINO_CASA.ocorreEm(hoje)) Habito.TREINO_CASA else Habito.MOBILIDADE
                Notificacoes.lembrete(
                    contexto,
                    alarme.id,
                    titulo = habito.rotulo,
                    texto = "Abra o app para seguir as séries de hoje.",
                    habitos = listOf(habito),
                )
            }

            Alarme.VERSICULO -> {
                val (texto, referencia) = Versiculos.doDia(hoje)
                Notificacoes.versiculo(contexto, texto, referencia)
            }
        }

        // Reagenda a partir de um minuto depois, para não pegar o próprio horário de novo.
        Agendador.agendar(contexto, alarme, LocalDateTime.now().plusMinutes(1))
    }
}
