package dev.pedro.pessoal.notificacoes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dev.pedro.pessoal.domain.engine.Regras
import dev.pedro.pessoal.domain.model.Habito
import dev.pedro.pessoal.domain.model.LembretePessoal
import dev.pedro.pessoal.domain.model.paraTexto
import java.time.DayOfWeek
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

    /**
     * Os alarmes dos lembretes criados usam id a partir de [BASE_PESSOAL], bem acima dos
     * cinco fixos (1 a 4 e 10), para um id de linha do banco nunca sobrescrever o alarme
     * da manhã ou o do versículo.
     */
    const val BASE_PESSOAL = 1000

    const val EXTRA_TITULO = "titulo"
    const val EXTRA_TEXTO = "texto"
    const val EXTRA_DIAS = "dias"
    const val EXTRA_MINUTO = "minuto"

    fun agendarTodos(contexto: Context) {
        Alarme.entries.forEach { agendar(contexto, it) }
    }

    /**
     * Agenda (ou reagenda) os lembretes que a pessoa criou. Chamado quando a lista muda e
     * quando o telefone reinicia.
     *
     * O que a notificação precisa dizer vai dentro da própria intenção — título, texto, dias
     * e horário. Assim o receptor mostra o aviso e já marca o próximo sem abrir o banco, que
     * é trabalho pesado demais para um BroadcastReceiver.
     */
    fun agendarPessoais(contexto: Context, lembretes: List<LembretePessoal>) {
        lembretes.forEach { agendarPessoal(contexto, it) }
    }

    fun agendarPessoal(
        contexto: Context,
        lembrete: LembretePessoal,
        apartirDe: LocalDateTime = LocalDateTime.now(),
    ) = marcarPessoal(
        contexto = contexto,
        id = idDe(lembrete),
        titulo = lembrete.titulo,
        texto = textoDe(lembrete),
        dias = lembrete.dias,
        hora = lembrete.hora,
        apartirDe = apartirDe,
    )

    /**
     * Reagenda a partir do que veio dentro da intenção, sem abrir o banco. É o que o
     * receptor usa depois de mostrar o aviso.
     */
    fun reagendarPessoal(
        contexto: Context,
        id: Int,
        titulo: String,
        texto: String,
        dias: Set<DayOfWeek>,
        minutoDoDia: Int,
        apartirDe: LocalDateTime = LocalDateTime.now(),
    ) = marcarPessoal(
        contexto = contexto,
        id = id,
        titulo = titulo,
        texto = texto,
        dias = dias,
        hora = LocalTime.of(minutoDoDia / 60, minutoDoDia % 60),
        apartirDe = apartirDe,
    )

    private fun marcarPessoal(
        contexto: Context,
        id: Int,
        titulo: String,
        texto: String,
        dias: Set<DayOfWeek>,
        hora: LocalTime,
        apartirDe: LocalDateTime,
    ) {
        if (dias.isEmpty()) return
        val gerente = contexto.getSystemService(AlarmManager::class.java) ?: return
        val aviso = hora.minusMinutes(Regras.AVISO_ANTES_MIN)

        // O próximo dia em que este lembrete acontece. Passada a hora do aviso, começa de
        // amanhã, senão o alarme seria marcado para um instante que já ficou para trás.
        var alvo: LocalDate = apartirDe.toLocalDate()
        if (apartirDe.toLocalTime() >= aviso) alvo = alvo.plusDays(1)
        var voltas = 0
        while (alvo.dayOfWeek !in dias && voltas++ < 8) alvo = alvo.plusDays(1)

        val momento = LocalDateTime.of(alvo, aviso)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // O que a notificação precisa dizer viaja dentro da intenção. Assim o receptor
        // mostra o aviso e já marca o próximo sem abrir o banco, que é trabalho pesado
        // demais para um BroadcastReceiver.
        val intencao = Intent(contexto, ReceptorAlarme::class.java).apply {
            putExtra(Acoes.EXTRA_ID, id)
            putExtra(EXTRA_TITULO, titulo)
            putExtra(EXTRA_TEXTO, texto)
            putExtra(EXTRA_DIAS, dias.paraTexto())
            putExtra(EXTRA_MINUTO, hora.hour * 60 + hora.minute)
        }
        val pendente = PendingIntent.getBroadcast(
            contexto,
            id,
            intencao,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        despertar(gerente, momento, pendente)
    }

    fun cancelarPessoal(contexto: Context, id: Long) {
        val gerente = contexto.getSystemService(AlarmManager::class.java) ?: return
        val pendente = PendingIntent.getBroadcast(
            contexto,
            BASE_PESSOAL + id.toInt(),
            Intent(contexto, ReceptorAlarme::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        gerente.cancel(pendente)
        pendente.cancel()
    }

    private fun idDe(lembrete: LembretePessoal) = BASE_PESSOAL + lembrete.id.toInt()

    private fun textoDe(lembrete: LembretePessoal): String {
        val hora = "%02d:%02d".format(lembrete.hora.hour, lembrete.hora.minute)
        return if (lembrete.descricao.isBlank()) "Às $hora." else "Às $hora. " + lembrete.descricao
    }

    fun agendar(contexto: Context, alarme: Alarme, apartirDe: LocalDateTime = LocalDateTime.now()) {
        val gerente = contexto.getSystemService(AlarmManager::class.java) ?: return
        val quando = proximaOcorrencia(alarme, apartirDe)
        val momento = quando.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val intencao = pendente(contexto, alarme)

        despertar(gerente, momento, intencao)
    }

    /**
     * Alarme exato só quando o sistema permite; senão cai no modo aproximado, que pode
     * atrasar alguns minutos mas não exige permissão especial.
     */
    private fun despertar(gerente: AlarmManager, momento: Long, intencao: PendingIntent) {
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
