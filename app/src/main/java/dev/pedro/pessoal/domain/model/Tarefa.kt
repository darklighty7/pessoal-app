package dev.pedro.pessoal.domain.model

data class Recompensa(val xp: Int, val ganhos: Map<Atributo, Int> = emptyMap())

/** Tipos de tarefa e o que cada uma rende ao ser concluída. */
enum class TipoTarefa(val recompensa: Recompensa) {
    ORACAO(Recompensa(xp = 10, ganhos = mapOf(Atributo.ESPIRITO to 10))),
    KARATE(Recompensa(xp = 25, ganhos = mapOf(Atributo.FORCA to 15))),
    TREINO_CASA(Recompensa(xp = 20, ganhos = mapOf(Atributo.FORCA to 15))),
    MOBILIDADE(Recompensa(xp = 10, ganhos = mapOf(Atributo.FORCA to 5))),
    // A leitura da Bíblia é pontuada por tempo: ver MotorPontuacao.recompensaLeitura
}

/**
 * Resultado de um hábito obrigatório ao fechar o dia.
 * [falhasSeguidas] e [sequencia] já incluem o dia que está sendo fechado.
 */
data class ResultadoHabito(
    val atributo: Atributo,
    val cumprido: Boolean,
    val falhasSeguidas: Int,
    val sequencia: Int,
)
