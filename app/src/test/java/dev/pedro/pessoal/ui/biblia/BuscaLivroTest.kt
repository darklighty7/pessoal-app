package dev.pedro.pessoal.ui.biblia

import dev.pedro.pessoal.data.LivroEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class BuscaLivroTest {

    private val livros = listOf(
        LivroEntity(1, "gn", "Gênesis", 50),
        LivroEntity(6, "js", "Josué", 24),
        LivroEntity(18, "jó", "Jó", 42),
        LivroEntity(19, "sl", "Salmos", 150),
        LivroEntity(22, "ct", "Cântico dos Cânticos", 8),
        LivroEntity(32, "jn", "Jonas", 4),
        LivroEntity(43, "jo", "João", 21),
        LivroEntity(62, "1jo", "1 João", 5),
        LivroEntity(63, "2jo", "2 João", 1),
        LivroEntity(64, "3jo", "3 João", 1),
    )

    private fun nomes(entrada: String) = BuscaLivro.filtrar(livros, entrada).map { it.nome }

    // ----- normalizar -----

    @Test
    fun `tira acentos, maiusculas e espacos`() {
        assertEquals("1joao", BuscaLivro.normalizar("1 João"))
        assertEquals("genesis", BuscaLivro.normalizar("GÊNESIS"))
        assertEquals("canticodoscanticos", BuscaLivro.normalizar("Cântico dos Cânticos"))
    }

    // ----- interpretar -----

    @Test
    fun `so o nome do livro, com ou sem espaco depois do numero`() {
        assertEquals(BuscaLivro.Referencia("1joao"), BuscaLivro.interpretar("1 João"))
        assertEquals(BuscaLivro.Referencia("1joao"), BuscaLivro.interpretar("1João"))
    }

    @Test
    fun `o numero do fim e o capitulo, nao parte do nome`() {
        assertEquals(BuscaLivro.Referencia("1joao", 3), BuscaLivro.interpretar("1 João 3"))
        assertEquals(BuscaLivro.Referencia("1joao", 3), BuscaLivro.interpretar("1João 3"))
        assertEquals(BuscaLivro.Referencia("salmo", 119), BuscaLivro.interpretar("Salmo 119"))
    }

    @Test
    fun `capitulo e versiculo separados por dois pontos ou ponto`() {
        assertEquals(BuscaLivro.Referencia("jo", 3, 16), BuscaLivro.interpretar("Jo 3:16"))
        assertEquals(BuscaLivro.Referencia("jo", 3, 16), BuscaLivro.interpretar("jo 3.16"))
    }

    @Test
    fun `campo vazio nao vira referencia`() {
        assertEquals(BuscaLivro.Referencia(""), BuscaLivro.interpretar("   "))
    }

    // ----- filtrar -----

    @Test
    fun `campo vazio devolve a lista inteira na ordem da biblia`() {
        assertEquals(livros.map { it.nome }, nomes(""))
    }

    @Test
    fun `o evangelho vem antes das cartas de Joao`() {
        assertEquals(listOf("João", "1 João", "2 João", "3 João"), nomes("joão"))
    }

    @Test
    fun `o numero do livro restringe a carta certa`() {
        assertEquals(listOf("1 João"), nomes("1joao"))
        assertEquals(listOf("1 João"), nomes("1 João 3"))
    }

    @Test
    fun `quem casa exato vem primeiro`() {
        // "jo" é o nome de Jó sem acento e a abreviação de João: os dois casam exato,
        // e o desempate é a ordem da Bíblia. Depois vêm os que apenas começam com "jo".
        assertEquals(listOf("Jó", "João", "Josué", "Jonas", "1 João", "2 João", "3 João"), nomes("jo"))
    }

    @Test
    fun `digitar sem acento acha o livro acentuado`() {
        assertEquals(listOf("Cântico dos Cânticos"), nomes("cantico"))
    }

    @Test
    fun `sem nada parecido, a lista fica vazia`() {
        assertEquals(emptyList<String>(), nomes("xyz"))
    }
}
