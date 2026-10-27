package dev.pedro.pessoal.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pedro.pessoal.ui.componentes.CampoTexto
import dev.pedro.pessoal.ui.componentes.MarcaForja
import dev.pedro.pessoal.ui.theme.Fundo
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.SobreNeon
import dev.pedro.pessoal.ui.theme.TextoSecundario

/**
 * Primeiro passo depois de entrar: só o nome.
 *
 * O nome vem antes da abertura porque é ele que o sistema anuncia lá — e a classe vem
 * depois, porque escolher um caminho antes de ouvir qual é a missão não quer dizer nada.
 *
 * A tela fica centrada, como a de login, para as duas parecerem a mesma sala.
 */
@Composable
fun NomeTela(
    email: String,
    ocupado: Boolean,
    aoConfirmar: (String) -> Unit,
    aoSair: () -> Unit,
) {
    var nome by remember { mutableStateOf("") }
    val valido = nome.trim().length >= 2

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
        MarcaForja(altura = 28.dp)

        Spacer(Modifier.height(36.dp))
        Text(
            "QUEM É VOCÊ?",
            color = Neon,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "O nome do seu personagem. É assim que o sistema vai te chamar.",
            color = TextoSecundario,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))
        CampoTexto(
            valor = nome,
            aoMudar = { nome = it },
            rotulo = "Nome do personagem",
            ultimo = true,
            habilitado = !ocupado,
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { aoConfirmar(nome.trim()) },
            enabled = valido && !ocupado,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = SobreNeon),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (ocupado) {
                CircularProgressIndicator(color = SobreNeon, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Text("Continuar", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(6.dp))
        TextButton(onClick = aoSair, enabled = !ocupado, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Sair da conta $email",
                color = TextoSecundario,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(48.dp))
    }
}
