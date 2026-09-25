package dev.pedro.pessoal.notificacoes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dev.pedro.pessoal.domain.model.Habito
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/** Os avisos do dia e a que horas cada um toca. */
enum class Alarme(val id: Int, val hora: LocalTime) {
    /** Bíblia e oração, com botão para cada um. */
    MANHA(Notificacoes.ID_MANHA, LocalTime.of(7, 30)),
    /** Lembrete de sair para o karatê. */
    KARATE_AVISO(Notificacoes.ID_KARATE_AVISO, LocalTime.of(18, 30)),
    /** Confirmação depois da aula. */
    KARATE_CONFIRMA(Notificacoes.ID_KARATE_CONFIRMA, LocalTime.of(20, 5)),
    /** Treino em casa, ou mobilidade no sábado. */
    TREINO(Notificacoes.ID_TREINO, LocalTime.of(19, 30)),
    /** Versículo fixo na tela de bloqueio. */
    VERSICULO(Notificacoes.ID_VERSICULO, LocalTime.of(6, 0));

    companion object {
        fun porId(id: Int): Alarme? = entries.firstOrNull { it.id == id }
    }
}

/**
 * Agenda os alarmes diários. Cada alarme é reagendado para o dia seguinte assim que
 * dispara, então basta chamar [agendarTodos] quando o app abre e depois de reiniciar.
 */
object Agendador {

    fun agendarTodos(contexto: Context) {
        Alarme.entries.forEach { agendar(contexto, it) }
    }

    fun agendar(contexto: Context, alarme: Alarme, apartirDe: LocalDateTime = LocalDateTime.now()) {
        val gerente = contexto.getSystemService(AlarmManager::class.java) ?: return
        val quando = proximaOcorrencia(alarme, apartirDe)
        val momento = quando.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val intencao = pendente(contexto, alarme)

        // Alarme exato só quando o sistema permite; senão cai no modo aproximado,
        // que pode atrasar alguns minutos mas não exige permissão especial.
        val exato = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || gerente.canScheduleExactAlarms()
        if (exato) {
            gerente.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momento, intencao)
        } else {
            gerente.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momento, intencao)
        }
    }

    /**
     * O próximo dia em que esse alarme faz sentido: pula os dias em que o hábito
     * correspondente não está programado (karatê só terça e quinta, por exemplo).
     */
    private fun proximaOcorrencia(alarme: Alarme, agora: LocalDateTime): LocalDateTime {
        var dia: LocalDate = agora.toLocalDate()
        if (agora.toLocalTime() >= alarme.hora) dia = dia.plusDays(1)
        repeat(8) {
            if (valeNoDia(alarme, dia)) return LocalDateTime.of(dia, alarme.hora)
            dia = dia.plusDays(1)
        }
        return LocalDateTime.of(dia, alarme.hora)
    }

    private fun valeNoDia(alarme: Alarme, dia: LocalDate): Boolean = when (alarme) {
        Alarme.MANHA, Alarme.VERSICULO -> true
        Alarme.KARATE_AVISO, Alarme.KARATE_CONFIRMA -> Habito.KARATE.ocorreEm(dia)
        Alarme.TREINO -> Habito.TREINO_CASA.ocorreEm(dia) || Habito.MOBILIDADE.ocorreEm(dia)
    }

    private fun pendente(contexto: Context, alarme: Alarme): PendingIntent {
        val intencao = Intent(contexto, ReceptorAlarme::class.java).apply {
            putExtra(Acoes.EXTRA_ID, alarme.id)
        }
        return PendingIntent.getBroadcast(
            contexto,
            alarme.id,
            intencao,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
