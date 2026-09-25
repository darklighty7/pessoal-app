package dev.pedro.pessoal.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

/** Um exercício com as variações em ordem de dificuldade, da mais fácil para a mais difícil. */
data class Exercicio(
    val nome: String,
    val variacoes: List<String>,
    val series: Int,
    /** Repetições por série, ou segundos quando [porTempo] é verdadeiro. */
    val repeticoes: Int,
    val porTempo: Boolean = false,
) {
    /** A variação vale enquanto houver: passado o fim da lista, fica na última. */
    fun variacao(nivel: Int): String = variacoes[nivel.coerceIn(0, variacoes.lastIndex)]

    fun podeProgredir(nivel: Int): Boolean = nivel < variacoes.lastIndex
}

/** Os treinos que rodam em casa, só com o peso do corpo. */
enum class Treino(val rotulo: String, val dias: Set<DayOfWeek>, val exercicios: List<Exercicio>) {
    A(
        rotulo = "Treino A · Superior",
        dias = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
        exercicios = listOf(
            Exercicio("Flexão", listOf("na parede", "no joelho", "normal", "com pausa", "diamante"), series = 3, repeticoes = 10),
            Exercicio("Tríceps no banco", listOf("pernas dobradas", "pernas esticadas", "pés elevados"), series = 3, repeticoes = 10),
            Exercicio("Prancha", listOf("no joelho", "normal", "com um braço"), series = 3, repeticoes = 30, porTempo = true),
            Exercicio("Superman", listOf("só braços", "completo", "com pausa"), series = 3, repeticoes = 12),
        ),
    ),
    B(
        rotulo = "Treino B · Inferior e core",
        dias = setOf(DayOfWeek.WEDNESDAY),
        exercicios = listOf(
            Exercicio("Agachamento", listOf("na cadeira", "livre", "com pausa", "búlgaro"), series = 3, repeticoes = 15),
            Exercicio("Afundo", listOf("estático", "alternado", "caminhando"), series = 3, repeticoes = 12),
            Exercicio("Ponte de glúteo", listOf("dois pés", "com pausa", "uma perna"), series = 3, repeticoes = 15),
            Exercicio("Panturrilha", listOf("dois pés", "uma perna", "no degrau"), series = 3, repeticoes = 20),
            Exercicio("Abdominal bicicleta", listOf("lento", "normal", "com pausa"), series = 3, repeticoes = 20),
        ),
    ),
    MOBILIDADE(
        rotulo = "Mobilidade",
        dias = setOf(DayOfWeek.SATURDAY),
        exercicios = listOf(
            Exercicio("Gato e camelo", listOf("normal"), series = 2, repeticoes = 10),
            Exercicio("Alongamento de posterior", listOf("sentado", "em pé"), series = 2, repeticoes = 30, porTempo = true),
            Exercicio("Rotação de quadril", listOf("normal"), series = 2, repeticoes = 10),
            Exercicio("Abertura de peito", listOf("normal"), series = 2, repeticoes = 30, porTempo = true),
        ),
    );

    companion object {
        fun doDia(data: LocalDate): Treino? = entries.firstOrNull { data.dayOfWeek in it.dias }
    }
}

/** Depois de quantos treinos completos o app sugere subir de variação. */
const val TREINOS_PARA_PROGREDIR = 3
