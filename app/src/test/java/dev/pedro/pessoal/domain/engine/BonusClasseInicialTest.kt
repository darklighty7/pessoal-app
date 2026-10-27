package dev.pedro.pessoal.domain.engine

import dev.pedro.pessoal.domain.model.Atributo
import dev.pedro.pessoal.domain.model.Classe
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * O bônus da classe escolhida na criação. O que precisa valer:
 * a escolha aparece já no primeiro dia, e escolher Aprendiz não custa pontos.
 */
class BonusClasseInicialTest {

    @Test
    fun `classe pura poe todo o bonus no proprio atributo`() {
        val inicio = MotorPontuacao.atributosIniciais(Classe.GUERREIRO)

        assertEquals(Regras.BONUS_CLASSE_INICIAL, inicio.getValue(Atributo.FORCA))
        assertEquals(Regras.VALOR_INICIAL_ATRIBUTO, inicio.getValue(Atributo.ESPIRITO))
        assertEquals(Regras.VALOR_INICIAL_ATRIBUTO, inicio.getValue(Atributo.INTELIGENCIA))
        assertEquals(Regras.VALOR_INICIAL_ATRIBUTO, inicio.getValue(Atributo.DISCIPLINA))
    }

    @Test
    fun `as quatro puras ja valem no primeiro dia`() {
        listOf(
            Classe.GUERREIRO to Atributo.FORCA,
            Classe.SACERDOTE to Atributo.ESPIRITO,
            Classe.SABIO to Atributo.INTELIGENCIA,
            Classe.SENTINELA to Atributo.DISCIPLINA,
        ).forEach { (classe, atributo) ->
            val inicio = MotorPontuacao.atributosIniciais(classe)
            assertEquals(
                "o bônus de $classe deveria cair em $atributo",
                Regras.BONUS_CLASSE_INICIAL,
                inicio.getValue(atributo),
            )
            // É este o ponto do bônus: sem ele a diferença ficaria dentro da margem
            // e o motor leria Aprendiz no dia em que o personagem nasce.
            assertEquals(classe, MotorPontuacao.calcularClasse(inicio))
        }
    }

    @Test
    fun `aprendiz espalha pelos quatro e continua sendo aprendiz`() {
        val inicio = MotorPontuacao.atributosIniciais(Classe.APRENDIZ)
        val fatia = Regras.BONUS_CLASSE_INICIAL / Atributo.entries.size

        Atributo.entries.forEach { assertEquals(fatia, inicio.getValue(it)) }
        assertEquals(Classe.APRENDIZ, MotorPontuacao.calcularClasse(inicio))
    }

    @Test
    fun `escolher aprendiz nao custa pontos`() {
        val total = { classe: Classe -> MotorPontuacao.atributosIniciais(classe).values.sum() }
        val esperado = Regras.VALOR_INICIAL_ATRIBUTO * Atributo.entries.size + Regras.BONUS_CLASSE_INICIAL

        listOf(Classe.APRENDIZ, Classe.GUERREIRO, Classe.SACERDOTE, Classe.SABIO, Classe.SENTINELA)
            .forEach { assertEquals("total de $it", esperado, total(it)) }
    }

    @Test
    fun `o bonus precisa ser maior que a margem de classe`() {
        // Se algum dia alguém baixar o bônus, este teste quebra antes de a escolha
        // da tela de criação parar de aparecer no primeiro dia.
        assertEquals(true, Regras.BONUS_CLASSE_INICIAL > Regras.MARGEM_CLASSE)
    }

    @Test
    fun `hibrida recebe o bonus cheio nos dois atributos`() {
        val inicio = MotorPontuacao.atributosIniciais(Classe.PALADINO)

        assertEquals(Regras.BONUS_CLASSE_INICIAL, inicio.getValue(Atributo.FORCA))
        assertEquals(Regras.BONUS_CLASSE_INICIAL, inicio.getValue(Atributo.ESPIRITO))
        assertEquals(Classe.PALADINO, MotorPontuacao.calcularClasse(inicio))
    }
}
