package dev.pedro.pessoal.data.nuvem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NumeroJogadorTest {

    @Test
    fun ultimoDaFilaDevolveONumero() {
        assertEquals(1L, totalAposApagar(numeroDoPerfil = 2L, totalAtual = 2L))
    }

    @Test
    fun alguemChegouDepoisEntaoONumeroFicaVago() {
        assertNull(totalAposApagar(numeroDoPerfil = 2L, totalAtual = 3L))
    }

    @Test
    fun perfilSemNumeroNaoMexeNoContador() {
        assertNull(totalAposApagar(numeroDoPerfil = null, totalAtual = 2L))
        assertNull(totalAposApagar(numeroDoPerfil = 0L, totalAtual = 0L))
    }

    @Test
    fun contadorInexistenteNaoMexe() {
        assertNull(totalAposApagar(numeroDoPerfil = 1L, totalAtual = null))
    }

    @Test
    fun unicoJogadorVoltaAoZero() {
        assertEquals(0L, totalAposApagar(numeroDoPerfil = 1L, totalAtual = 1L))
    }
}
