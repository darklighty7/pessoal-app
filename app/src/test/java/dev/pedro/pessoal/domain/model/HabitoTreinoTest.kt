package dev.pedro.pessoal.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HabitoTreinoTest {

    private val segunda = LocalDate.of(2026, 9, 21)
    private val terca = LocalDate.of(2026, 9, 22)
    private val sabado = LocalDate.of(2026, 9, 26)
    private val domingo = LocalDate.of(2026, 9, 27)

    @Test
    fun `biblia e oracao aparecem todos os dias`() {
        listOf(segunda, terca, sabado, domingo).forEach { dia ->
            val doDia = Habito.doDia(dia, admin = true)
            assertTrue(Habito.BIBLIA in doDia)
            assertTrue(Habito.ORACAO in doDia)
        }
    }

    @Test
    fun `karate so cai na terca e na quinta`() {
        assertTrue(Habito.KARATE.ocorreEm(terca))
        assertFalse(Habito.KARATE.ocorreEm(segunda))
        assertFalse(Habito.KARATE.ocorreEm(domingo))
    }

    @Test
    fun `mobilidade de sabado nao conta para o dia perfeito`() {
        assertFalse(Habito.MOBILIDADE.obrigatorio)
        assertEquals(2, Habito.doDia(sabado, admin = true).count { it.obrigatorio })
    }

    @Test
    fun `domingo so tem os habitos diarios`() {
        assertEquals(listOf(Habito.BIBLIA, Habito.ORACAO), Habito.doDia(domingo, admin = true))
        assertNull(Treino.doDia(domingo))
    }

    @Test
    fun `usuario comum so tem a biblia como habito fixo`() {
        listOf(segunda, terca, sabado, domingo).forEach { dia ->
            assertEquals(listOf(Habito.BIBLIA), Habito.doDia(dia, admin = false))
        }
    }

    @Test
    fun `habito desligado some do dia`() {
        val doDia = Habito.doDia(terca, admin = true, desligados = setOf(Habito.KARATE))
        assertFalse(Habito.KARATE in doDia)
        assertTrue(Habito.BIBLIA in doDia)
    }

    @Test
    fun `treino da semana segue o dia`() {
        assertEquals(Treino.A, Treino.doDia(segunda))
        assertEquals(Treino.MOBILIDADE, Treino.doDia(sabado))
    }

    @Test
    fun `variacao para na ultima e nao progride alem dela`() {
        val flexao = Treino.A.exercicios.first { it.nome == "Flexão" }
        assertEquals("na parede", flexao.variacao(0))
        assertEquals("diamante", flexao.variacao(99))
        assertTrue(flexao.podeProgredir(0))
        assertFalse(flexao.podeProgredir(flexao.variacoes.lastIndex))
    }
}
