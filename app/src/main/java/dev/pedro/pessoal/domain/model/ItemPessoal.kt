package dev.pedro.pessoal.domain.model

import dev.pedro.pessoal.domain.engine.Regras
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Um lembrete criado pela própria pessoa, ao lado dos hábitos fixos do [Habito].
 *
 * O aviso sai 30 minutos antes de [hora], a mesma lógica do karatê: o lembrete existe para
 * dar tempo de se preparar, não para avisar quando já é tarde.
 */
data class LembretePessoal(
    val id: Long,
    val titulo: String,
    val descricao: String,
    val hora: LocalTime,
    val dias: Set<DayOfWeek>,
    val atributo: Atributo,
) {
    /**
     * Como este lembrete aparece na tabela de registros, que guarda hábitos por nome.
     * O prefixo mantém os criados longe das constantes de [Habito] — um hábito chamado
     * "BIBLIA" e um lembrete de id 3 nunca disputam a mesma linha.
     */
    val chaveRegistro: String get() = PREFIXO + id

    fun ocorreEm(data: LocalDate): Boolean = data.dayOfWeek in dias

    val horaDoAviso: LocalTime get() = hora.minusMinutes(Regras.AVISO_ANTES_MIN)

    val recompensa: Recompensa
        get() = Recompensa(
            xp = Regras.XP_LEMBRETE_PESSOAL,
            ganhos = mapOf(atributo to Regras.GANHO_LEMBRETE_PESSOAL),
        )

    companion object {
        const val PREFIXO = "LEMBRETE_"

        /** Se a chave de registro veio de um lembrete criado, devolve o id; senão, nulo. */
        fun idDaChave(chave: String): Long? =
            if (chave.startsWith(PREFIXO)) chave.removePrefix(PREFIXO).toLongOrNull() else null
    }
}

/**
 * Um exercício criado pela própria pessoa. Mais simples que os do [Treino]: sem escada de
 * variações, porque quem escreve o próprio treino já escolhe a dificuldade que quer.
 */
data class ExercicioPessoal(
    val id: Long,
    val nome: String,
    val series: Int,
    /** Repetições por série, ou segundos quando [porTempo] é verdadeiro. */
    val repeticoes: Int,
    val porTempo: Boolean,
    val dias: Set<DayOfWeek>,
) {
    fun ocorreEm(data: LocalDate): Boolean = data.dayOfWeek in dias

    /** "3 × 12" ou "3 × 30s" */
    val resumo: String get() = "$series × $repeticoes" + if (porTempo) "s" else ""
}

/**
 * Os dias da semana viram texto no banco: "1,3,5" com os valores de [DayOfWeek], onde
 * segunda é 1 e domingo é 7. Guardar o número em vez do nome deixa o dado independente
 * do idioma do aparelho.
 */
fun Set<DayOfWeek>.paraTexto(): String = sortedBy { it.value }.joinToString(",") { it.value.toString() }

fun textoParaDias(texto: String): Set<DayOfWeek> = texto
    .split(",")
    .mapNotNull { pedaco -> pedaco.trim().toIntOrNull()?.takeIf { it in 1..7 } }
    .map { DayOfWeek.of(it) }
    .toSet()
