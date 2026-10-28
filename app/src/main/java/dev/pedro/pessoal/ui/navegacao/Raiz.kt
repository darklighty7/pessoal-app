package dev.pedro.pessoal.ui.navegacao

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pedro.pessoal.ui.AppViewModel
import dev.pedro.pessoal.ui.auth.ClasseTela
import dev.pedro.pessoal.ui.auth.EstadoSessao
import dev.pedro.pessoal.ui.auth.LoginTela
import dev.pedro.pessoal.ui.auth.NomeTela
import dev.pedro.pessoal.ui.auth.SessaoViewModel
import dev.pedro.pessoal.ui.biblia.BibliaViewModel
import dev.pedro.pessoal.ui.boasvindas.BoasVindasTela
import dev.pedro.pessoal.ui.theme.Fundo
import dev.pedro.pessoal.ui.theme.Neon

/**
 * Porta de entrada: decide entre login, criação do personagem e o app em si.
 * Enquanto o Firebase responde se há sessão salva, mostra só o indicador — é rápido
 * e evita a tela de login piscar para quem já está logado.
 */
@Composable
fun Raiz(
    sessao: SessaoViewModel,
    appViewModel: AppViewModel,
    bibliaViewModel: BibliaViewModel,
) {
    val estado by sessao.estado.collectAsStateWithLifecycle()

    when (val atual = estado) {
        EstadoSessao.Carregando -> Carregando()

        EstadoSessao.Deslogado -> {
            val formulario by sessao.formulario.collectAsStateWithLifecycle()
            LoginTela(
                formulario = formulario,
                aoEscreverEmail = sessao::escreverEmail,
                aoEscreverSenha = sessao::escreverSenha,
                aoAlternarCadastro = sessao::alternarCadastro,
                aoEnviar = sessao::enviar,
            )
        }

        is EstadoSessao.SemNome -> {
            val salvando by sessao.salvandoNome.collectAsStateWithLifecycle()
            NomeTela(
                email = atual.email,
                ocupado = salvando,
                aoConfirmar = { nome -> sessao.definirNome(atual.uid, atual.email, nome) },
                aoSair = sessao::sair,
            )
        }

        is EstadoSessao.BoasVindas -> BoasVindasTela(
            nomeJogador = atual.perfil.nomePersonagem,
            numeroJogador = atual.perfil.numeroJogador,
            aoTerminar = sessao::concluirAbertura,
        )

        is EstadoSessao.SemClasse -> ClasseTela(
            perfil = atual.perfil,
            aoConfirmar = { classe -> sessao.definirClasse(atual.perfil.uid, classe) },
            aoSair = sessao::sair,
        )

        is EstadoSessao.Pronto -> {
            // Só aqui o banco local descobre de quem ele é. Antes disso os lembretes criados
            // não aparecem e os dias não fecham — nenhum dos dois sabe de quem é o dia.
            LaunchedEffect(atual.perfil.uid, atual.perfil.admin) {
                appViewModel.entrarComo(atual.perfil.uid, atual.perfil.admin)
            }
            AppNavegacao(appViewModel, bibliaViewModel)
        }
    }
}

@Composable
private fun Carregando() {
    Box(Modifier.fillMaxSize().background(Fundo), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Neon, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
    }
}
