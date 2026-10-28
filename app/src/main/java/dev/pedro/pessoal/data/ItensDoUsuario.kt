package dev.pedro.pessoal.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import dev.pedro.pessoal.domain.model.Atributo
import dev.pedro.pessoal.domain.model.ExercicioPessoal
import dev.pedro.pessoal.domain.model.LembretePessoal
import dev.pedro.pessoal.domain.model.paraTexto
import dev.pedro.pessoal.domain.model.textoParaDias
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

/**
 * Os lembretes e exercícios que a pessoa cria.
 *
 * Trazem [usuarioId] desde a primeira versão de propósito: as tabelas antigas ainda não têm
 * dono, porque o app nasceu de um usuário só, e corrigir isso é uma migração à parte. Nas
 * tabelas novas sai mais barato já nascer certo do que migrar duas vezes.
 */
@Entity(tableName = "lembrete")
data class LembreteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: String,
    val titulo: String,
    val descricao: String,
    /** Minutos desde a meia-noite: 19h30 vira 1170. Ordena sozinho numa consulta. */
    val minutoDoDia: Int,
    /** Dias da semana em texto, "1,3,5". Ver `paraTexto` em ItemPessoal.kt. */
    val dias: String,
    val atributo: String,
    val criadoEm: Long,
) {
    fun paraDominio() = LembretePessoal(
        id = id,
        titulo = titulo,
        descricao = descricao,
        hora = LocalTime.of(minutoDoDia / 60, minutoDoDia % 60),
        dias = textoParaDias(dias),
        atributo = runCatching { Atributo.valueOf(atributo) }.getOrDefault(Atributo.DISCIPLINA),
    )

    companion object {
        fun de(lembrete: LembretePessoal, usuarioId: String, criadoEm: Long) = LembreteEntity(
            id = lembrete.id,
            usuarioId = usuarioId,
            titulo = lembrete.titulo.trim(),
            descricao = lembrete.descricao.trim(),
            minutoDoDia = lembrete.hora.hour * 60 + lembrete.hora.minute,
            dias = lembrete.dias.paraTexto(),
            atributo = lembrete.atributo.name,
            criadoEm = criadoEm,
        )
    }
}

@Entity(tableName = "exercicio")
data class ExercicioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: String,
    val nome: String,
    val series: Int,
    val repeticoes: Int,
    val porTempo: Boolean,
    val dias: String,
    val criadoEm: Long,
) {
    fun paraDominio() = ExercicioPessoal(
        id = id,
        nome = nome,
        series = series,
        repeticoes = repeticoes,
        porTempo = porTempo,
        dias = textoParaDias(dias),
    )

    companion object {
        fun de(exercicio: ExercicioPessoal, usuarioId: String, criadoEm: Long) = ExercicioEntity(
            id = exercicio.id,
            usuarioId = usuarioId,
            nome = exercicio.nome.trim(),
            series = exercicio.series,
            repeticoes = exercicio.repeticoes,
            porTempo = exercicio.porTempo,
            dias = exercicio.dias.paraTexto(),
            criadoEm = criadoEm,
        )
    }
}

@Dao
interface ItensDao {

    @Query("SELECT * FROM lembrete WHERE usuarioId = :usuarioId ORDER BY minutoDoDia, titulo")
    fun observarLembretes(usuarioId: String): Flow<List<LembreteEntity>>

    @Query("SELECT * FROM lembrete WHERE usuarioId = :usuarioId ORDER BY minutoDoDia, titulo")
    suspend fun buscarLembretes(usuarioId: String): List<LembreteEntity>

    /** Todos, de todos os usuários: o reagendamento depois do boot não sabe quem está logado. */
    @Query("SELECT * FROM lembrete")
    suspend fun todosOsLembretes(): List<LembreteEntity>

    @Upsert
    suspend fun gravarLembrete(lembrete: LembreteEntity): Long

    @Query("DELETE FROM lembrete WHERE id = :id")
    suspend fun apagarLembrete(id: Long)

    @Query("SELECT * FROM exercicio WHERE usuarioId = :usuarioId ORDER BY nome")
    fun observarExercicios(usuarioId: String): Flow<List<ExercicioEntity>>

    @Query("SELECT * FROM exercicio WHERE usuarioId = :usuarioId ORDER BY nome")
    suspend fun buscarExercicios(usuarioId: String): List<ExercicioEntity>

    @Upsert
    suspend fun gravarExercicio(exercicio: ExercicioEntity): Long

    @Query("DELETE FROM exercicio WHERE id = :id")
    suspend fun apagarExercicio(id: Long)

    @Query("DELETE FROM lembrete WHERE usuarioId = :usuarioId")
    suspend fun limparLembretes(usuarioId: String)

    @Query("DELETE FROM exercicio WHERE usuarioId = :usuarioId")
    suspend fun limparExercicios(usuarioId: String)
}
