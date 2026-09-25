package dev.pedro.pessoal.domain.model

import dev.pedro.pessoal.domain.engine.Regras
import java.util.Locale

/**
 * Estado completo do personagem. Imutável: o motor sempre devolve uma cópia nova.
 * Atributos em centésimos para evitar erros de ponto flutuante (0.1 + 0.2 != 0.3).
 */
data class Status(
    val nome: String = "Pedro",
    val nivel: Int = 1,
    /** XP acumulado dentro do nível atual (nunca negativo). */
    val xp: Int = 0,
    val atributos: Map<Atributo, Int> = Atributo.entries.associateWith { Regras.VALOR_INICIAL_ATRIBUTO },
    val classe: Classe = Classe.APRENDIZ,
    val titulo: Titulo = Titulo.INICIANTE,
    /** Classe que está "tentando" assumir e há quantos dias seguidos. */
    val classeCandidata: Classe? = null,
    val diasCandidata: Int = 0,
) {
    val rank: Rank get() = Rank.doNivel(nivel)
    val xpNecessario: Int get() = Regras.xpParaProximoNivel(nivel)
    fun valor(atributo: Atributo): Int = atributos.getValue(atributo)
}

private val PT_BR: Locale = Locale.forLanguageTag("pt-BR")

/** 1020 -> "10,20" */
fun formatarAtributo(centesimos: Int): String = String.format(PT_BR, "%.2f", centesimos / 100.0)

/** 10 -> "+0,10", -20 -> "−0,20" */
fun formatarVariacao(centesimos: Int): String {
    val sinal = if (centesimos >= 0) "+" else "−"
    return sinal + formatarAtributo(kotlin.math.abs(centesimos))
}
