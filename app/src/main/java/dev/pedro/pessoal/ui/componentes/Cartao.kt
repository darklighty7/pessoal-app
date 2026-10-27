package dev.pedro.pessoal.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithCache
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
    brasa: Boolean = false,
) {
    val fracao = progresso.coerceIn(0f, 1f)
    val tamanhoBrasa = altura + 6.dp

    Box(
        modifier.fillMaxWidth().height(if (brasa) tamanhoBrasa else altura),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(altura)
                .clip(RoundedCornerShape(50))
                .background(Trilho),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fracao)
                    .height(altura)
                    .clip(RoundedCornerShape(50))
                    .background(cor),
            )
        }

        // A brasa marca a ponta do preenchimento, inteira dentro da barra — assim nunca
        // é cortada por um recorte de quem desenha a barra. O widthIn garante que ela
        // não encolha junto com o preenchimento quando o progresso é quase zero.
        if (brasa) {
            Box(
                Modifier.fillMaxWidth(fracao).widthIn(min = tamanhoBrasa).fillMaxHeight(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Brasa(tamanho = tamanhoBrasa)
            }
        }
    }
}

/**
 * Esmaece as pontas de uma lista para o fundo, sem custo de animação.
 * O gradiente é criado uma vez por tamanho. Quem chama dentro de uma tela que recompõe com
 * frequência deve guardar o resultado com remember { Modifier.fadeBordas(...) }: um modificador
 * novo a cada recomposição invalida o desenho (e a camada offscreen) à toa.
 */
fun Modifier.fadeBordas(topo: Float = 0f, base: Float = 0.12f): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithCache {
        val mascara = Brush.verticalGradient(
            0f to if (topo > 0f) Color.Transparent else Color.Black,
            topo to Color.Black,
            (1f - base) to Color.Black,
            1f to Color.Transparent,
        )
        onDrawWithContent {
            drawContent()
            drawRect(brush = mascara, blendMode = BlendMode.DstIn)
        }
    }

fun Modifier.fadeInferior(fracao: Float = 0.12f): Modifier = fadeBordas(0f, fracao)
