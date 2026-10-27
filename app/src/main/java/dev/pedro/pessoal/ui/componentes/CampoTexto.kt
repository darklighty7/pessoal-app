package dev.pedro.pessoal.ui.componentes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import dev.pedro.pessoal.ui.theme.Borda
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.Superficie
import dev.pedro.pessoal.ui.theme.Texto
import dev.pedro.pessoal.ui.theme.TextoSecundario

/** Campo de texto na paleta escura do app: fundo de superfície e foco em neon. */
@Composable
fun CampoTexto(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier,
    senha: Boolean = false,
    email: Boolean = false,
    ultimo: Boolean = false,
    habilitado: Boolean = true,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        label = { Text(rotulo, style = MaterialTheme.typography.bodyMedium) },
        singleLine = true,
        enabled = habilitado,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth(),
        visualTransformation = if (senha) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = when {
                email -> KeyboardType.Email
                senha -> KeyboardType.Password
                else -> KeyboardType.Text
            },
            imeAction = if (ultimo) ImeAction.Done else ImeAction.Next,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Texto,
            unfocusedTextColor = Texto,
            focusedContainerColor = Superficie,
            unfocusedContainerColor = Superficie,
            disabledContainerColor = Superficie,
            focusedBorderColor = Neon,
            unfocusedBorderColor = Borda,
            focusedLabelColor = Neon,
            unfocusedLabelColor = TextoSecundario,
            cursorColor = Neon,
        ),
    )
}
