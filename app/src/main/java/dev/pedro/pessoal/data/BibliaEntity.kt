package dev.pedro.pessoal.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

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

@Dao
interface BibliaDao {
    @Query("SELECT COUNT(*) FROM versiculo")
    suspend fun totalVersiculos(): Int

    @Query("SELECT * FROM livro ORDER BY numero")
    suspend fun livros(): List<LivroEntity>

    @Query("SELECT * FROM livro WHERE numero = :numero")
    suspend fun livro(numero: Int): LivroEntity?

    @Query("SELECT * FROM versiculo WHERE livro = :livro AND capitulo = :capitulo ORDER BY numero")
    suspend fun capitulo(livro: Int, capitulo: Int): List<VersiculoEntity>

    @Insert
    suspend fun inserirLivros(livros: List<LivroEntity>)

    @Insert
    suspend fun inserirVersiculos(versiculos: List<VersiculoEntity>)

    @Query("SELECT * FROM grifo WHERE livro = :livro AND capitulo = :capitulo")
    suspend fun grifos(livro: Int, capitulo: Int): List<GrifoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarGrifo(grifo: GrifoEntity)

    @Query("DELETE FROM grifo WHERE livro = :livro AND capitulo = :capitulo AND numero = :numero")
    suspend fun removerGrifo(livro: Int, capitulo: Int, numero: Int)

    @Query(
        "SELECT g.livro AS livro, g.capitulo AS capitulo, g.numero AS numero, g.cor AS cor, " +
            "v.texto AS texto, l.nome AS livroNome " +
            "FROM grifo g " +
            "JOIN versiculo v ON v.livro = g.livro AND v.capitulo = g.capitulo AND v.numero = g.numero " +
            "JOIN livro l ON l.numero = g.livro " +
            "ORDER BY g.criadoEm DESC",
    )
    suspend fun todosGrifos(): List<GrifoListado>
}
