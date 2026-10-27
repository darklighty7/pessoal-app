package dev.pedro.pessoal.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.pedro.pessoal.data.auth.AutenticacaoRepositorio
import dev.pedro.pessoal.data.auth.ResultadoAuth
import dev.pedro.pessoal.data.nuvem.PerfilRepositorio
import dev.pedro.pessoal.domain.model.Classe
import dev.pedro.pessoal.domain.model.PerfilUsuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Em que ponto da entrada o app está. */
sealed interface EstadoSessao {
    /** Ainda perguntando ao Firebase se existe sessão salva. */
    data object Carregando : EstadoSessao
    data object Deslogado : EstadoSessao
    /** Logado, mas o personagem ainda não foi criado. */
    data class SemPersonagem(val uid: String, val email: String) : EstadoSessao
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
    private val gravarPersonagemLocal: suspend (String, Classe) -> Unit,
) : ViewModel() {

    private val _formulario = MutableStateFlow(FormularioLogin())
    val formulario: StateFlow<FormularioLogin> = _formulario.asStateFlow()

    val estado: StateFlow<EstadoSessao> = auth.observar()
        .flatMapLatest { usuario ->
            if (usuario == null) {
                flowOf<EstadoSessao>(EstadoSessao.Deslogado)
            } else {
                val semPersonagem = EstadoSessao.SemPersonagem(usuario.uid, usuario.email.orEmpty())
                perfis.observar(usuario.uid)
                    .map<PerfilUsuario?, EstadoSessao> { perfil ->
                        if (perfil != null && perfil.completo) EstadoSessao.Pronto(perfil) else semPersonagem
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
        viewModelScope.launch {
            gravarPersonagemLocal(nome, classe)
            runCatching { perfis.salvarPersonagem(uid, email, nome, classe) }
            // Se o Firestore estiver fora do ar, o personagem já está gravado no aparelho
            // e o envio fica na fila do próprio Firestore.
        }
    }

    fun sair() = auth.sair()

    private fun validar(form: FormularioLogin): String? = when {
        form.email.isBlank() -> "Digite o seu e-mail."
        !form.email.contains("@") -> "Esse e-mail não parece completo."
        form.senha.length < 6 -> "A senha precisa de pelo menos 6 caracteres."
        else -> null
    }

    companion object {
        fun fabrica(
            auth: AutenticacaoRepositorio,
            perfis: PerfilRepositorio,
            gravarPersonagemLocal: suspend (String, Classe) -> Unit,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SessaoViewModel(auth, perfis, gravarPersonagemLocal) as T
        }
    }
}
