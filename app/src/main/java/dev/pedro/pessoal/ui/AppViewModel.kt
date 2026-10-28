package dev.pedro.pessoal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.pedro.pessoal.data.ProgressoExercicioEntity
import dev.pedro.pessoal.data.RegistroEntity
import dev.pedro.pessoal.data.Repositorio
import dev.pedro.pessoal.domain.model.ExercicioPessoal
import dev.pedro.pessoal.domain.model.Habito
import dev.pedro.pessoal.domain.model.LembretePessoal
import dev.pedro.pessoal.domain.model.Status
import dev.pedro.pessoal.domain.model.Treino
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Estado compartilhado pelas telas: o status do personagem e o que já foi feito hoje.
 * A data de hoje fica num fluxo próprio para a virada de meia-noite trocar o dia sozinha.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AppViewModel(
    private val repositorio: Repositorio,
    /**
     * Agendar alarme precisa de Context, e ViewModel não guarda Context — a Activity vive e
     * morre, o ViewModel sobrevive, e segurar uma referência vazaria a tela inteira. Então o
     * agendamento entra como função, ligada lá fora no [fabrica].
     */
    private val agendarLembretes: (List<LembretePessoal>) -> Unit = {},
    private val cancelarLembrete: (Long) -> Unit = {},
) : ViewModel() {

    private val _hoje = MutableStateFlow(LocalDate.now())
    val hoje: StateFlow<LocalDate> = _hoje.asStateFlow()

    val status: StateFlow<Status> = repositorio.observarStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Status())

    val registrosDeHoje: StateFlow<List<RegistroEntity>> = _hoje
        .flatMapLatest { repositorio.observarDia(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val progressoDoTreino: StateFlow<Map<String, ProgressoExercicioEntity>> = _hoje
        .flatMapLatest { data ->
            val treino = Treino.doDia(data)
            if (treino == null) kotlinx.coroutines.flow.flowOf(emptyMap())
            else repositorio.observarProgresso(treino)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val lembretesPessoais: StateFlow<List<LembretePessoal>> = repositorio.observarLembretes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val exerciciosPessoais: StateFlow<List<ExercicioPessoal>> = repositorio.observarExercicios()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { repositorio.fecharDiasPendentes() }
    }

    /**
     * Liga o banco local à conta que entrou. Até isto acontecer, os lembretes criados não
     * aparecem e os dias não fecham — nenhum dos dois sabe ainda de quem é o dia.
     */
    fun entrarComo(uid: String, admin: Boolean) {
        viewModelScope.launch {
            repositorio.definirUsuario(uid, admin)
            agendarLembretes(repositorio.lembretesDoUsuario())
        }
    }

    /** Chamado quando o app volta do segundo plano: pega a virada de dia. */
    fun revisarData() {
        val agora = LocalDate.now()
        if (agora != _hoje.value) {
            _hoje.value = agora
            viewModelScope.launch { repositorio.fecharDiasPendentes(agora) }
        }
    }

    fun marcar(habito: Habito, concluido: Boolean) {
        viewModelScope.launch { repositorio.marcar(habito, _hoje.value, concluido) }
    }

    fun registrarLeitura(minutos: Int) {
        viewModelScope.launch { repositorio.registrarLeitura(_hoje.value, minutos) }
    }

    fun concluirTreino(treino: Treino) {
        viewModelScope.launch { repositorio.concluirTreino(treino, _hoje.value) }
    }

    // ----- O que a pessoa cria -----

    fun marcarPessoal(lembrete: LembretePessoal, concluido: Boolean) {
        viewModelScope.launch { repositorio.marcarPessoal(lembrete, _hoje.value, concluido) }
    }

    fun salvarLembrete(lembrete: LembretePessoal) {
        viewModelScope.launch {
            val id = repositorio.salvarLembrete(lembrete)
            // Reagenda só o que mudou, com o id que o banco devolveu — um lembrete novo chega
            // aqui com id zero, e agendar com zero sobrescreveria o alarme de outro.
            if (id > 0) agendarLembretes(listOf(lembrete.copy(id = id)))
        }
    }

    fun apagarLembrete(id: Long) {
        viewModelScope.launch {
            cancelarLembrete(id)
            repositorio.apagarLembrete(id)
        }
    }

    fun salvarExercicio(exercicio: ExercicioPessoal) {
        viewModelScope.launch { repositorio.salvarExercicio(exercicio) }
    }

    fun apagarExercicio(id: Long) {
        viewModelScope.launch { repositorio.apagarExercicio(id) }
    }

    companion object {
        fun fabrica(
            repositorio: Repositorio,
            agendarLembretes: (List<LembretePessoal>) -> Unit = {},
            cancelarLembrete: (Long) -> Unit = {},
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AppViewModel(repositorio, agendarLembretes, cancelarLembrete) as T
        }
    }
}
