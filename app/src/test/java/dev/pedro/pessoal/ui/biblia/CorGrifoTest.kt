package dev.pedro.pessoal.ui.biblia

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CorGrifoTest {
    /** O banco guarda a posição da cor: mudar a ordem trocaria a cor dos grifos já salvos. */
    @Test
    fun ordemDasCoresNaoMuda() {
        assertEquals(
            listOf("amarelo", "verde", "azul", "rosa", "laranja"),
            CorGrifo.entries.map { it.nome },
        )
    }

    @Test
    fun indiceInvalidoNaoQuebra() {
        assertEquals(CorGrifo.AZUL, CorGrifo.deIndice(2))
        assertNull(CorGrifo.deIndice(99))
        assertNull(CorGrifo.deIndice(-1))
    }
}
