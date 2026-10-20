package dev.pedro.pessoal.ui.biblia

import androidx.compose.ui.graphics.Color

/**
 * Cores do marca-texto. O banco guarda a posição (ordinal) da cor, então
 * não reordene nem remova itens: só acrescente novos no fim.
 */
enum class CorGrifo(val nome: String, val cor: Color) {
    AMARELO("amarelo", Color(0xFFF5D547)),
    VERDE("verde", Color(0xFF3DDC97)),
    AZUL("azul", Color(0xFF5AA7FF)),
    ROSA("rosa", Color(0xFFFF7DB5)),
    LARANJA("laranja", Color(0xFFFF9F43)),
    ;

    /** Fundo translúcido, para o texto continuar legível no tema escuro. */
    val fundo: Color get() = cor.copy(alpha = 0.22f)

    companion object {
        fun deIndice(indice: Int): CorGrifo? = entries.getOrNull(indice)
    }
}
