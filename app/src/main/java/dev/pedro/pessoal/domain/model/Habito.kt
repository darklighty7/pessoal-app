package dev.pedro.pessoal.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Os hábitos do dia a dia. Cada um sabe em que dias da semana aparece e a que horas
 * o lembrete toca. [obrigatorio] marca os que contam para o dia perfeito e para as
 * perdas por falhas seguidas; a mobilidade de sábado é opcional.
 */
enum class Habito(
    val rotulo: String,
    val descricao: String,
    val atributo: Atributo,
    val dias: Set<DayOfWeek>,
    val hora: LocalTime,
    val obrigatorio: Boolean = true,
    val tipo: TipoTarefa? = null,
    val doDono: Boolean = true,
) {
    BIBLIA(
        rotulo = "Ler a Bíblia",
        descricao = "10 minutos de leitura contam como meta do dia",
        atributo = Atributo.ESPIRITO,
        dias = DayOfWeek.entries.toSet(),
        hora = LocalTime.of(7, 30),
        doDono = false,
    ),
    ORACAO(
        rotulo = "Orar",
        descricao = "Momento de oração",
        atributo = Atributo.ESPIRITO,
        dias = DayOfWeek.entries.toSet(),
        hora = LocalTime.of(7, 30),
        tipo = TipoTarefa.ORACAO,
    ),
    KARATE(
        rotulo = "Aula de karatê",
        descricao = "Terças e quintas, das 19h às 20h",
        atributo = Atributo.FORCA,
        dias = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
        hora = LocalTime.of(18, 30),
        tipo = TipoTarefa.KARATE,
    ),
    TREINO_CASA(
        rotulo = "Treino em casa",
        descricao = "Segundas, quartas e sextas",
        atributo = Atributo.FORCA,
        dias = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
        hora = LocalTime.of(19, 30),
        tipo = TipoTarefa.TREINO_CASA,
    ),
    MOBILIDADE(
        rotulo = "Mobilidade",
        descricao = "Sábado, opcional",
        atributo = Atributo.FORCA,
        dias = setOf(DayOfWeek.SATURDAY),
        hora = LocalTime.of(19, 30),
        obrigatorio = false,
        tipo = TipoTarefa.MOBILIDADE,
    );

    fun ocorreEm(data: LocalDate): Boolean = data.dayOfWeek in dias

    companion object {
        /**
         * Os hábitos fixos do dia. Para o dono do app, todos; para os demais, só os que não
         * são da rotina dele — hoje, só a Bíblia.
         */
        fun doDia(
            data: LocalDate,
            admin: Boolean,
            desligados: Set<Habito> = emptySet(),
        ): List<Habito> = entries.filter {
            it.ocorreEm(data) && (admin || !it.doDono) && it !in desligados
        }
    }
}
