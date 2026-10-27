package dev.pedro.pessoal.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.pedro.pessoal.ui.theme.BrasaApagada
import dev.pedro.pessoal.ui.theme.Ganho

/**
 * O losango da marca — um quadrado girado 45°. É o mesmo símbolo que termina a barra do
 * "f" no ícone e serve de pingo no "j" do letreiro.
 *
 * Acesa, é o verde cheio; apagada, só o contorno. Sem animação: a brasa marca estado,
 * não chama atenção.
 *
 * O [Path] vive num `remember` e é refeito dentro do desenho em vez de alocado a cada
 * quadro. Reconstruir quatro pontos custa muito menos do que criar um objeto novo, e a
 * fase de desenho roda numa thread só, então reaproveitar o mesmo caminho é seguro.
 */
@Composable
fun Brasa(
    tamanho: Dp,
    modifier: Modifier = Modifier,
    acesa: Boolean = true,
) {
    val caminho = remember { Path() }
    Canvas(modifier.size(tamanho)) {
        val meioX = size.width / 2f
        val meioY = size.height / 2f
        caminho.reset()
        caminho.moveTo(meioX, 0f)
        caminho.lineTo(size.width, meioY)
        caminho.lineTo(meioX, size.height)
        caminho.lineTo(0f, meioY)
        caminho.close()

        if (acesa) {
            drawPath(caminho, Ganho)
        } else {
            drawPath(caminho, BrasaApagada, style = Stroke(width = CONTORNO.toPx()))
        }
    }
}

private val CONTORNO = 1.5.dp
