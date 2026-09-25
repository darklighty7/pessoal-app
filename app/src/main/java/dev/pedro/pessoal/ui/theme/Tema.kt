package dev.pedro.pessoal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val Esquema = darkColorScheme(
    primary = Neon,
    onPrimary = Fundo,
    background = Fundo,
    onBackground = Texto,
    surface = Superficie,
    onSurface = Texto,
    surfaceVariant = SuperficieBaixa,
    onSurfaceVariant = TextoSecundario,
    outline = Borda,
)

/** O app é sempre escuro, independentemente do tema do sistema. */
@Composable
fun PessoalTema(conteudo: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, typography = Tipografia, content = conteudo)
}
