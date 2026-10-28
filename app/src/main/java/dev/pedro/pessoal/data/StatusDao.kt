package dev.pedro.pessoal.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusDao {
    @Query("SELECT * FROM status WHERE id = 1")
    fun observar(): Flow<StatusEntity?>

    @Query("SELECT * FROM status WHERE id = 1")
    suspend fun buscar(): StatusEntity?

    @Upsert
    suspend fun gravar(status: StatusEntity)

    @Query("DELETE FROM status")
    suspend fun limpar()
}
