package dev.pedro.pessoal.ui.biblia

import kotlin.math.roundToInt

/**
 * Tamanho do texto da leitura, em sp. Tudo que o usuário escolhe (botões, controle
 * deslizante ou pinça) passa por aqui, para o texto nunca sair da faixa legível.
 */
object TamanhoFonte {
    const val MINIMO = 14f
    const val PADRAO = 18f
    const val MAXIMO = 34f
    const val PASSO = 2f

    fun limitar(valor: Float): Float = if (valor.isNaN()) PADRAO else valor.coerceIn(MINIMO, MAXIMO)

    fun aumentar(atual: Float): Float = limitar(atual + PASSO)

    fun diminuir(atual: Float): Float = limitar(atual - PASSO)

    /** Arredonda para meio sp, para a pinça não deixar valores quebrados salvos. */
    fun arredondar(valor: Float): Float = limitar((valor * 2f).roundToInt() / 2f)

    /** Entrelinha proporcional ao texto: 29 sp para 18 sp, como era antes do zoom. */
    fun entrelinha(tamanho: Float): Float = tamanho * (29f / 18f)

    /** O número do versículo cresce junto com o texto, mas mais devagar. */
    fun numero(tamanho: Float): Float = (tamanho * (13f / 18f)).coerceIn(12f, 20f)

    /** Tamanho em relação ao padrão, para mostrar ao usuário (100% = padrão). */
    fun percentual(tamanho: Float): Int = (tamanho / PADRAO * 100f).roundToInt()
}
