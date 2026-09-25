package dev.pedro.pessoal.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * Baixa a Bíblia uma única vez, na primeira abertura, e guarda no banco.
 * A fonte é o projeto thiagobodruk/biblia, que publica a Nova Versão Internacional
 * em JSON. Depois de importada, o app funciona sem internet.
 */
class BibliaRepositorio(private val dao: BibliaDao) {

    /** Em que pé está o download, para a tela mostrar. */
    sealed interface Estado {
        data object Pronta : Estado
        data object Baixando : Estado
        data class Importando(val livrosFeitos: Int, val livrosTotal: Int) : Estado
        data class Falhou(val motivo: String) : Estado
    }

    suspend fun jaImportada(): Boolean = withContext(Dispatchers.IO) { dao.totalVersiculos() > 0 }

    suspend fun livros(): List<LivroEntity> = withContext(Dispatchers.IO) { dao.livros() }

    suspend fun capitulo(livro: Int, capitulo: Int): List<VersiculoEntity> =
        withContext(Dispatchers.IO) { dao.capitulo(livro, capitulo) }

    /**
     * Baixa e importa. [aoProgredir] é chamado a cada etapa para a tela acompanhar.
     * Se já estiver importada, não faz nada.
     */
    suspend fun baixarSeFaltar(aoProgredir: (Estado) -> Unit) = withContext(Dispatchers.IO) {
        if (dao.totalVersiculos() > 0) {
            aoProgredir(Estado.Pronta)
            return@withContext
        }
        try {
            aoProgredir(Estado.Baixando)
            val texto = baixar(FONTE)
            val json = JSONArray(texto.removePrefix("\uFEFF"))

            val livros = mutableListOf<LivroEntity>()
            for (i in 0 until json.length()) {
                val obj = json.getJSONObject(i)
                livros += LivroEntity(
                    numero = i + 1,
                    abreviacao = obj.getString("abbrev"),
                    nome = obj.optString("name", obj.getString("abbrev")),
                    capitulos = obj.getJSONArray("chapters").length(),
                )
            }
            dao.inserirLivros(livros)

            for (i in 0 until json.length()) {
                val capitulos = json.getJSONObject(i).getJSONArray("chapters")
                val versiculos = mutableListOf<VersiculoEntity>()
                for (c in 0 until capitulos.length()) {
                    val versos = capitulos.getJSONArray(c)
                    for (v in 0 until versos.length()) {
                        versiculos += VersiculoEntity(
                            livro = i + 1,
                            capitulo = c + 1,
                            numero = v + 1,
                            texto = versos.getString(v),
                        )
                    }
                }
                dao.inserirVersiculos(versiculos)
                aoProgredir(Estado.Importando(i + 1, json.length()))
            }
            aoProgredir(Estado.Pronta)
        } catch (e: Exception) {
            aoProgredir(Estado.Falhou(e.message ?: "não foi possível baixar"))
        }
    }

    private fun baixar(endereco: String): String {
        val conexao = (URL(endereco).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 60_000
            instanceFollowRedirects = true
        }
        try {
            if (conexao.responseCode != HttpURLConnection.HTTP_OK) {
                error("servidor respondeu ${conexao.responseCode}")
            }
            return conexao.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            conexao.disconnect()
        }
    }

    companion object {
        /** Nova Versão Internacional, ~4 MB, baixada uma vez só. */
        const val FONTE = "https://raw.githubusercontent.com/thiagobodruk/biblia/master/json/nvi.json"
        const val NOME_VERSAO = "Nova Versão Internacional"
    }
}
