package dev.pedro.pessoal.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class ContarSeguidosTest {

    private val hoje = LocalDate.of(2026, 11, 13) // sexta
    private val todoDia: (LocalDate) -> Boolean = { true }

    @Test
    fun `lembrete criado ontem e falhado hoje conta uma falha so`() {
        // Ontem não falhou (foi cumprido); antes disso o lembrete não existia: sem registro.
        val anteriores = mapOf(hoje.minusDays(1) to true)
        assertEquals(1, contarSeguidos(anteriores, hoje, buscando = false, ocorreEm = todoDia))
    }

    @Test
    fun `dias sem registro nao contam como falha`() {
        // Antes: o mapa vazio virava 90 dias de falha e a primeira falha já era "3 ou mais".
        assertEquals(1, contarSeguidos(emptyMap(), hoje, buscando = false, ocorreEm = todoDia))
    }

    @Test
    fun `falhas gravadas em dias seguidos somam`() {
        val anteriores = mapOf(
            hoje.minusDays(1) to false,
            hoje.minusDays(2) to false,
            hoje.minusDays(3) to true,
        )
        assertEquals(3, contarSeguidos(anteriores, hoje, buscando = false, ocorreEm = todoDia))
    }

    @Test
    fun `a contagem para no primeiro dia sem registro`() {
        // Dois dias falhados, um buraco (hábito desligado, por exemplo) e falhas antigas.
        val anteriores = mapOf(
            hoje.minusDays(1) to false,
            hoje.minusDays(3) to false,
            hoje.minusDays(4) to false,
        )
        assertEquals(2, contarSeguidos(anteriores, hoje, buscando = false, ocorreEm = todoDia))
    }

    @Test
    fun `sequencia de cumpridos continua contando`() {
        val anteriores = (1L..5L).associate { hoje.minusDays(it) to true }
        assertEquals(6, contarSeguidos(anteriores, hoje, buscando = true, ocorreEm = todoDia))
    }

    @Test
    fun `so olha os dias em que a tarefa estava programada`() {
        // Karatê: terça e quinta. Hoje é sexta só para o teste; os dias fora da agenda são pulados.
        val karate: (LocalDate) -> Boolean = { it.dayOfWeek == DayOfWeek.TUESDAY || it.dayOfWeek == DayOfWeek.THURSDAY }
        val anteriores = mapOf(
            LocalDate.of(2026, 11, 12) to false, // quinta
            LocalDate.of(2026, 11, 10) to false, // terça
            LocalDate.of(2026, 11, 5) to true, // quinta da semana anterior
        )
        assertEquals(3, contarSeguidos(anteriores, hoje, buscando = false, ocorreEm = karate))
    }
}
