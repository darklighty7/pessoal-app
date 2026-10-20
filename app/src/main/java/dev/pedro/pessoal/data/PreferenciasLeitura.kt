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

    private companion object {
        const val ARQUIVO = "leitura"
        const val CHAVE_TAMANHO = "tamanho_fonte"
    }
}
