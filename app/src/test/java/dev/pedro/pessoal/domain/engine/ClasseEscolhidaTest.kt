package dev.pedro.pessoal.domain.engine

import dev.pedro.pessoal.domain.model.Atributo
import dev.pedro.pessoal.domain.model.Classe
import dev.pedro.pessoal.domain.model.ResultadoHabito
import dev.pedro.pessoal.domain.model.Status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * A classe escolhida na criação do personagem vale como ponto de partida: segura o lugar
 * enquanto os atributos ainda estão parecidos, e sai de cena quando eles apontam outra coisa.
 */
class ClasseEscolhidaTest {

    private val motor = MotorPontuacao

    private fun comAtributos(vararg pares: Pair<Atributo, Int>): Map<Atributo, Int> =
        Atributo.entries.associateWith { 0 } + pares

    @Test
    fun `classe escolhida continua enquanto os atributos estao parecidos`() {
        val s = motor.atualizarClasse(
            Status(classe = Classe.GUERREIRO, classeEscolhida = Classe.GUERREIRO),
        )
        assertEquals(Classe.GUERREIRO, s.classe)
        assertNull(s.classeCandidata)
    }

    @Test
    fun `sem classe escolhida o comportamento antigo continua valendo`() {
        val s = motor.atualizarClasse(Status(classe = Classe.APRENDIZ))
        assertEquals(Classe.APRENDIZ, s.classe)
        assertNull(s.classeCandidata)
    }

    @Test
    fun `a escolha sobrevive ao fechamento de um dia com atributos ainda parecidos`() {
        // Um dia inteiro cumprido mexe no XP e na disciplina, mas não desequilibra
        // o bastante para o motor indicar outra classe.
        val inicial = Status(classe = Classe.SACERDOTE, classeEscolhida = Classe.SACERDOTE)
        val depois = motor.fecharDia(
            inicial,
            listOf(ResultadoHabito(Atributo.ESPIRITO, cumprido = true, falhasSeguidas = 0, sequencia = 1)),
        )
        assertEquals(Classe.SACERDOTE, depois.classe)
    }

    @Test
    fun `os atributos tomam a frente quando apontam uma classe de verdade`() {
        var s = Status(
            classe = Classe.SACERDOTE,
            classeEscolhida = Classe.SACERDOTE,
            atributos = comAtributos(Atributo.FORCA to 200),
        )
        repeat(Regras.DIAS_PARA_TROCAR_CLASSE) { s = motor.atualizarClasse(s) }
        assertEquals(Classe.GUERREIRO, s.classe)
        assertNull(s.classeCandidata)
    }
}
