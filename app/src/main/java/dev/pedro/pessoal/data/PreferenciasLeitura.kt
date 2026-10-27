package dev.pedro.pessoal.data

import android.content.Context
import androidx.core.content.edit

/** Preferências da leitura da Bíblia, guardadas só no aparelho. */
class PreferenciasLeitura(contexto: Context) {

    // Guarda só o Context da aplicação: o ViewModel vive mais que a Activity, que é recriada ao girar a tela.
    private val appContext = contexto.applicationContext

    // O arquivo só é aberto no primeiro uso, que o ViewModel faz fora da thread principal.
    private val prefs by lazy {
        appContext.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)
    }

    /** Tamanho do texto em sp, ou null se o usuário nunca mexeu nele. */
    fun lerTamanhoFonte(): Float? =
        if (prefs.contains(CHAVE_TAMANHO)) prefs.getFloat(CHAVE_TAMANHO, 0f) else null

    fun salvarTamanhoFonte(valor: Float) = prefs.edit { putFloat(CHAVE_TAMANHO, valor) }

    /**
     * Onde a leitura parou da última vez, ou null na primeira abertura do app.
     * O livro é o número canônico (1 = Gênesis).
     */
    fun lerUltimaPosicao(): Posicao? {
        val livro = prefs.getInt(CHAVE_LIVRO, 0)
        if (livro <= 0) return null
        return Posicao(livro, prefs.getInt(CHAVE_CAPITULO, 1).coerceAtLeast(1))
    }

    fun salvarUltimaPosicao(livro: Int, capitulo: Int) = prefs.edit {
        putInt(CHAVE_LIVRO, livro)
        putInt(CHAVE_CAPITULO, capitulo)
    }

    /** Livro e capítulo onde a leitura parou. */
    data class Posicao(val livro: Int, val capitulo: Int)

    private companion object {
        const val ARQUIVO = "leitura"
        const val CHAVE_TAMANHO = "tamanho_fonte"
        const val CHAVE_LIVRO = "ultimo_livro"
        const val CHAVE_CAPITULO = "ultimo_capitulo"
    }
}
