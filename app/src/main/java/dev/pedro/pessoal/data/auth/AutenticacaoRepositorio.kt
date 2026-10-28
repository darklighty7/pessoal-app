package dev.pedro.pessoal.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Quem está logado agora, com o token de acesso que o Firebase emitiu para esta sessão. */
data class UsuarioAutenticado(
    val uid: String,
    val email: String,
    val token: String?,
)

sealed interface ResultadoAuth {
    data class Sucesso(val usuario: UsuarioAutenticado) : ResultadoAuth
    data class Erro(val mensagem: String) : ResultadoAuth
}

/**
 * Login e cadastro por e-mail e senha no Firebase Auth.
 *
 * Sobre o token: quem emite e renova é o próprio Firebase. A cada login sai um token novo,
 * que vale uma hora e é renovado sozinho enquanto o app estiver aberto. Aqui ele só é lido
 * com [token], para quando precisar chamar algum serviço de fora; as chamadas ao Firestore
 * já vão assinadas sem precisar passar nada.
 */
class AutenticacaoRepositorio(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {

    val uidAtual: String? get() = auth.currentUser?.uid

    /** Emite o usuário logado e volta a emitir a cada login ou logout. */
    fun observar(): Flow<FirebaseUser?> = callbackFlow {
        val ouvinte = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(ouvinte)
        awaitClose { auth.removeAuthStateListener(ouvinte) }
    }

    suspend fun entrar(email: String, senha: String): ResultadoAuth = executar {
        auth.signInWithEmailAndPassword(email.trim(), senha).await().user
    }

    suspend fun cadastrar(email: String, senha: String): ResultadoAuth = executar {
        auth.createUserWithEmailAndPassword(email.trim(), senha).await().user
    }

    fun sair() = auth.signOut()

    /**
     * O Firebase recusa apagar a conta quando o login é antigo, e a recusa só aparece
     * depois da tentativa. Perguntar antes evita o pior cenário: apagar os dados e depois
     * descobrir que a conta não sai.
     */
    fun precisaEntrarDeNovo(): Boolean {
        val entrou = auth.currentUser?.metadata?.lastSignInTimestamp ?: return true
        return System.currentTimeMillis() - entrou > VALIDADE_DO_LOGIN_MS
    }

    /** Apaga a conta do Firebase Auth. Devolve a mensagem do erro, ou nulo em caso de sucesso. */
    suspend fun apagarConta(): String? = try {
        val usuario = auth.currentUser ?: return "Ninguém está logado."
        usuario.delete().await()
        null
    } catch (e: FirebaseAuthRecentLoginRequiredException) {
        "Por segurança, entre de novo antes de apagar a conta."
    } catch (e: FirebaseNetworkException) {
        "Sem conexão. Tente de novo quando houver internet."
    } catch (e: Exception) {
        e.message ?: "Não foi possível apagar a conta."
    }

    /** Token de acesso da sessão atual. [renovar] força um token novo em vez do que está em cache. */
    suspend fun token(renovar: Boolean = false): String? =
        auth.currentUser?.getIdToken(renovar)?.await()?.token  // null quando não há ninguém logado

    private suspend fun executar(acao: suspend () -> FirebaseUser?): ResultadoAuth = try {
        val usuario = acao() ?: return ResultadoAuth.Erro("Não foi possível entrar. Tente de novo.")
        ResultadoAuth.Sucesso(
            UsuarioAutenticado(
                uid = usuario.uid,
                email = usuario.email.orEmpty(),
                token = usuario.getIdToken(false).await().token,
            ),
        )
    } catch (e: FirebaseAuthWeakPasswordException) {
        ResultadoAuth.Erro("Senha fraca: use pelo menos 6 caracteres.")
    } catch (e: FirebaseAuthUserCollisionException) {
        ResultadoAuth.Erro("Já existe uma conta com esse e-mail.")
    } catch (e: FirebaseAuthInvalidUserException) {
        ResultadoAuth.Erro("Não achei uma conta com esse e-mail.")
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        // O Firebase devolve o mesmo erro para e-mail inexistente e senha errada, de propósito.
        ResultadoAuth.Erro("E-mail ou senha incorretos.")
    } catch (e: FirebaseNetworkException) {
        ResultadoAuth.Erro("Sem conexão. Verifique a internet e tente de novo.")
    } catch (e: Exception) {
        // Rede instável, cota do projeto, conta desativada: nada disso deve derrubar a tela.
        ResultadoAuth.Erro("Não consegui entrar agora. Tente de novo em instantes.")
    }
}

/** Depois disso o Firebase passa a exigir um login novo para operações delicadas. */
private const val VALIDADE_DO_LOGIN_MS = 5 * 60 * 1000L
