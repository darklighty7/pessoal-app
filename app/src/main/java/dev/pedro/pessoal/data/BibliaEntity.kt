package dev.pedro.pessoal.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction

/** Um livro da Bíblia, com a ordem canônica em [numero] (1 = Gênesis). */
@Entity(tableName = "livro")
data class LivroEntity(
    @PrimaryKey val numero: Int,
    val abreviacao: String,
    val nome: String,
    val capitulos: Int,
)

@Entity(tableName = "versiculo", primaryKeys = ["livro", "capitulo", "numero"])
data class VersiculoEntity(
    val livro: Int,
    val capitulo: Int,
    val numero: Int,
    val texto: String,
)

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

/** Um grifo já ligado ao texto e ao nome do livro, para a lista de grifos. */
data class GrifoListado(
    val livro: Int,
    val capitulo: Int,
    val numero: Int,
    val cor: Int,
    val texto: String,
    val livroNome: String,
)

/**
 * Classe abstrata, e não interface, para [importar] poder ter corpo dentro de uma
 * transação do Room sem depender de métodos padrão de interface.
 */
@Dao
abstract class BibliaDao {
    @Query("SELECT COUNT(*) FROM versiculo")
    abstract suspend fun totalVersiculos(): Int

    @Query("SELECT COUNT(*) FROM livro")
    abstract suspend fun totalLivros(): Int

    /** Livros com pelo menos um versículo; numa importação completa é igual a [totalLivros]. */
    @Query("SELECT COUNT(DISTINCT livro) FROM versiculo")
    abstract suspend fun livrosComVersiculos(): Int

    @Query("SELECT * FROM livro ORDER BY numero")
    abstract suspend fun livros(): List<LivroEntity>

    @Query("SELECT * FROM livro WHERE numero = :numero")
    abstract suspend fun livro(numero: Int): LivroEntity?

    @Query("SELECT * FROM versiculo WHERE livro = :livro AND capitulo = :capitulo ORDER BY numero")
    abstract suspend fun capitulo(livro: Int, capitulo: Int): List<VersiculoEntity>

    @Insert
    abstract suspend fun inserirLivros(livros: List<LivroEntity>)

    @Insert
    abstract suspend fun inserirVersiculos(versiculos: List<VersiculoEntity>)

    @Query("DELETE FROM versiculo")
    abstract suspend fun apagarVersiculos()

    @Query("DELETE FROM livro")
    abstract suspend fun apagarLivros()

    /**
     * Grava a Bíblia inteira de uma vez: ou entra tudo, ou nada. Apaga antes o que tiver
     * sobrado de uma importação interrompida. Os grifos ficam (a chave é livro/capítulo/versículo).
     */
    @Transaction
    open suspend fun importar(livros: List<LivroEntity>, versiculos: List<VersiculoEntity>) {
        apagarVersiculos()
        apagarLivros()
        inserirLivros(livros)
        inserirVersiculos(versiculos)
    }

    @Query("SELECT * FROM grifo WHERE livro = :livro AND capitulo = :capitulo")
    abstract suspend fun grifos(livro: Int, capitulo: Int): List<GrifoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun salvarGrifo(grifo: GrifoEntity)

    @Query("DELETE FROM grifo WHERE livro = :livro AND capitulo = :capitulo AND numero = :numero")
    abstract suspend fun removerGrifo(livro: Int, capitulo: Int, numero: Int)

    @Query(
        "SELECT g.livro AS livro, g.capitulo AS capitulo, g.numero AS numero, g.cor AS cor, " +
            "v.texto AS texto, l.nome AS livroNome " +
            "FROM grifo g " +
            "JOIN versiculo v ON v.livro = g.livro AND v.capitulo = g.capitulo AND v.numero = g.numero " +
            "JOIN livro l ON l.numero = g.livro " +
            "ORDER BY g.criadoEm DESC",
    )
    abstract suspend fun todosGrifos(): List<GrifoListado>
}
