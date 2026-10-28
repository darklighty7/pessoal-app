package dev.pedro.pessoal.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * O que foi feito em cada dia. Uma linha por hábito por dia.
 * [data] é gravada como texto no formato aaaa-mm-dd para ordenar sozinha.
 */
@Entity(tableName = "registro", primaryKeys = ["data", "habito"])
data class RegistroEntity(
    val data: String,
    val habito: String,
    val concluido: Boolean = false,
    /** Só a leitura da Bíblia usa: total de minutos lidos no dia. */
    val minutosLeitura: Int = 0,
)

/** Marca os dias já fechados pelo motor, para não fechar o mesmo dia duas vezes. */
@Entity(tableName = "dia_fechado")
data class DiaFechadoEntity(
    @androidx.room.PrimaryKey val data: String,
)

/** Em que variação de cada exercício o treino está, e quantos treinos completos já saíram. */
@Entity(tableName = "progresso_exercicio", primaryKeys = ["treino", "exercicio"])
data class ProgressoExercicioEntity(
    val treino: String,
    val exercicio: String,
    val nivel: Int = 0,
    val treinosCompletos: Int = 0,
)

@Dao
interface RegistroDao {
    @Query("SELECT * FROM registro WHERE data = :data")
    fun observarDia(data: String): Flow<List<RegistroEntity>>

    @Query("SELECT * FROM registro WHERE data = :data")
    suspend fun buscarDia(data: String): List<RegistroEntity>

    @Query("SELECT * FROM registro WHERE habito = :habito AND data < :data ORDER BY data DESC LIMIT :limite")
    suspend fun anteriores(habito: String, data: String, limite: Int): List<RegistroEntity>

    @Upsert
    suspend fun gravar(registro: RegistroEntity)

    @Query("SELECT * FROM dia_fechado WHERE data = :data")
    suspend fun diaFechado(data: String): DiaFechadoEntity?

    @Query("SELECT data FROM dia_fechado ORDER BY data DESC LIMIT 1")
    suspend fun ultimoDiaFechado(): String?

    @Upsert
    suspend fun marcarDiaFechado(dia: DiaFechadoEntity)

    @Query("SELECT * FROM progresso_exercicio WHERE treino = :treino")
    fun observarProgresso(treino: String): Flow<List<ProgressoExercicioEntity>>

    @Query("SELECT * FROM progresso_exercicio WHERE treino = :treino")
    suspend fun buscarProgresso(treino: String): List<ProgressoExercicioEntity>

    @Upsert
    suspend fun gravarProgresso(progresso: ProgressoExercicioEntity)

    @Query("DELETE FROM registro")
    suspend fun limparRegistros()

    @Query("DELETE FROM dia_fechado")
    suspend fun limparDiasFechados()

    @Query("DELETE FROM progresso_exercicio")
    suspend fun limparProgresso()
}
