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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
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
    cantos: Dp = 20.dp,
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

/** Barra de progresso fina e arredondada; azul por padrão, verde quando a meta fecha. */
@Composable
fun BarraNeon(
    progresso: Float,
    modifier: Modifier = Modifier,
    altura: Dp = 8.dp,
    cor: Color = Neon,
) {
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
                .background(cor),
        )
    }
}

/** Esmaece as pontas de uma lista para o fundo, sem custo de animação. */
fun Modifier.fadeBordas(topo: Float = 0f, base: Float = 0.12f): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                0f to if (topo > 0f) Color.Transparent else Color.Black,
                topo to Color.Black,
                (1f - base) to Color.Black,
                1f to Color.Transparent,
            ),
            blendMode = BlendMode.DstIn,
        )
    }

fun Modifier.fadeInferior(fracao: Float = 0.12f): Modifier = fadeBordas(0f, fracao)
