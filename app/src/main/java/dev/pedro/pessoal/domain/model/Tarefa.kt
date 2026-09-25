package dev.pedro.pessoal.domain.model

data class Recompensa(val xp: Int, val atributo: Atributo? = null, val ganho: Int = 0)

/** Tipos de tarefa e o que cada uma rende ao ser concluída. */
enum class TipoTarefa(val recompensa: Recompensa) {
    ORACAO(Recompensa(xp = 10, atributo = Atributo.ESPIRITO, ganho = 10)),
    KARATE(Recompensa(xp = 25, atributo = Atributo.FORCA, ganho = 15)),
    TREINO_CASA(Recompensa(xp = 20, atributo = Atributo.FORCA, ganho = 15)),
    MOBILIDADE(Recompensa(xp = 10, atributo = Atributo.FORCA, ganho = 5)),
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
