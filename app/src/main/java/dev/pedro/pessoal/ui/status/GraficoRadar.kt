package dev.pedro.pessoal.ui.status

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.pedro.pessoal.domain.model.Atributo
import dev.pedro.pessoal.domain.model.formatarAtributo
import dev.pedro.pessoal.ui.theme.ChakraPetch
import dev.pedro.pessoal.ui.theme.GradeRadar
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.TextoSecundario

/** Gráfico em losango: FOR em cima, INT à direita, DIS embaixo, ESP à esquerda. */
@Composable
fun GraficoRadar(atributos: Map<Atributo, Int>, modifier: Modifier = Modifier) {
    val ordem = listOf(Atributo.FORCA, Atributo.INTELIGENCIA, Atributo.DISCIPLINA, Atributo.ESPIRITO)
    // A escala acompanha o maior atributo, para o desenho não ficar minúsculo nem estourar
    val escala = maxOf(100, ((atributos.values.maxOrNull() ?: 0) * 1.25f).toInt()).toFloat()
    val descricao = ordem.joinToString { "${it.nome} ${formatarAtributo(atributos.getValue(it))}" }

    Box(modifier.size(240.dp, 210.dp).semantics { contentDescription = descricao }) {
        Canvas(Modifier.size(170.dp).align(Alignment.Center)) {
            val c = center
            val r = size.minDimension / 2
            fun ponto(i: Int, fracao: Float): Offset = when (i) {
                0 -> Offset(c.x, c.y - r * fracao)
                1 -> Offset(c.x + r * fracao, c.y)
                2 -> Offset(c.x, c.y + r * fracao)
                else -> Offset(c.x - r * fracao, c.y)
            }
            fun losango(fracoes: List<Float>) = Path().apply {
                fracoes.forEachIndexed { i, f -> ponto(i, f).let { if (i == 0) moveTo(it.x, it.y) else lineTo(it.x, it.y) } }
                close()
            }
            listOf(0.25f, 0.5f, 0.75f, 1f).forEach {
                drawPath(losango(List(4) { _ -> it }), GradeRadar, style = Stroke(1.dp.toPx()))
            }
            drawLine(GradeRadar, ponto(0, 1f), ponto(2, 1f), 1.dp.toPx())
            drawLine(GradeRadar, ponto(3, 1f), ponto(1, 1f), 1.dp.toPx())

            val valores = losango(ordem.map { atributos.getValue(it) / escala })
            drawPath(valores, Neon.copy(alpha = 0.28f))
            drawPath(valores, Neon, style = Stroke(2.dp.toPx(), join = StrokeJoin.Round))
        }
        val rotulo = MaterialTheme.typography.labelSmall.copy(fontFamily = ChakraPetch, color = TextoSecundario)
        Text("FOR", style = rotulo, modifier = Modifier.align(Alignment.TopCenter))
        Text("INT", style = rotulo, modifier = Modifier.align(Alignment.CenterEnd))
        Text("DIS", style = rotulo, modifier = Modifier.align(Alignment.BottomCenter))
        Text("ESP", style = rotulo, modifier = Modifier.align(Alignment.CenterStart))
    }
}
