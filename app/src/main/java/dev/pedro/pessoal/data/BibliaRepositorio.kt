package dev.pedro.pessoal.data

import androidx.core.text.HtmlCompat
import com.youversion.platform.core.api.YouVersionApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A estrutura da Bíblia (livros, capítulos e versículos por capítulo) e os grifos.
 *
 * O texto não passa por aqui: cada versículo é desenhado pelo `BibleText` do SDK da YouVersion,
 * que busca e guarda em cache o texto conforme a licença (ver [Escrituras]). A estrutura vem
 * uma vez da YouVersion e fica no banco, para o índice e a navegação funcionarem sem internet.
 */
class BibliaRepositorio(
    private val dao: BibliaDao,
    private val preferencias: PreferenciasLeitura,
) {

    /** Em que pé está a preparação, para a tela mostrar. */
    sealed interface Estado {
        data object Pronta : Estado
        data object Preparando : Estado
        data class Falhou(val motivo: String) : Estado
    }

    /** Pronta se os 66 livros tiverem código e contagem de versículos. */
    suspend fun jaPreparada(): Boolean = withContext(Dispatchers.IO) { completa() }

    private suspend fun completa(): Boolean {
        val livros = dao.totalLivros()
        return livros == TOTAL_LIVROS && dao.livrosCompletos() == livros
    }

    suspend fun livros(): List<LivroEntity> = withContext(Dispatchers.IO) { dao.livros() }

    /**
     * Quantos versículos o capítulo tem. Normalmente já vem da estrutura; se o índice da
     * YouVersion não tiver trazido a contagem, busca só a lista de versículos (sem texto).
     */
    suspend fun versiculos(livro: LivroEntity, capitulo: Int): Int = withContext(Dispatchers.IO) {
        livro.versiculosDo(capitulo).takeIf { it > 0 }?.let { return@withContext it }
        runCatching {
            YouVersionApi.bible.verses(Escrituras.VERSAO_NVI, livro.usfm, capitulo.toString()).size
        }.getOrDefault(0)
    }

    /** Aviso de copyright e de marca da NVI, como a YouVersion entrega; vazio se ainda não veio. */
    fun avisoCopyright(): String = preferencias.lerAvisoCopyright().orEmpty()

    suspend fun grifos(livro: Int, capitulo: Int): Map<Int, Int> =
        withContext(Dispatchers.IO) { dao.grifos(livro, capitulo).associate { it.numero to it.cor } }

    suspend fun grifar(livro: Int, capitulo: Int, numeros: Collection<Int>, cor: Int) =
        withContext(Dispatchers.IO) {
            val agora = System.currentTimeMillis()
            numeros.forEach { dao.salvarGrifo(GrifoEntity(livro, capitulo, it, cor, agora)) }
        }

    suspend fun removerGrifos(livro: Int, capitulo: Int, numeros: Collection<Int>) =
        withContext(Dispatchers.IO) { numeros.forEach { dao.removerGrifo(livro, capitulo, it) } }

    suspend fun todosGrifos(): List<GrifoListado> = withContext(Dispatchers.IO) { dao.todosGrifos() }

    /**
     * Busca a estrutura da NVI na YouVersion e guarda, se ainda não estiver no aparelho.
     * Também guarda o aviso de copyright. Precisa de internet só nesta primeira vez.
     */
    suspend fun prepararSeFaltar(aoProgredir: (Estado) -> Unit) = withContext(Dispatchers.IO) {
        if (completa()) {
            aoProgredir(Estado.Pronta)
            atualizarAvisoSeFaltar()
            return@withContext
        }
        if (!Escrituras.temChave) {
            aoProgredir(Estado.Falhou("Falta a chave da YouVersion (YVP_APP_KEY no local.properties)"))
            return@withContext
        }
        try {
            aoProgredir(Estado.Preparando)
            val indice = YouVersionApi.bible.versionIndex(Escrituras.VERSAO_NVI)
            val livros = indice.books.orEmpty()
                .filter { it.isCanonical && !it.usfm.isNullOrBlank() }
                .mapIndexed { i, livro ->
                    val capitulos = livro.chapters.orEmpty().filter { it.isCanonical }
                    val usfm = livro.usfm.orEmpty()
                    LivroEntity(
                        numero = i + 1,
                        abreviacao = (livro.abbreviation ?: usfm).lowercase(),
                        nome = livro.title ?: livro.fullTitle ?: usfm,
                        capitulos = capitulos.size,
                        usfm = usfm,
                        versiculos = capitulos.joinToString(",") { (it.verses?.size ?: 0).toString() },
                    )
                }
            // A numeração dos grifos é a ordem canônica de 66 livros; outra contagem é sinal de erro.
            check(livros.size == TOTAL_LIVROS) { "a YouVersion trouxe ${livros.size} livros" }
            dao.substituirLivros(livros)
            atualizarAvisoSeFaltar()
            aoProgredir(Estado.Pronta)
        } catch (e: Exception) {
            aoProgredir(Estado.Falhou(e.message ?: "não foi possível falar com a YouVersion"))
        }
    }

    private suspend fun atualizarAvisoSeFaltar() {
        if (!preferencias.lerAvisoCopyright().isNullOrBlank()) return
        runCatching {
            val versao = YouVersionApi.bible.version(Escrituras.VERSAO_NVI)
            val aviso = versao.copyright?.let {
                HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim()
            }
            if (!aviso.isNullOrBlank()) preferencias.salvarAvisoCopyright(aviso)
        }
    }

    companion object {
        const val TOTAL_LIVROS = 66
    }
}
