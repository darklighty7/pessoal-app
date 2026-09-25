package dev.pedro.pessoal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.pedro.pessoal.data.ProgressoExercicioEntity
import dev.pedro.pessoal.data.RegistroEntity
import dev.pedro.pessoal.data.Repositorio
import dev.pedro.pessoal.domain.model.Habito
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
class AppViewModel(private val repositorio: Repositorio) : ViewModel() {

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

    init {
        viewModelScope.launch { repositorio.fecharDiasPendentes() }
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

    companion object {
        fun fabrica(repositorio: Repositorio) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(repositorio) as T
        }
    }
}
