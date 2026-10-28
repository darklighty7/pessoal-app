package dev.pedro.pessoal.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.pedro.pessoal.domain.model.Atributo
import dev.pedro.pessoal.domain.model.Classe
import dev.pedro.pessoal.domain.model.Habito
import dev.pedro.pessoal.domain.model.Status
import dev.pedro.pessoal.domain.model.Titulo

/**
 * O Status do personagem gravado no banco. Só existe uma linha (id = 1).
 * Guarda tipos simples; as enumerações viram texto e os atributos viram colunas.
 * O rank não é gravado porque é calculado a partir do nível.
 */
@Entity(tableName = "status")
data class StatusEntity(
    @PrimaryKey val id: Int = 1,
    val nome: String,
    val nivel: Int,
    val xp: Int,
    val classe: String,
    val titulo: String,
    val classeCandidata: String?,
    val diasCandidata: Int,
    val classeEscolhida: String? = null,
    val admin: Boolean = false,
    /** Nomes separados por vírgula; vazio quer dizer nenhum desligado. */
    val habitosDesligados: String = "",
    val forca: Int,
    val inteligencia: Int,
    val disciplina: Int,
    val espirito: Int,
) {
    fun paraDominio(): Status = Status(
        nome = nome,
        nivel = nivel,
        xp = xp,
        atributos = mapOf(
            Atributo.FORCA to forca,
            Atributo.INTELIGENCIA to inteligencia,
            Atributo.DISCIPLINA to disciplina,
            Atributo.ESPIRITO to espirito,
        ),
        classe = Classe.valueOf(classe),
        titulo = Titulo.valueOf(titulo),
        classeCandidata = classeCandidata?.let { Classe.valueOf(it) },
        diasCandidata = diasCandidata,
        classeEscolhida = classeEscolhida?.let { Classe.valueOf(it) },
        admin = admin,
        habitosDesligados = habitosDesligados
            .split(",")
            .mapNotNull { nome -> runCatching { Habito.valueOf(nome.trim()) }.getOrNull() }
            .toSet(),
    )

    companion object {
        fun de(status: Status) = StatusEntity(
            nome = status.nome,
            nivel = status.nivel,
            xp = status.xp,
            classe = status.classe.name,
            titulo = status.titulo.name,
            classeCandidata = status.classeCandidata?.name,
            diasCandidata = status.diasCandidata,
            classeEscolhida = status.classeEscolhida?.name,
            admin = status.admin,
            habitosDesligados = status.habitosDesligados.joinToString(",") { it.name },
            forca = status.valor(Atributo.FORCA),
            inteligencia = status.valor(Atributo.INTELIGENCIA),
            disciplina = status.valor(Atributo.DISCIPLINA),
            espirito = status.valor(Atributo.ESPIRITO),
        )
    }
}
