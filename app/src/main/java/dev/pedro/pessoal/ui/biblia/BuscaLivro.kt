package dev.pedro.pessoal.ui.biblia

import dev.pedro.pessoal.data.LivroEntity
import java.text.Normalizer
import java.util.Locale

/**
 * Busca de passagem pelo nome do livro, aceitando o jeito que a gente escreve de verdade:
 * "1joao", "1 João 3", "jo 3:16", "SALMO 119". Sem acento, sem ligar para maiúsculas e sem
 * exigir espaço depois do número do livro.
 *
 * Só mexe com nomes de livros; não procura palavras dentro dos versículos.
 */
object BuscaLivro {

    /** Uma referência digitada, já separada em nome, capítulo e versículo. */
    data class Referencia(
        /** O nome do livro, normalizado. Vazio quando o usuário ainda não digitou nada. */
        val termo: String,
        val capitulo: Int? = null,
        val versiculo: Int? = null,
    )

    private val PT_BR: Locale = Locale.forLanguageTag("pt-BR")
    private val ACENTOS = Regex("\\p{Mn}+")
    private val NAO_ALFANUMERICO = Regex("[^a-z0-9]")

    // "joão 3:16" e "1jo 3.16" — o capítulo e o versículo separados por : ou .
    private val COM_VERSICULO = Regex("^(.*\\D)\\s*(\\d+)\\s*[:.]\\s*(\\d+)\\s*$")

    // "1 joão 3" — exige espaço antes do capítulo, senão "1 joão" viraria livro 1, capítulo... nada.
    private val SO_CAPITULO = Regex("^(.*\\D)\\s+(\\d+)\\s*$")

    /** "1 João" -> "1joao". Tira acentos, maiúsculas, pontos e espaços. */
    fun normalizar(texto: String): String = Normalizer
        .normalize(texto, Normalizer.Form.NFD)
        .replace(ACENTOS, "")
        .lowercase(PT_BR)
        .replace(NAO_ALFANUMERICO, "")

    /** Separa o que foi digitado em nome do livro, capítulo e versículo. */
    fun interpretar(entrada: String): Referencia {
        val limpo = entrada.trim()
        if (limpo.isEmpty()) return Referencia(termo = "")

        COM_VERSICULO.matchEntire(limpo)?.destructured?.let { (nome, capitulo, versiculo) ->
            return Referencia(normalizar(nome), capitulo.toIntOrNull(), versiculo.toIntOrNull())
        }
        SO_CAPITULO.matchEntire(limpo)?.destructured?.let { (nome, capitulo) ->
            return Referencia(normalizar(nome), capitulo.toIntOrNull())
        }
        return Referencia(normalizar(limpo))
    }

    /**
     * Um livro com o nome e a abreviação já normalizados.
     *
     * Normalizar custa (NFD, duas expressões regulares e duas cópias da string por campo), e
     * os nomes dos livros nunca mudam. Isto é feito uma vez, quando a lista carrega, em vez
     * de 132 vezes a cada tecla digitada.
     */
    data class LivroIndexado(
        val livro: LivroEntity,
        val nome: String,
        val abreviacao: String,
    )

    fun indexar(livros: List<LivroEntity>): List<LivroIndexado> =
        livros.map { LivroIndexado(it, normalizar(it.nome), normalizar(it.abreviacao)) }

    /**
     * Os livros que combinam com o que foi digitado, dos mais parecidos para os menos:
     * nome ou abreviação iguais, depois os que começam igual, depois os que contêm o termo.
     * Com o campo vazio, devolve a lista inteira na ordem da Bíblia.
     */
    fun filtrar(indice: List<LivroIndexado>, entrada: String): List<LivroEntity> {
        val termo = interpretar(entrada).termo
        if (termo.isEmpty()) return indice.map { it.livro }
        return indice
            .mapNotNull { item -> pontuar(item, termo)?.let { item to it } }
            .sortedWith(compareBy({ it.second }, { it.first.livro.numero }))
            .map { it.first.livro }
    }

    /** Menor é mais parecido; null quando o livro não combina. */
    private fun pontuar(item: LivroIndexado, termo: String): Int? = when {
        item.nome == termo || item.abreviacao == termo -> 0
        item.nome.startsWith(termo) -> 1
        item.abreviacao.startsWith(termo) -> 2
        item.nome.contains(termo) -> 3
        else -> null
    }
}
