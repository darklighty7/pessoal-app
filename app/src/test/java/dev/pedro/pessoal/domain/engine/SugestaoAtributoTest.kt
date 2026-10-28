package dev.pedro.pessoal.domain.engine

import dev.pedro.pessoal.domain.model.Atributo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * A sugestão de atributo a partir do título do lembrete.
 *
 * O que precisa valer: acertar o óbvio, resolver as sobreposições pelo começo de palavra
 * mais específico, e calar a boca quando não reconhece nada — porque devolver um palpite
 * fraco é pior que não devolver nada, já que a tela move a escolha da pessoa.
 */
class SugestaoAtributoTest {

    @Test
    fun `reconhece o obvio de cada atributo`() {
        assertEquals(Atributo.FORCA, SugestaoAtributo.de("Correr 5km"))
        assertEquals(Atributo.ESPIRITO, SugestaoAtributo.de("Orar pela manhã"))
        assertEquals(Atributo.INTELIGENCIA, SugestaoAtributo.de("Estudar inglês"))
        assertEquals(Atributo.DISCIPLINA, SugestaoAtributo.de("Beber 2 litros de água"))
    }

    @Test
    fun `o comeco de palavra mais longo vence`() {
        // "bibli" tem 5 letras e "ler" tem 3: quem escreve isto quer dizer Espírito,
        // não leitura em geral.
        assertEquals(Atributo.ESPIRITO, SugestaoAtributo.de("Ler a Bíblia"))
        // "karate" (6) ganha de "aula" (4).
        assertEquals(Atributo.FORCA, SugestaoAtributo.de("Aula de karatê"))
        // "leit" (4) e "meta" (4) empatam, e Inteligência vem antes de Disciplina.
        assertEquals(Atributo.INTELIGENCIA, SugestaoAtributo.de("Meta de leitura"))
    }

    @Test
    fun `nao se perde com acento nem com caixa`() {
        assertEquals(Atributo.FORCA, SugestaoAtributo.de("ALONGAMENTO"))
        assertEquals(Atributo.ESPIRITO, SugestaoAtributo.de("jejum"))
        assertEquals(Atributo.FORCA, SugestaoAtributo.de("Fazer exercícios"))
    }

    @Test
    fun `cala a boca quando nao reconhece`() {
        assertNull(SugestaoAtributo.de(""))
        assertNull(SugestaoAtributo.de("ok"))
        assertNull(SugestaoAtributo.de("Ir ao mercado"))
        assertNull(SugestaoAtributo.de("Ligar para a vó"))
    }

    @Test
    fun `palavras curtas demais nao disparam nada`() {
        // O filtro de três letras existe para "de", "ao" e companhia não virarem pista.
        assertNull(SugestaoAtributo.de("de ao na"))
    }
}
