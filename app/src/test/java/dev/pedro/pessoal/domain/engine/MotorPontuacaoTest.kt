package dev.pedro.pessoal.domain.engine

import dev.pedro.pessoal.domain.model.Atributo
import dev.pedro.pessoal.domain.model.Rank
import dev.pedro.pessoal.domain.model.Classe
import dev.pedro.pessoal.domain.model.ResultadoHabito
import dev.pedro.pessoal.domain.model.Status
import dev.pedro.pessoal.domain.model.Titulo
import dev.pedro.pessoal.domain.model.TipoTarefa
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MotorPontuacaoTest {

    private val motor = MotorPontuacao

    @Test
    fun `personagem comeca no nivel 1 com tudo zerado`() {
        val s = Status()
        assertEquals(1, s.nivel)
        assertEquals(0, s.xp)
        Atributo.entries.forEach { assertEquals(0, s.valor(it)) }
        assertEquals(Classe.APRENDIZ, s.classe)
    }

    @Test
    fun `faixas de rank`() {
        assertEquals(Rank.E, Rank.doNivel(1))
        assertEquals(Rank.E, Rank.doNivel(9))
        assertEquals(Rank.D, Rank.doNivel(10))
        assertEquals(Rank.D, Rank.doNivel(24))
        assertEquals(Rank.C, Rank.doNivel(25))
        assertEquals(Rank.C, Rank.doNivel(44))
        assertEquals(Rank.B, Rank.doNivel(45))
        assertEquals(Rank.B, Rank.doNivel(69))
        assertEquals(Rank.A, Rank.doNivel(70))
        assertEquals(Rank.A, Rank.doNivel(99))
        assertEquals(Rank.S, Rank.doNivel(100))
        assertEquals(Rank.S, Rank.doNivel(250))
    }

    @Test
    fun `sobe de nivel e guarda o XP que sobrou, cascateando varios niveis se preciso`() {
        // nível 1 pede 100 e nível 2 pede 102: 80 + 150 = 230 cobre os dois e sobra 28
        val s = motor.adicionarXp(Status(xp = 80), 150)
        assertEquals(3, s.nivel)
        assertEquals(28, s.xp)
    }

    @Test
    fun `curva de niveis cresce 2 XP fixos por nivel`() {
        assertEquals(100, Regras.xpParaProximoNivel(1))
        assertEquals(102, Regras.xpParaProximoNivel(2))
        assertEquals(104, Regras.xpParaProximoNivel(3))
    }

    @Test
    fun `XP para no zero e nao desce de nivel`() {
        val s = motor.adicionarXp(Status(nivel = 3, xp = 1), -2)
        assertEquals(3, s.nivel)
        assertEquals(0, s.xp)
    }

    @Test
    fun `atributo nunca fica abaixo do valor inicial`() {
        val s = motor.ajustarAtributo(Status(), Atributo.FORCA, -50)
        assertEquals(Regras.VALOR_INICIAL_ATRIBUTO, s.valor(Atributo.FORCA))
    }

    @Test
    fun `leitura abaixo da meta nao rende`() {
        assertEquals(0, motor.recompensaLeitura(9).xp)
    }

    @Test
    fun `leitura rende por minuto extra ate o teto`() {
        assertEquals(10, motor.recompensaLeitura(10).xp)
        assertEquals(18, motor.recompensaLeitura(18).xp)
        assertEquals(30, motor.recompensaLeitura(90).xp)
    }

    @Test
    fun `uma falha isolada nao tira atributo`() {
        val inicio = motor.ajustarAtributo(Status(), Atributo.FORCA, 100)
        val s = motor.fecharDia(inicio, listOf(ResultadoHabito(Atributo.FORCA, false, 1, 0)))
        assertEquals(100, s.valor(Atributo.FORCA))
    }

    @Test
    fun `duas e tres falhas seguidas tiram atributo e disciplina`() {
        var s = motor.ajustarAtributo(Status(), Atributo.FORCA, 100)
        s = motor.ajustarAtributo(s, Atributo.DISCIPLINA, 100)
        s = motor.fecharDia(s, listOf(ResultadoHabito(Atributo.FORCA, false, 2, 0)))
        assertEquals(90, s.valor(Atributo.FORCA))
        assertEquals(100, s.valor(Atributo.DISCIPLINA))
        s = motor.fecharDia(s, listOf(ResultadoHabito(Atributo.FORCA, false, 3, 0)))
        assertEquals(70, s.valor(Atributo.FORCA))
        assertEquals(95, s.valor(Atributo.DISCIPLINA))
    }

    @Test
    fun `dia perfeito da bonus e o desgaste e descontado`() {
        val s = motor.fecharDia(Status(), listOf(ResultadoHabito(Atributo.ESPIRITO, true, 0, 1)))
        assertEquals(15 - 2, s.xp)
        assertEquals(10, s.valor(Atributo.DISCIPLINA))
    }

    @Test
    fun `sequencia de 7 dias da bonus e titulo`() {
        val s = motor.fecharDia(Status(), listOf(ResultadoHabito(Atributo.ESPIRITO, true, 0, 7)))
        assertEquals(30, s.valor(Atributo.ESPIRITO))
        assertEquals(Titulo.GUERREIRO_CONSTANTE, s.titulo)
    }

    @Test
    fun `classes pura hibrida e aprendiz`() {
        val base = Atributo.entries.associateWith { 0 }
        assertEquals(Classe.APRENDIZ, motor.calcularClasse(base))
        assertEquals(Classe.SACERDOTE, motor.calcularClasse(base + (Atributo.ESPIRITO to 100)))
        assertEquals(
            Classe.PALADINO,
            motor.calcularClasse(base + (Atributo.ESPIRITO to 100) + (Atributo.FORCA to 80)),
        )
    }

    @Test
    fun `classe so muda depois de 3 dias seguidos`() {
        var s = motor.ajustarAtributo(Status(), Atributo.ESPIRITO, 100)
        s = motor.atualizarClasse(s)
        assertEquals(Classe.APRENDIZ, s.classe)
        assertEquals(1, s.diasCandidata)
        s = motor.atualizarClasse(motor.atualizarClasse(s))
        assertEquals(Classe.SACERDOTE, s.classe)
        assertNull(s.classeCandidata)
    }

    @Test
    fun `cenario do Pedro - Biblia em dia e treino atrasado vira Sacerdote`() {
        var s = Status()
        var falhasTreino = 0
        for (dia in 1..6) {
            s = motor.aplicar(s, motor.recompensaLeitura(15))
            s = motor.aplicar(s, TipoTarefa.ORACAO.recompensa)
            falhasTreino++
            s = motor.fecharDia(
                s,
                listOf(
                    ResultadoHabito(Atributo.ESPIRITO, true, 0, dia),
                    ResultadoHabito(Atributo.FORCA, false, falhasTreino, 0),
                ),
            )
        }
        assertEquals(Classe.SACERDOTE, s.classe)
    }

    @Test
    fun `ler em duas sessoes rende o mesmo que ler tudo de uma vez`() {
        // O repositório paga por diferença: o que já foi pago antes é descontado.
        val primeira = motor.recompensaLeitura(6).xp - motor.recompensaLeitura(0).xp
        val segunda = motor.recompensaLeitura(15).xp - motor.recompensaLeitura(6).xp
        assertEquals(motor.recompensaLeitura(15).xp, primeira + segunda)
    }

    @Test
    fun `leitura respeita o teto diario`() {
        assertEquals(Regras.XP_MAX_LEITURA_DIA, motor.recompensaLeitura(120).xp)
    }
}
