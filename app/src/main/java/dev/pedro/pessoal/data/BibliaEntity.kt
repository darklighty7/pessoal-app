package dev.pedro.pessoal.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction

/**
 * Um livro da Bíblia, com a ordem canônica em [numero] (1 = Gênesis). Guarda só a estrutura,
 * nunca o texto: o texto da NVI vem do SDK da YouVersion (ver [Escrituras]).
 *
 * [usfm] é o código do livro na YouVersion ("GEN", "JHN"...). [versiculos] tem a quantidade
 * de versículos de cada capítulo, separada por vírgula ("31,25,24...").
 */
@Entity(tableName = "livro")
data class LivroEntity(
    @PrimaryKey val numero: Int,
    val abreviacao: String,
    val nome: String,
    val capitulos: Int,
    val usfm: String = "",
    val versiculos: String = "",
) {
    /** Quantos versículos o [capitulo] tem; 0 se a estrutura ainda não souber. */
    fun versiculosDo(capitulo: Int): Int =
        versiculos.split(',').getOrNull(capitulo - 1)?.trim()?.toIntOrNull() ?: 0
}

/**
 * Um versículo grifado (marca-texto). [cor] é a posição da cor em `CorGrifo`
 * (amarelo = 0, verde = 1, azul = 2, rosa = 3, laranja = 4).
 */
@Entity(tableName = "grifo", primaryKeys = ["livro", "capitulo", "numero"])
data class GrifoEntity(
    val livro: Int,
    val capitulo: Int,
    val numero: Int,
    val cor: Int,
    val criadoEm: Long,
)

/**
 * Um grifo com o nome do livro, para a lista de grifos. Sem o texto: a lista mostra só a
 * referência, e o texto aparece ao abrir o capítulo (a licença limita quantos versículos
 * podem estar na tela ao mesmo tempo).
 */
data class GrifoListado(
    val livro: Int,
    val capitulo: Int,
    val numero: Int,
    val cor: Int,
    val livroNome: String,
)

/** Classe abstrata, e não interface, para [substituirLivros] poder ter corpo numa transação. */
@Dao
abstract class BibliaDao {
    @Query("SELECT COUNT(*) FROM livro")
    abstract suspend fun totalLivros(): Int

    /** Livros com a estrutura completa (código e versículos); quando pronta, é igual a [totalLivros]. */
    @Query("SELECT COUNT(*) FROM livro WHERE usfm != '' AND versiculos != ''")
    abstract suspend fun livrosCompletos(): Int

    @Query("SELECT * FROM livro ORDER BY numero")
    abstract suspend fun livros(): List<LivroEntity>

    @Query("SELECT * FROM livro WHERE numero = :numero")
    abstract suspend fun livro(numero: Int): LivroEntity?

    @Insert
    abstract suspend fun inserirLivros(livros: List<LivroEntity>)

    @Query("DELETE FROM livro")
    abstract suspend fun apagarLivros()

    /** Troca a estrutura inteira de uma vez: ou entra tudo, ou nada. Os grifos ficam. */
    @Transaction
    open suspend fun substituirLivros(livros: List<LivroEntity>) {
        apagarLivros()
        inserirLivros(livros)
    }

    @Query("SELECT * FROM grifo WHERE livro = :livro AND capitulo = :capitulo")
    abstract suspend fun grifos(livro: Int, capitulo: Int): List<GrifoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun salvarGrifo(grifo: GrifoEntity)

    @Query("DELETE FROM grifo WHERE livro = :livro AND capitulo = :capitulo AND numero = :numero")
    abstract suspend fun removerGrifo(livro: Int, capitulo: Int, numero: Int)

    @Query(
        "SELECT g.livro AS livro, g.capitulo AS capitulo, g.numero AS numero, g.cor AS cor, " +
            "l.nome AS livroNome " +
            "FROM grifo g " +
            "JOIN livro l ON l.numero = g.livro " +
            "ORDER BY g.criadoEm DESC",
    )
    abstract suspend fun todosGrifos(): List<GrifoListado>
}
