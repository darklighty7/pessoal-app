package dev.pedro.pessoal.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.pedro.pessoal.data.auth.AutenticacaoRepositorio
import dev.pedro.pessoal.data.auth.ResultadoAuth
import dev.pedro.pessoal.data.nuvem.ContadorRepositorio
import dev.pedro.pessoal.data.nuvem.PerfilRepositorio
import dev.pedro.pessoal.domain.model.Classe
import dev.pedro.pessoal.domain.model.PerfilUsuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Em que ponto da entrada o app está. */
sealed interface EstadoSessao {
    /** Ainda perguntando ao Firebase se existe sessão salva. */
    data object Carregando : EstadoSessao
    data object Deslogado : EstadoSessao
    /** Logado, mas o personagem ainda não foi criado. */
    data class SemPersonagem(val uid: String, val email: String) : EstadoSessao
    /** Personagem recém-criado: roda a abertura antes de entrar no app. */
    data class BoasVindas(val perfil: PerfilUsuario) : EstadoSessao
    data class Pronto(val perfil: PerfilUsuario) : EstadoSessao
}

/** O que a tela de login mostra enquanto o usuário digita. */
data class FormularioLogin(
    val email: String = "",
    val senha: String = "",
    val criandoConta: Boolean = false,
    val ocupado: Boolean = false,
    val erro: String? = null,
)

/**
 * Entrada do app: login, cadastro e criação do personagem.
 *
 * O personagem vive em dois lugares de propósito: o perfil (nome, classe escolhida, se é
 * ADM) fica no Firestore, para o usuário reencontrar em qualquer aparelho; o status do dia
 * a dia (XP, atributos, hábitos marcados) continua no banco local, que é o que o motor de
 * pontuação usa e precisa funcionar sem internet.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessaoViewModel(
    private val auth: AutenticacaoRepositorio,
    private val perfis: PerfilRepositorio,
    private val contadores: ContadorRepositorio,
    private val gravarPersonagemLocal: suspend (String, Classe) -> Unit,
) : ViewModel() {

    private val _formulario = MutableStateFlow(FormularioLogin())
    val formulario: StateFlow<FormularioLogin> = _formulario.asStateFlow()

    /** Enquanto o contador de jogadores não responde, a tela de criação fica travada. */
    private val _criandoPersonagem = MutableStateFlow(false)
    val criandoPersonagem: StateFlow<Boolean> = _criandoPersonagem.asStateFlow()

    /**
     * O uid de quem acabou de criar o personagem nesta sessão. Só ele vê a abertura —
     * quem já tem conta e entra noutro aparelho vai direto para o app.
     */
    private val aberturaPendente = MutableStateFlow<String?>(null)

    val estado: StateFlow<EstadoSessao> = auth.observar()
        .flatMapLatest { usuario ->
            if (usuario == null) {
                aberturaPendente.value = null
                flowOf<EstadoSessao>(EstadoSessao.Deslogado)
            } else {
                val semPersonagem = EstadoSessao.SemPersonagem(usuario.uid, usuario.email.orEmpty())
                combine(perfis.observar(usuario.uid), aberturaPendente) { perfil, pendente ->
                    val proximo: EstadoSessao = when {
                        perfil == null || !perfil.completo -> semPersonagem
                        pendente == usuario.uid -> EstadoSessao.BoasVindas(perfil)
                        else -> EstadoSessao.Pronto(perfil)
                    }
                    proximo
                }
                    // Sem internet e sem cópia local do perfil, o jeito é pedir o personagem
                    // de novo; o merge na gravação não apaga nada do que já existe lá.
                    .catch { emit(semPersonagem) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoSessao.Carregando)

    fun escreverEmail(valor: String) { _formulario.value = _formulario.value.copy(email = valor, erro = null) }

    fun escreverSenha(valor: String) { _formulario.value = _formulario.value.copy(senha = valor, erro = null) }

    fun alternarCadastro() {
        _formulario.value = _formulario.value.copy(
            criandoConta = !_formulario.value.criandoConta,
            erro = null,
        )
    }

    fun enviar() {
        val form = _formulario.value
        if (form.ocupado) return
        val erroLocal = validar(form)
        if (erroLocal != null) {
            _formulario.value = form.copy(erro = erroLocal)
            return
        }
        _formulario.value = form.copy(ocupado = true, erro = null)
        viewModelScope.launch {
            val resultado = if (form.criandoConta) {
                auth.cadastrar(form.email, form.senha)
            } else {
                auth.entrar(form.email, form.senha)
            }
            _formulario.value = when (resultado) {
                is ResultadoAuth.Sucesso -> FormularioLogin() // limpa a senha da memória
                is ResultadoAuth.Erro -> _formulario.value.copy(ocupado = false, erro = resultado.mensagem)
            }
        }
    }

    fun criarPersonagem(uid: String, email: String, nome: String, classe: Classe) {
        if (_criandoPersonagem.value) return
        _criandoPersonagem.value = true
        // Marcado antes de qualquer gravação: assim o perfil não chega ao app sem passar
        // pela abertura, por mais rápido que o Firestore responda.
        aberturaPendente.value = uid
        viewModelScope.launch {
            gravarPersonagemLocal(nome, classe)
            // A transação do contador exige servidor. Sem rede ela falha, e aí o jogador
            // fica sem número — a abertura mostra só o nome, em vez de travar na espera.
            val numero = withTimeoutOrNull(ESPERA_CONTADOR_MS) {
                runCatching { contadores.proximoNumero() }.getOrNull()
            } ?: ContadorRepositorio.SEM_NUMERO
            _criandoPersonagem.value = false
            runCatching { perfis.salvarPersonagem(uid, email, nome, classe, numero) }
            // Se o Firestore estiver fora do ar, o personagem já está gravado no aparelho
            // e o envio fica na fila do próprio Firestore.
        }
    }

    /** Chamado quando a abertura termina: daqui em diante é o app normal. */
    fun concluirAbertura() { aberturaPendente.value = null }

    fun sair() = auth.sair()

    private fun validar(form: FormularioLogin): String? = when {
        form.email.isBlank() -> "Digite o seu e-mail."
        !form.email.contains("@") -> "Esse e-mail não parece completo."
        form.senha.length < 6 -> "A senha precisa de pelo menos 6 caracteres."
        else -> null
    }

    companion object {
        private const val ESPERA_CONTADOR_MS = 8_000L

        fun fabrica(
            auth: AutenticacaoRepositorio,
            perfis: PerfilRepositorio,
            contadores: ContadorRepositorio,
            gravarPersonagemLocal: suspend (String, Classe) -> Unit,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SessaoViewModel(auth, perfis, contadores, gravarPersonagemLocal) as T
        }
    }
}
