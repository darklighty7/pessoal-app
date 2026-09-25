package dev.pedro.pessoal.data

import dev.pedro.pessoal.domain.engine.MotorPontuacao
import dev.pedro.pessoal.domain.engine.Regras
import dev.pedro.pessoal.domain.model.Habito
import dev.pedro.pessoal.domain.model.ResultadoHabito
import dev.pedro.pessoal.domain.model.Status
import dev.pedro.pessoal.domain.model.Treino
import dev.pedro.pessoal.domain.model.TREINOS_PARA_PROGREDIR
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Ponte entre o banco e o motor de pontuação. É aqui que marcar uma tarefa vira XP,
 * e que os dias que passaram sem o app ser aberto são fechados.
 */
class Repositorio(
    private val statusDao: StatusDao,
    private val registroDao: RegistroDao,
) {
    fun observarStatus(): Flow<Status> = statusDao.observar().map { it?.paraDominio() ?: Status() }

    fun observarDia(data: LocalDate): Flow<List<RegistroEntity>> = registroDao.observarDia(data.toString())

    private suspend fun status(): Status = statusDao.buscar()?.paraDominio() ?: Status()

    private suspend fun gravar(status: Status) = statusDao.gravar(StatusEntity.de(status))

    /** Marca ou desmarca um hábito no dia, aplicando (ou devolvendo) a recompensa na hora. */
    suspend fun marcar(habito: Habito, data: LocalDate, concluido: Boolean) {
        val chave = data.toString()
        val atual = registroDao.buscarDia(chave).firstOrNull { it.habito == habito.name }
        if (atual?.concluido == concluido) return

        registroDao.gravar(
            RegistroEntity(
                data = chave,
                habito = habito.name,
                concluido = concluido,
                minutosLeitura = atual?.minutosLeitura ?: 0,
            ),
        )

        val recompensa = habito.tipo?.recompensa ?: return
        val s = status()
        val novo = if (concluido) {
            MotorPontuacao.aplicar(s, recompensa)
        } else {
            // Desmarcar devolve o que a marcação deu.
            MotorPontuacao.aplicar(s, recompensa.copy(xp = -recompensa.xp, ganho = -recompensa.ganho))
        }
        gravar(novo)
    }

    /**
     * Soma minutos de leitura ao dia. A recompensa é recalculada por diferença: o que já
     * havia sido pago pelos minutos anteriores é descontado, então ler em várias sessões
     * rende o mesmo que ler tudo de uma vez.
     */
    suspend fun registrarLeitura(data: LocalDate, minutos: Int) {
        if (minutos <= 0) return
        val chave = data.toString()
        val atual = registroDao.buscarDia(chave).firstOrNull { it.habito == Habito.BIBLIA.name }
        val antes = atual?.minutosLeitura ?: 0
        val depois = antes + minutos

        val pago = MotorPontuacao.recompensaLeitura(antes)
        val total = MotorPontuacao.recompensaLeitura(depois)
        val diferenca = total.copy(xp = total.xp - pago.xp, ganho = total.ganho - pago.ganho)

        registroDao.gravar(
            RegistroEntity(
                data = chave,
                habito = Habito.BIBLIA.name,
                concluido = depois >= Regras.META_LEITURA_MIN,
                minutosLeitura = depois,
            ),
        )
        if (diferenca.xp != 0 || diferenca.ganho != 0) gravar(MotorPontuacao.aplicar(status(), diferenca))
    }

    /**
     * Fecha todos os dias que já terminaram e ainda não foram fechados, do mais antigo
     * para o mais novo. Roda quando o app abre, então ficar dias sem abrir não quebra a conta.
     * Limita a 60 dias para não travar depois de um sumiço longo.
     */
    suspend fun fecharDiasPendentes(hoje: LocalDate = LocalDate.now()) {
        val ultimo = registroDao.ultimoDiaFechado()?.let { LocalDate.parse(it) }
        val comeco = (ultimo?.plusDays(1) ?: hoje.minusDays(1)).coerceGe(hoje.minusDays(60))
        var dia = comeco
        while (dia.isBefore(hoje)) {
            if (registroDao.diaFechado(dia.toString()) == null) fecharDia(dia)
            dia = dia.plusDays(1)
        }
    }

    private suspend fun fecharDia(data: LocalDate) {
        val registros = registroDao.buscarDia(data.toString()).associateBy { it.habito }
        val habitos = Habito.doDia(data).filter { it.obrigatorio }.map { habito ->
            val cumprido = registros[habito.name]?.concluido == true
            ResultadoHabito(
                atributo = habito.atributo,
                cumprido = cumprido,
                falhasSeguidas = if (cumprido) 0 else contar(habito, data, buscando = false),
                sequencia = if (cumprido) contar(habito, data, buscando = true) else 0,
            )
        }
        gravar(MotorPontuacao.fecharDia(status(), habitos))
        registroDao.marcarDiaFechado(DiaFechadoEntity(data.toString()))
    }

    /**
     * Conta quantos dias seguidos, terminando em [data], o hábito foi cumprido
     * ([buscando] verdadeiro) ou falhado ([buscando] falso). Só conta os dias em que
     * o hábito estava programado.
     */
    private suspend fun contar(habito: Habito, data: LocalDate, buscando: Boolean): Int {
        val anteriores = registroDao.anteriores(habito.name, data.toString(), limite = 90)
            .associate { LocalDate.parse(it.data) to it.concluido }
        var total = 1 // o próprio dia que está fechando
        var dia = data.minusDays(1)
        var restantes = 90
        while (restantes-- > 0) {
            if (habito.ocorreEm(dia)) {
                if ((anteriores[dia] ?: false) != buscando) break
                total++
            }
            dia = dia.minusDays(1)
        }
        return total
    }

    // ----- Treinos -----

    fun observarProgresso(treino: Treino): Flow<Map<String, ProgressoExercicioEntity>> =
        registroDao.observarProgresso(treino.name).map { lista -> lista.associateBy { it.exercicio } }

    /**
     * Fecha um treino: marca o hábito do dia e conta mais um treino completo em cada
     * exercício, subindo de variação quando chega na contagem combinada.
     */
    suspend fun concluirTreino(treino: Treino, data: LocalDate) {
        val progresso = registroDao.buscarProgresso(treino.name).associateBy { it.exercicio }
        for (exercicio in treino.exercicios) {
            val atual = progresso[exercicio.nome]
                ?: ProgressoExercicioEntity(treino.name, exercicio.nome)
            val completos = atual.treinosCompletos + 1
            val sobe = completos >= TREINOS_PARA_PROGREDIR && exercicio.podeProgredir(atual.nivel)
            registroDao.gravarProgresso(
                atual.copy(
                    nivel = if (sobe) atual.nivel + 1 else atual.nivel,
                    treinosCompletos = if (sobe) 0 else completos,
                ),
            )
        }
        val habito = if (treino == Treino.MOBILIDADE) Habito.MOBILIDADE else Habito.TREINO_CASA
        marcar(habito, data, concluido = true)
    }
}

private fun LocalDate.coerceGe(minimo: LocalDate): LocalDate = if (isBefore(minimo)) minimo else this
