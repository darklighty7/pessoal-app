package dev.pedro.pessoal.ui.status

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.State
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pedro.pessoal.domain.model.Rank
import dev.pedro.pessoal.ui.theme.ChakraPetch
import dev.pedro.pessoal.ui.theme.Neon

/** Material de cada rank: cor clara (topo), escura (base), borda e cor da letra. */
private data class Material(
    val nome: String,
    val claro: Color,
    val escuro: Color,
    val borda: Color,
    val letra: Color,
)

private val RoxoLendario = Color(0xFFB06CFF)

private fun materialDo(rank: Rank): Material = when (rank) {
    Rank.E -> Material("Ferro", Color(0xFF9AA3B2), Color(0xFF4B5563), Color(0xFFC8CFD9), Color.White)
    Rank.D -> Material("Bronze", Color(0xFFE0A160), Color(0xFF7A4520), Color(0xFFF2C08A), Color.White)
    Rank.C -> Material("Prata", Color(0xFFF1F4F8), Color(0xFF8C95A3), Color.White, Color(0xFF1B2336))
    Rank.B -> Material("Ouro", Color(0xFFFFE27A), Color(0xFFA8740A), Color(0xFFFFF1B8), Color(0xFF3A2A05))
    Rank.A -> Material("Cristal", Color(0xFFB8F4FF), Color(0xFF1AA7C9), Color(0xFFE8FCFF), Color(0xFF063646))
    Rank.S -> Material("Lendário", Color(0xFF7FC4FF), Color(0xFF6D3CFF), Color(0xFFD9C8FF), Color.White)
}

fun nomeMaterial(rank: Rank): String = materialDo(rank).nome

/**
 * Selo no estilo "materiais de jogo ranqueado": um escudo que muda de material a cada rank,
 * com um reflexo de luz passando o tempo todo. Os enfeites vão se somando:
 * E liso · D rebites · C asas pequenas · B asas + estrela · A asas completas + brilhos ·
 * S coroa, aura pulsante e cor mudando entre azul e roxo.
 *
 * Todo o desenho usa uma grade de 120x120 unidades, escalada para o tamanho pedido.
 */
