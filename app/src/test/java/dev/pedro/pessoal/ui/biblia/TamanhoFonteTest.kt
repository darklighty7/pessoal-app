package dev.pedro.pessoal.ui.biblia

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TamanhoFonteTest {

    @Test
    fun `limitar mantem o tamanho dentro da faixa`() {
        assertEquals(TamanhoFonte.MINIMO, TamanhoFonte.limitar(1f), 0f)
        assertEquals(TamanhoFonte.MAXIMO, TamanhoFonte.limitar(500f), 0f)
        assertEquals(22f, TamanhoFonte.limitar(22f), 0f)
    }

    @Test
    fun `valor invalido volta para o padrao`() {
        assertEquals(TamanhoFonte.PADRAO, TamanhoFonte.limitar(Float.NaN), 0f)
    }

    @Test
    fun `aumentar e diminuir andam um passo e param nos limites`() {
        assertEquals(20f, TamanhoFonte.aumentar(18f), 0f)
        assertEquals(16f, TamanhoFonte.diminuir(18f), 0f)
        assertEquals(TamanhoFonte.MAXIMO, TamanhoFonte.aumentar(TamanhoFonte.MAXIMO), 0f)
        assertEquals(TamanhoFonte.MINIMO, TamanhoFonte.diminuir(TamanhoFonte.MINIMO), 0f)
    }

    @Test
    fun `arredondar usa meio sp e respeita a faixa`() {
        assertEquals(20.5f, TamanhoFonte.arredondar(20.3f), 0f)
        assertEquals(20f, TamanhoFonte.arredondar(20.2f), 0f)
        assertEquals(TamanhoFonte.MAXIMO, TamanhoFonte.arredondar(99f), 0f)
        assertEquals(TamanhoFonte.MINIMO, TamanhoFonte.arredondar(3f), 0f)
    }

    @Test
    fun `entrelinha no padrao e a de antes do zoom`() {
        assertEquals(29f, TamanhoFonte.entrelinha(TamanhoFonte.PADRAO), 0.001f)
    }

    @Test
    fun `entrelinha fica folgada em toda a faixa`() {
        var tamanho = TamanhoFonte.MINIMO
        while (tamanho <= TamanhoFonte.MAXIMO) {
            assertTrue(TamanhoFonte.entrelinha(tamanho) >= tamanho * 1.5f)
            tamanho += TamanhoFonte.PASSO
        }
    }

    @Test
    fun `numero do versiculo cresce mais devagar que o texto`() {
        assertEquals(13f, TamanhoFonte.numero(TamanhoFonte.PADRAO), 0.001f)
        assertEquals(12f, TamanhoFonte.numero(TamanhoFonte.MINIMO), 0f)
        assertEquals(20f, TamanhoFonte.numero(TamanhoFonte.MAXIMO), 0f)
        assertTrue(TamanhoFonte.numero(TamanhoFonte.MAXIMO) < TamanhoFonte.MAXIMO)
    }

    @Test
    fun `percentual mostra o tamanho em relacao ao padrao`() {
        assertEquals(100, TamanhoFonte.percentual(TamanhoFonte.PADRAO))
        assertEquals(78, TamanhoFonte.percentual(TamanhoFonte.MINIMO))
        assertEquals(189, TamanhoFonte.percentual(TamanhoFonte.MAXIMO))
    }
}
