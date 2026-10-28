package dev.pedro.pessoal.ui.componentes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Ícones de linha minimalistas (24x24), no mesmo traço dos moldes. */
object Icones {
    private fun linha(nome: String, vararg caminhos: String): ImageVector =
        ImageVector.Builder(nome, 24.dp, 24.dp, 24f, 24f).apply {
            caminhos.forEach {
                addPath(
                    pathData = addPathNodes(it),
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = 1.75f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()

    val Escudo = linha("escudo", "M12 3l7 3v5c0 4.5-3 8-7 10-4-2-7-5.5-7-10V6l7-3z")
    val Sino = linha("sino", "M6 16v-5a6 6 0 0 1 12 0v5l1.5 2h-15L6 16z", "M10 20a2 2 0 0 0 4 0")
    val Halter = linha("halter", "M3 10v4M6 7v10M18 7v10M21 10v4M6 12h12")
    val Lampada = linha(
        "lampada", "M9 18h6M10 21h4",
        "M12 3a6 6 0 0 0-3.5 10.9c.6.5 1 1.2 1 2.1h5c0-.9.4-1.6 1-2.1A6 6 0 0 0 12 3z",
    )
    val Alvo = linha("alvo", "M4 12a8 8 0 1 0 16 0a8 8 0 1 0-16 0", "M8 12a4 4 0 1 0 8 0a4 4 0 1 0-8 0")
    val Cruz = linha("cruz", "M12 3v18M7 8h10")
    val Chama = linha("chama", "M12 3c1 3 4 5 4 9a4 4 0 0 1-8 0c0-2 1-3 2-4 0 2 1 3 2 3-1-3 0-6 0-8z")
    val Ajustes = linha(
        "ajustes", "M4 7h10M18 7h2M4 17h4M12 17h8",
        "M14 7a2 2 0 1 0 4 0a2 2 0 1 0-4 0", "M8 17a2 2 0 1 0 4 0a2 2 0 1 0-4 0",
    )
    val Confirmar = linha("confirmar", "M5 12.5l4.5 4.5L19 7.5")
    val Livro = linha(
        "livro", "M4 5a2 2 0 0 1 2-2h5v18H6a2 2 0 0 1-2-2V5z",
        "M20 5a2 2 0 0 0-2-2h-5v18h5a2 2 0 0 0 2-2V5z",
    )
    val Relogio = linha("relogio", "M4 12a8 8 0 1 0 16 0a8 8 0 1 0-16 0", "M12 7v5l3 2")
    val Tocar = linha("tocar", "M8 5l11 7-11 7V5z")
    val Pausar = linha("pausar", "M9 5v14M15 5v14")
    val Voltar = linha("voltar", "M15 5l-7 7 7 7")
    val Avancar = linha("avancar", "M9 5l7 7-7 7")
    val Fechar = linha("fechar", "M6 6l12 12M18 6L6 18")
    val Lupa = linha("lupa", "M4 11a7 7 0 1 0 14 0a7 7 0 1 0-14 0", "M16 16l4 4")
    val Marcador = linha("marcador", "M4 20h9", "M14.5 4.5l5 5L9 20l-5-5L14.5 4.5z")
    val Borracha = linha("borracha", "M8 20h12", "M5 14l8-8 6 6-6 6H9l-4-4z")
    val Mais = linha("mais", "M12 5v14M5 12h14")
    val Lapis = linha("lapis", "M4 20h4L19 9l-4-4L4 16v4z", "M14 6l4 4")
    val Fonte = linha("fonte", "M3 18l5-12 5 12M5 14h6", "M15 15.5a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0-5 0", "M20 13v5")
}