@Composable
fun SeloRank(rank: Rank, modifier: Modifier = Modifier, tamanho: Dp = 88.dp) {
    val m = materialDo(rank)
    val etapa = rank.ordinal // E=0 ... S=5
    val lendario = rank == Rank.S
    val comBrilhos = etapa >= 4

    val anim = rememberInfiniteTransition(label = "selo")

    // Guardados como State e lidos só lá dentro do desenho. Ler aqui com `by` faria este
    // composable recompor a cada quadro, e cada recomposição jogaria fora o cache dos
    // caminhos logo abaixo — que é justamente o que se quer evitar.
    val reflexo = anim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(if (lendario) 2400 else 3600, easing = LinearEasing)),
        label = "reflexo",
    )

    // Só o S muda de cor e tem aura, e só A e S piscam. Nos outros ranks essas animações
    // nem são criadas: antes rodavam todas, sempre, e o resultado era idêntico.
    val corTopo = if (lendario) {
        anim.animateColor(
            initialValue = m.claro,
            targetValue = m.claro,
            animationSpec = infiniteRepeatable(
                keyframes {
                    durationMillis = 4000
                    m.claro at 0
                    RoxoLendario at 1333
                    Neon at 2666
                    m.claro at 4000
                },
            ),
            label = "corTopo",
        )
    } else {
        null
    }

    val aura = if (lendario) {
        anim.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
            label = "aura",
        )
    } else {
        null
    }

    val brilhos: List<State<Float>> = if (comBrilhos) {
        listOf(
            anim.animateFloat(0f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "b1"),
            anim.animateFloat(
                0f, 1f, infiniteRepeatable(tween(1100), RepeatMode.Reverse, StartOffset(600)), label = "b2",
            ),
            anim.animateFloat(
                0f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse, StartOffset(1100)), label = "b3",
            ),
        )
    } else {
        emptyList()
    }

    Box(
        modifier
            .size(tamanho)
            .clearAndSetSemantics { contentDescription = "Rank ${rank.name}, ${m.nome}" },
        contentAlignment = Alignment.Center,
    ) {
        Spacer(
            Modifier.fillMaxSize().drawWithCache {
                // Este bloco roda uma vez por tamanho, não por quadro. Tudo o que depende
                // só de `u` mora aqui: antes, o escudo, a coroa, as penas e as faíscas
                // eram alocados sessenta vezes por segundo para sair sempre iguais.
                val u = size.width / 120f
                fun p(x: Float, y: Float) = Offset(x * u, y * u)

                val escudo = caminhoEscudo(u)
                val interno = caminhoEscudoInterno(u)
                val centro = p(60f, 62f)
                val rebiteEsquerdo = p(31f, 62f)
                val rebiteDireito = p(89f, 62f)

                val enfeite: Path? = when {
                    lendario -> poligono(
                        u, 44f to 26f, 48f to 12f, 54f to 22f, 60f to 8f, 66f to 22f, 72f to 12f, 76f to 26f,
                    )
                    etapa >= 3 -> poligono(
                        u, 60f to 7f, 63f to 15f, 71f to 15f, 64.5f to 20f, 67f to 28f,
                        60f to 23f, 53f to 28f, 55.5f to 20f, 49f to 15f, 57f to 15f,
                    )
                    else -> null
                }

                // Cada pena com a espessura do seu traço, já pronta.
                val penas: List<Pair<Path, Float>> = if (etapa >= 2) {
                    val quantas = when (etapa) { 2 -> 1; 3 -> 2; else -> 3 }
                    (0 until quantas).map { k ->
                        val y = 44f + k * 10
                        val caminho = Path().apply {
                            moveTo(34 * u, y * u)
                            quadraticTo((22f - k * 2) * u, (y - 4) * u, (12f + k * 4) * u, (y - 12 + k * 3) * u)
                        }
                        caminho to (5f - k) * u
                    }
                } else {
                    emptyList()
                }

                val faiscas = if (comBrilhos) {
                    listOf(faisca(u, 24f, 30f), faisca(u, 96f, 40f), faisca(u, 88f, 92f))
                } else {
                    emptyList()
                }

                // Fora do S a cor do topo não muda, então o pincel também não precisa mudar.
                val materialFixo = Brush.verticalGradient(
                    listOf(m.claro, m.escuro),
                    startY = 26 * u,
                    endY = 97 * u,
                )

                onDrawBehind {
                    val material = if (corTopo != null) {
                        Brush.verticalGradient(listOf(corTopo.value, m.escuro), startY = 26 * u, endY = 97 * u)
                    } else {
                        materialFixo
                    }

                    if (aura != null) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color(0xFF6D3CFF).copy(alpha = aura.value), Color.Transparent),
                                center = centro,
                                radius = 50 * u,
                            ),
                            radius = 50 * u,
                            center = centro,
                        )
                    }

                    // Asas: 1 par no C, 2 no B, 3 no A e S
                    if (penas.isNotEmpty()) {
                        val desenharAsa: DrawScope.() -> Unit = {
                            penas.forEach { (caminho, espessura) ->
                                drawPath(caminho, material, style = Stroke(espessura, cap = StrokeCap.Round))
                            }
                        }
                        desenharAsa()
                        scale(scaleX = -1f, scaleY = 1f, pivot = center) { desenharAsa() }
                    }

                    // Rebites nas laterais (D em diante)
                    if (etapa >= 1) {
                        drawCircle(material, radius = 3 * u, center = rebiteEsquerdo)
                        drawCircle(material, radius = 3 * u, center = rebiteDireito)
                    }

                    // Coroa no S, estrela no B e no A
                    if (enfeite != null) {
                        drawPath(enfeite, material)
                        if (lendario) drawPath(enfeite, m.borda, style = Stroke(1 * u))
                    }

                    // Escudo
                    drawPath(escudo, material)
                    drawPath(escudo, m.borda, style = Stroke(2 * u))
                    drawPath(interno, m.borda.copy(alpha = 0.55f), style = Stroke(1 * u))

                    // Reflexo de luz atravessando o escudo. É o único pincel que precisa
                    // nascer a cada quadro: ele depende de onde a luz está.
                    clipPath(escudo) {
                        val x = (-20f + reflexo.value * 130f) * u
                        rotate(20f, pivot = Offset(x + 7 * u, size.height / 2)) {
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    listOf(Color.Transparent, Color.White.copy(alpha = 0.7f), Color.Transparent),
                                    startX = x,
                                    endX = x + 14 * u,
                                ),
                                topLeft = Offset(x, -20 * u),
                                size = Size(14 * u, size.height + 40 * u),
                            )
                        }
                    }

                    // Brilhos piscando (A e S)
                    faiscas.forEachIndexed { i, caminho ->
                        drawPath(caminho, Color.White.copy(alpha = brilhos[i].value))
                    }
                }
            },
        )
        Text(
            rank.name,
            color = m.letra,
            fontFamily = ChakraPetch,
            fontWeight = FontWeight.Bold,
            fontSize = (tamanho.value * 0.23f).sp,
            modifier = Modifier.offset(y = tamanho * 0.02f),
        )
    }
}

private fun caminhoEscudo(u: Float) = Path().apply {
    moveTo(60 * u, 26 * u)
    lineTo(86 * u, 36 * u)
    lineTo(86 * u, 60 * u)
    cubicTo(86 * u, 79 * u, 75 * u, 90 * u, 60 * u, 97 * u)
    cubicTo(45 * u, 90 * u, 34 * u, 79 * u, 34 * u, 60 * u)
    lineTo(34 * u, 36 * u)
    close()
}

private fun caminhoEscudoInterno(u: Float) = Path().apply {
    moveTo(60 * u, 33 * u)
    lineTo(80 * u, 41 * u)
    lineTo(80 * u, 60 * u)
    cubicTo(80 * u, 75 * u, 72 * u, 84 * u, 60 * u, 90 * u)
    cubicTo(48 * u, 84 * u, 40 * u, 75 * u, 40 * u, 60 * u)
    lineTo(40 * u, 41 * u)
    close()
}

private fun poligono(u: Float, vararg pontos: Pair<Float, Float>) = Path().apply {
    pontos.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(x * u, y * u) else lineTo(x * u, y * u) }
    close()
}

/** Estrelinha de 4 pontas usada nos brilhos. */
private fun faisca(u: Float, x: Float, y: Float) = poligono(
    u, x to y - 5, x + 1.2f to y - 1.2f, x + 5 to y, x + 1.2f to y + 1.2f,
    x to y + 5, x - 1.2f to y + 1.2f, x - 5 to y, x - 1.2f to y - 1.2f,
)

@Preview(name = "Todos os selos", showBackground = true, backgroundColor = 0xFF0A0F1C, widthDp = 600)
@Composable
private fun SelosPreview() {
    Row(
        Modifier.padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Rank.entries.forEach { SeloRank(it) }
    }
}
