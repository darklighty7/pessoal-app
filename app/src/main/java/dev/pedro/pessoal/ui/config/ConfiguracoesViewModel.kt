package dev.pedro.pessoal.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.pedro.pessoal.data.Repositorio
import dev.pedro.pessoal.data.auth.AutenticacaoRepositorio
import dev.pedro.pessoal.data.nuvem.ContadorRepositorio
import dev.pedro.pessoal.data.nuvem.PerfilRepositorio
import dev.pedro.pessoal.domain.model.Habito
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** O que a tela de configurações precisa além do perfil e do status, que já vêm de fora. */
data class EstadoConfiguracoes(
    val ocupado: Boolean = false,
    val totalJogadores: Int? = null,
    /** Erro ou confirmação para mostrar embaixo da ação que acabou de rodar. */
    val recado: String? = null,
)

class ConfiguracoesViewModel(
    private val repositorio: Repositorio,
    private val auth: AutenticacaoRepositorio,
    private val perfis: PerfilRepositorio,
    private val contadores: ContadorRepositorio,
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoConfiguracoes())
    val estado: StateFlow<EstadoConfiguracoes> = _estado.asStateFlow()

    fun limparRecado() {
        _estado.value = _estado.value.copy(recado = null)
    }

    fun carregarTotalDeJogadores() {
        if (_estado.value.totalJogadores != null) return
        viewModelScope.launch {
            val total = runCatching { contadores.total() }.getOrNull() ?: return@launch
            _estado.value = _estado.value.copy(totalJogadores = total)
        }
    }

    fun renomear(uid: String, email: String, nome: String) {
        val limpo = nome.trim()
        if (limpo.length < 2) return
        viewModelScope.launch {
            repositorio.definirNome(limpo)
            runCatching { perfis.salvarNome(uid, email, limpo) }
            _estado.value = _estado.value.copy(recado = "Nome atualizado.")
        }
    }

    fun alternarHabito(habito: Habito) {
        viewModelScope.launch { repositorio.alternarHabito(habito) }
    }

    fun zerarProgresso() {
        viewModelScope.launch {
            repositorio.zerarProgresso()
            _estado.value = _estado.value.copy(recado = "Progresso zerado.")
        }
    }

    /**
     * Apaga a conta inteira: nuvem, aparelho e login.
     *
     * A ordem importa e não é a óbvia. O Firebase exige um login recente para apagar a
     * conta, e só diz isso depois da tentativa — perguntar antes evita o pior cenário, que
     * é apagar os dados e descobrir que a conta continua de pé.
     *
     * Depois disso, os dados saem antes do login, porque as regras do Firestore só deixam o
     * dono apagar o próprio perfil: sem usuário logado, o documento ficaria órfão para
     * sempre, sem ninguém com direito de removê-lo.
     */
    fun apagarConta(uid: String) {
        if (_estado.value.ocupado) return
        if (auth.precisaEntrarDeNovo()) {
            _estado.value = _estado.value.copy(
                recado = "Por segurança, saia e entre de novo antes de apagar a conta.",
            )
            return
        }

        _estado.value = _estado.value.copy(ocupado = true, recado = null)
        viewModelScope.launch {
            runCatching { perfis.apagar(uid) }
            repositorio.apagarTudoLocal()
            val erro = auth.apagarConta()
            _estado.value = _estado.value.copy(ocupado = false, recado = erro)
            // Deu certo: o ouvinte do Auth percebe que não há mais ninguém logado e a
            // Raiz volta sozinha para a tela de entrada.
        }
    }

    fun sair() = auth.sair()

    companion object {
        fun fabrica(
            repositorio: Repositorio,
            auth: AutenticacaoRepositorio,
            perfis: PerfilRepositorio,
            contadores: ContadorRepositorio,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ConfiguracoesViewModel(repositorio, auth, perfis, contadores) as T
        }
    }
}
