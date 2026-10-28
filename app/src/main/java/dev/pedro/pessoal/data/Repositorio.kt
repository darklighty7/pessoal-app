package dev.pedro.pessoal.data

import dev.pedro.pessoal.domain.engine.MotorPontuacao
import dev.pedro.pessoal.domain.engine.Regras
import dev.pedro.pessoal.domain.model.Atributo
import dev.pedro.pessoal.domain.model.Classe
import dev.pedro.pessoal.domain.model.ExercicioPessoal
import dev.pedro.pessoal.domain.model.Habito
import dev.pedro.pessoal.domain.model.LembretePessoal
import dev.pedro.pessoal.domain.model.Recompensa
import dev.pedro.pessoal.domain.model.ResultadoHabito
import dev.pedro.pessoal.domain.model.Status
import dev.pedro.pessoal.domain.model.Treino
import dev.pedro.pessoal.domain.model.TREINOS_PARA_PROGREDIR
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Ponte entre o banco e o motor de pontuação. É aqui que marcar uma tarefa vira XP,
 * e que os dias que passaram sem o app ser aberto são fechados.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Repositorio(
    private val statusDao: StatusDao,
    private val registroDao: RegistroDao,
    private val itensDao: ItensDao,
) {
    /**
     * De quem é o banco agora. As tabelas antigas ainda não têm dono — o app nasceu de um
     * usuário só —, mas lembretes e exercícios já nascem separados por conta.
     */
    private val usuarioAtual = MutableStateFlow<String?>(null)

    /**
     * Liga o banco local à conta que entrou. O `admin` desce para o status porque o
     * fechamento do dia precisa saber quais hábitos fixos valem, e ele não fala com a nuvem.
     */
    suspend fun definirUsuario(uid: String, admin: Boolean) {
        usuarioAtual.value = uid
        val atual = status()
        if (atual.admin != admin) gravar(atual.copy(admin = admin))
        // Agora que o dono é conhecido, os dias que passaram podem ser fechados.
        fecharDiasPendentes()
    }
    fun observarStatus(): Flow<Status> = statusDao.observar().map { it?.paraDominio() ?: Status() }

    fun observarDia(data: LocalDate): Flow<List<RegistroEntity>> = registroDao.observarDia(data.toString())

    private suspend fun status(): Status = statusDao.buscar()?.paraDominio() ?: Status()

    private suspend fun gravar(status: Status) = statusDao.gravar(StatusEntity.de(status))

    /** Primeiro passo da entrada: só o nome. */
    suspend fun definirNome(nome: String) {
        gravar(status().copy(nome = nome.trim()))
    }

    /**
     * Último passo: a classe escolhida, que entra também como [Status.classeEscolhida] para
     * o motor saber que ela é um ponto de partida e não um resultado dos atributos.
     */
    suspend fun definirClasse(classe: Classe) {
        val atual = status()
        // O bônus da classe só vale para quem está começando do zero. Antes do login o app
        // era de um usuário só, então pode existir um personagem com progresso de verdade
        // passando por esta tela — e aí os atributos dele são dele, não um ponto de partida.
        val intocado = atual.nivel == 1 &&
            atual.xp == 0 &&
            atual.atributos.values.all { it == Regras.VALOR_INICIAL_ATRIBUTO }

        gravar(
            atual.copy(
                classe = classe,
                classeEscolhida = classe,
                classeCandidata = null,
                diasCandidata = 0,
                atributos = if (intocado) MotorPontuacao.atributosIniciais(classe) else atual.atributos,
            ),
        )
    }

    /** Marca ou desmarca um hábito no dia, aplicando (ou devolvendo) a recompensa na hora. */
    suspend fun marcar(habito: Habito, data: LocalDate, concluido: Boolean) {
        marcarChave(habito.name, habito.tipo?.recompensa, data, concluido)
    }

    /**
     * O caminho comum de marcar e desmarcar, para hábito fixo e lembrete criado. A tabela de
     * registros guarda o hábito por nome, então basta a chave ser diferente — um hábito
     * "BIBLIA" e um lembrete de id 3 nunca disputam a mesma linha.
     */
    private suspend fun marcarChave(
        chave: String,
        recompensa: Recompensa?,
        data: LocalDate,
        concluido: Boolean,
    ) {
        val dia = data.toString()
        val atual = registroDao.buscarDia(dia).firstOrNull { it.habito == chave }
        if (atual?.concluido == concluido) return

        registroDao.gravar(
            RegistroEntity(
                data = dia,
                habito = chave,
                concluido = concluido,
                minutosLeitura = atual?.minutosLeitura ?: 0,
            ),
        )

        if (recompensa == null) return
        val s = status()
        val novo = if (concluido) {
            MotorPontuacao.aplicar(s, recompensa)
        } else {
            // Desmarcar devolve o que a marcação deu.
            MotorPontuacao.aplicar(
                s,
                recompensa.copy(xp = -recompensa.xp, ganhos = recompensa.ganhos.mapValues { -it.value }),
            )
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
        val diferenca = Recompensa(
            xp = total.xp - pago.xp,
            ganhos = (total.ganhos.keys + pago.ganhos.keys)
                .associateWith { (total.ganhos[it] ?: 0) - (pago.ganhos[it] ?: 0) }
                .filterValues { it != 0 },
        )

        registroDao.gravar(
            RegistroEntity(
                data = chave,
                habito = Habito.BIBLIA.name,
                concluido = depois >= Regras.META_LEITURA_MIN,
                minutosLeitura = depois,
            ),
        )
        if (diferenca.xp != 0 || diferenca.ganhos.isNotEmpty()) {
            gravar(MotorPontuacao.aplicar(status(), diferenca))
        }
    }

    // ----- Configurações -----

    /** Liga ou desliga um hábito fixo. Só o ADM tem o que desligar. */
    suspend fun alternarHabito(habito: Habito) {
        val atual = status()
        val novos = if (habito in atual.habitosDesligados) {
            atual.habitosDesligados - habito
        } else {
            atual.habitosDesligados + habito
        }
        gravar(atual.copy(habitosDesligados = novos))
    }

    /**
     * Zera o que foi conquistado e mantém quem é o personagem: nome, classe e os hábitos
     * desligados continuam. É recomeçar o jogo, não trocar de jogador.
     */
    suspend fun zerarProgresso() {
        val atual = status()
        registroDao.limparRegistros()
        registroDao.limparDiasFechados()
        registroDao.limparProgresso()
        gravar(
            Status(
                nome = atual.nome,
                classe = atual.classeEscolhida ?: Classe.APRENDIZ,
                classeEscolhida = atual.classeEscolhida,
                admin = atual.admin,
                habitosDesligados = atual.habitosDesligados,
                atributos = atual.classeEscolhida
                    ?.let { MotorPontuacao.atributosIniciais(it) }
                    ?: Status().atributos,
            ),
        )
    }

    /** Apaga tudo o que é desta conta no aparelho. Usado ao apagar a conta. */
    suspend fun apagarTudoLocal() {
        usuarioAtual.value?.let { uid ->
            itensDao.limparLembretes(uid)
            itensDao.limparExercicios(uid)
        }
        registroDao.limparRegistros()
        registroDao.limparDiasFechados()
        registroDao.limparProgresso()
        statusDao.limpar()
        usuarioAtual.value = null
    }

    // ----- Fechamento do dia -----

    suspend fun fecharDiasPendentes(hoje: LocalDate = LocalDate.now()) {
        // Sem saber de quem é o dia, não dá para fechá-lo: os lembretes criados pela pessoa
        // contam no fechamento, e marcá-lo fechado agora os perderia para sempre.
        if (usuarioAtual.value == null) return

        val ultimo = registroDao.ultimoDiaFechado()?.let { LocalDate.parse(it) }
        val comeco = (ultimo?.plusDays(1) ?: hoje.minusDays(1)).coerceGe(hoje.minusDays(60))

        // Os lembretes são os mesmos em todos os dias fechados nesta rodada. Buscar uma vez
        // só: depois de uma viagem longa este laço pode rodar 60 vezes, e seriam 60
        // consultas idênticas ao banco.
        val lembretes = lembretesDoUsuario()

        var dia = comeco
        while (dia.isBefore(hoje)) {
            if (registroDao.diaFechado(dia.toString()) == null) fecharDia(dia, lembretes)
            dia = dia.plusDays(1)
        }
    }

    private suspend fun fecharDia(data: LocalDate, lembretes: List<LembretePessoal>) {
        val atual = status()
        val registros = registroDao.buscarDia(data.toString()).associateBy { it.habito }

        val fixos = Habito.doDia(data, atual.admin, atual.habitosDesligados)
            .filter { it.obrigatorio }
            .map { habito ->
                resultado(habito.atributo, habito.name, data, registros) { habito.ocorreEm(it) }
            }

        // Os lembretes criados contam igual aos fixos. Sem isso, quem monta a própria rotina
        // teria um dia perfeito só com a Bíblia, e tudo o que escolheu fazer não valeria nada.
        val criados = lembretes
            .filter { it.ocorreEm(data) }
            .map { lembrete ->
                resultado(lembrete.atributo, lembrete.chaveRegistro, data, registros) { lembrete.ocorreEm(it) }
            }

        gravar(MotorPontuacao.fecharDia(atual, fixos + criados))
        registroDao.marcarDiaFechado(DiaFechadoEntity(data.toString()))
    }

    private suspend fun resultado(
        atributo: Atributo,
        chave: String,
        data: LocalDate,
        registros: Map<String, RegistroEntity>,
        ocorreEm: (LocalDate) -> Boolean,
    ): ResultadoHabito {
        val cumprido = registros[chave]?.concluido == true
        return ResultadoHabito(
            atributo = atributo,
            cumprido = cumprido,
            falhasSeguidas = if (cumprido) 0 else contar(chave, data, buscando = false, ocorreEm = ocorreEm),
            sequencia = if (cumprido) contar(chave, data, buscando = true, ocorreEm = ocorreEm) else 0,
        )
    }

    /**
     * Conta quantos dias seguidos, terminando em [data], a tarefa foi cumprida
     * ([buscando] verdadeiro) ou falhada ([buscando] falso). Só conta os dias em que
     * ela estava programada.
     */
    private suspend fun contar(
        chave: String,
        data: LocalDate,
        buscando: Boolean,
        ocorreEm: (LocalDate) -> Boolean,
    ): Int {
        val anteriores = registroDao.anteriores(chave, data.toString(), limite = 90)
            .associate { LocalDate.parse(it.data) to it.concluido }
        var total = 1 // o próprio dia que está fechando
        var dia = data.minusDays(1)
        var restantes = 90
        while (restantes-- > 0) {
            if (ocorreEm(dia)) {
                if ((anteriores[dia] ?: false) != buscando) break
                total++
            }
            dia = dia.minusDays(1)
        }
        return total
    }

    // ----- Lembretes e exercícios criados pela pessoa -----

    fun observarLembretes(): Flow<List<LembretePessoal>> = usuarioAtual.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList())
        else itensDao.observarLembretes(uid).map { lista -> lista.map { it.paraDominio() } }
    }

    fun observarExercicios(): Flow<List<ExercicioPessoal>> = usuarioAtual.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList())
        else itensDao.observarExercicios(uid).map { lista -> lista.map { it.paraDominio() } }
    }

    /** Para o agendamento: os lembretes de quem está logado, já no formato do domínio. */
    suspend fun lembretesDoUsuario(): List<LembretePessoal> {
        val uid = usuarioAtual.value ?: return emptyList()
        return itensDao.buscarLembretes(uid).map { it.paraDominio() }
    }

    /** Todos, de todas as contas: o reagendamento depois do boot não sabe quem está logado. */
    suspend fun todosOsLembretes(): List<LembretePessoal> =
        itensDao.todosOsLembretes().map { it.paraDominio() }

    /** Devolve o id gravado — zero para um lembrete novo vira o id de verdade. */
    suspend fun salvarLembrete(lembrete: LembretePessoal): Long {
        val uid = usuarioAtual.value ?: return 0L
        val id = itensDao.gravarLembrete(LembreteEntity.de(lembrete, uid, System.currentTimeMillis()))
        // O Upsert devolve -1 quando atualizou em vez de inserir; aí o id já era o que veio.
        return if (id > 0) id else lembrete.id
    }

    suspend fun apagarLembrete(id: Long) = itensDao.apagarLembrete(id)

    suspend fun salvarExercicio(exercicio: ExercicioPessoal): Long {
        val uid = usuarioAtual.value ?: return 0L
        val id = itensDao.gravarExercicio(ExercicioEntity.de(exercicio, uid, System.currentTimeMillis()))
        return if (id > 0) id else exercicio.id
    }

    suspend fun apagarExercicio(id: Long) = itensDao.apagarExercicio(id)

    /** Marca um lembrete criado. Mesma mecânica dos hábitos fixos, com outra chave. */
    suspend fun marcarPessoal(lembrete: LembretePessoal, data: LocalDate, concluido: Boolean) {
        marcarChave(lembrete.chaveRegistro, lembrete.recompensa, data, concluido)
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
