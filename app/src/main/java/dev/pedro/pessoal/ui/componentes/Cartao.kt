package dev.pedro.pessoal.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.pedro.pessoal.ui.theme.Borda
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.Superficie
import dev.pedro.pessoal.ui.theme.Trilho

@Composable
fun Cartao(
    modifier: Modifier = Modifier,
    corBorda: Color = Borda,
    cantos: Dp = 12.dp,
    fundo: Color = Superficie,
    conteudo: @Composable BoxScope.() -> Unit,
) {
    val forma = RoundedCornerShape(cantos)
    Box(
        modifier = modifier
            .clip(forma)
            .background(fundo)
            .border(1.dp, corBorda, forma),
        content = conteudo,
    )
}

/** Barra de progresso fina com o preenchimento em neon. */
@Composable
fun BarraNeon(progresso: Float, modifier: Modifier = Modifier, altura: Dp = 8.dp) {
    Box(
        modifier
            .fillMaxWidth()
            .height(altura)
            .clip(RoundedCornerShape(50))
            .background(Trilho),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progresso.coerceIn(0f, 1f))
                .height(altura)
                .clip(RoundedCornerShape(50))
                .background(Neon),
        )
    }
}
