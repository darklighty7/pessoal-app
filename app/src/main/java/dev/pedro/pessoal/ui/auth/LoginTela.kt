package dev.pedro.pessoal.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pedro.pessoal.R
import dev.pedro.pessoal.ui.componentes.CampoTexto
import dev.pedro.pessoal.ui.theme.Alerta
import dev.pedro.pessoal.ui.theme.Fundo
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.SobreNeon
import dev.pedro.pessoal.ui.theme.Texto
import dev.pedro.pessoal.ui.theme.TextoSecundario
import androidx.compose.foundation.Image
import androidx.compose.foundation.background

/** Entrada do app: o mesmo formulário serve para entrar e para criar conta. */
@Composable
fun LoginTela(
    formulario: FormularioLogin,
    aoEscreverEmail: (String) -> Unit,
    aoEscreverSenha: (String) -> Unit,
    aoAlternarCadastro: () -> Unit,
    aoEnviar: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Fundo)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(72.dp))
        // O letreiro é um desenho, não um texto: a largura vem da proporção do vetor.
        Image(
            painter = painterResource(R.drawable.forja_wordmark),
            contentDescription = "Forja",
            modifier = Modifier.height(44.dp).aspectRatio(214f / 100f),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "FORJE-SE TODO DIA",
            color = TextoSecundario,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 3.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(40.dp))

        CampoTexto(
            valor = formulario.email,
            aoMudar = aoEscreverEmail,
            rotulo = "E-mail",
            email = true,
            habilitado = !formulario.ocupado,
        )
        Spacer(Modifier.height(14.dp))
        CampoTexto(
            valor = formulario.senha,
            aoMudar = aoEscreverSenha,
            rotulo = "Senha",
            senha = true,
            ultimo = true,
            habilitado = !formulario.ocupado,
        )

        if (formulario.erro != null) {
            Spacer(Modifier.height(14.dp))
            Text(
                formulario.erro,
                color = Alerta,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = aoEnviar,
            enabled = !formulario.ocupado,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = SobreNeon),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (formulario.ocupado) {
                CircularProgressIndicator(color = SobreNeon, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    if (formulario.criandoConta) "Criar conta" else "Entrar",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        TextButton(onClick = aoAlternarCadastro, enabled = !formulario.ocupado) {
            Text(
                if (formulario.criandoConta) "Já tenho conta. Entrar" else "Não tenho conta. Criar agora",
                color = Texto,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(48.dp))
    }
}
