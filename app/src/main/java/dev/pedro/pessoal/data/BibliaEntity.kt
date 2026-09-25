package dev.pedro.pessoal.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
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
}
