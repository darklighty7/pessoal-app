package dev.pedro.pessoal.ui.boasvindas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pedro.pessoal.ui.theme.ChakraPetch
import dev.pedro.pessoal.ui.theme.Fundo
import dev.pedro.pessoal.ui.theme.Ganho
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.Texto
import dev.pedro.pessoal.ui.theme.TextoSecundario
import kotlinx.coroutines.delay

/**
 * A abertura que roda uma única vez, logo depois de criar o personagem.
 *
 * Começa como terminal — o sistema ligando e reconhecendo quem chegou — e termina em
 * caixa de diálogo de RPG, onde a missão é anunciada. Um toque pula a digitação; a
 * última página só sai pelo botão, porque é o aceite.
 */
@Composable
fun BoasVindasTela(
    nomeJogador: String,
    numeroJogador: Int,
    aoTerminar: () -> Unit,
) {
    val roteiro = remember(nomeJogador, numeroJogador) { roteiroTerminal(nomeJogador, numeroJogador) }

    var fase by remember { mutableStateOf(Fase.TERMINAL) }
    var linhasProntas by remember { mutableIntStateOf(0) }
    var parcial by remember { mutableStateOf("") }
    var pagina by remember { mutableIntStateOf(0) }
    var paginaParcial by remember { mutableStateOf("") }
    var paginaCompleta by remember { mutableStateOf(false) }
    var pular by remember { mutableStateOf(false) }

    // Fase 1: o terminal escreve o roteiro linha por linha. Com [pular] ligado, o laço
    // atravessa o resto sem esperar nada e as linhas aparecem de uma vez.
    LaunchedEffect(Unit) {
        roteiro.forEachIndexed { indice, linha ->
            if (!pular) {
                for (i in linha.texto.indices) {
                    if (pular) break
                    parcial = linha.texto.substring(0, i + 1)
                    delay(linha.estilo.msPorLetra)
                }
            }
            linhasProntas = indice + 1
            parcial = ""
            if (!pular) delay(linha.pausaDepois)
        }
        pular = false
        fase = Fase.DIALOGO
    }

    // Fase 2: uma página de diálogo por vez.
    LaunchedEffect(fase, pagina) {
        if (fase != Fase.DIALOGO) return@LaunchedEffect
        paginaCompleta = false
        paginaParcial = ""
        val texto = PAGINAS[pagina].texto
        for (i in texto.indices) {
            if (pular) break
            paginaParcial = texto.substring(0, i + 1)
            delay(MS_POR_LETRA_DIALOGO)
        }
        paginaParcial = texto
        paginaCompleta = true
        pular = false
    }

    val opacidadeTerminal by animateFloatAsState(
        targetValue = if (fase == Fase.DIALOGO) 0.3f else 1f,
        animationSpec = tween(MS_TRANSICAO),
        label = "opacidade do terminal",
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(Fundo)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                when {
                    fase == Fase.TERMINAL -> pular = true
                    !paginaCompleta -> pular = true
                    pagina < PAGINAS.lastIndex -> pagina += 1
                    // Na última página o toque não faz nada: o aceite é pelo botão.
                    else -> Unit
                }
            },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .graphicsLayer { alpha = opacidadeTerminal },
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            roteiro.take(linhasProntas).forEach { linha ->
                Text(text = linha.texto, style = linha.estilo.textStyle(), color = linha.estilo.cor)
            }
            if (linhasProntas < roteiro.size) {
                val estilo = roteiro[linhasProntas].estilo
                LinhaDigitando(texto = { parcial }, estilo = estilo)
            }
        }

        AnimatedVisibility(
            visible = fase == Fase.DIALOGO,
            enter = slideInVertically(tween(MS_TRANSICAO)) { it / 2 } + fadeIn(tween(MS_TRANSICAO)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            CaixaDialogo(
                texto = { paginaParcial },
                cor = PAGINAS[pagina].cor,
                ultima = pagina == PAGINAS.lastIndex,
                completa = paginaCompleta,
                aoAceitar = aoTerminar,
            )
        }

        Box(Modifier.fillMaxSize().varredura())
    }
}

/**
 * Fica sozinha num composable para que só ela refaça o layout a cada letra; as linhas
 * já escritas acima não são medidas de novo.
 */
@Composable
private fun LinhaDigitando(texto: () -> String, estilo: EstiloLinha) {
    Row {
        Text(text = texto(), style = estilo.textStyle(), color = estilo.cor)
        Cursor(estilo.cor)
    }
}

@Composable
private fun Cursor(cor: Color) {
    var aceso by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(MS_PISCA)
            aceso = !aceso
        }
    }
    Box(
        Modifier
            .padding(start = 3.dp)
            .size(width = 9.dp, height = 17.dp)
            .background(if (aceso) cor else Color.Transparent),
    )
}

@Composable
private fun CaixaDialogo(
    texto: () -> String,
    cor: Color,
    ultima: Boolean,
    completa: Boolean,
    aoAceitar: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(16.dp)
            .molduraPixel(UNIDADE, fundo = CAIXA_FUNDO, borda = Neon)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Text(
            text = "O SISTEMA",
            style = TextStyle(
                fontFamily = ChakraPetch,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
            ),
            color = Neon,
        )
        Spacer(Modifier.height(10.dp))
        TextoDialogo(texto = texto, cor = cor)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (ultima && completa) {
                BotaoAceitar(aoAceitar)
            } else if (completa) {
                SetaPiscando()
            }
        }
    }
}

/**
 * A altura mínima reserva espaço para as páginas mais longas, senão a moldura cresceria
 * a cada letra e a caixa inteira ficaria pulando durante a digitação.
 */
@Composable
private fun TextoDialogo(texto: () -> String, cor: Color) {
    Text(
        text = texto(),
        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 15.sp, lineHeight = 24.sp),
        color = cor,
        modifier = Modifier.fillMaxWidth().heightIn(min = ALTURA_TEXTO),
    )
}

@Composable
private fun BotaoAceitar(aoTocar: () -> Unit) {
    Text(
        text = "ACEITAR A MISSÃO",
        style = TextStyle(
            fontFamily = ChakraPetch,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp,
        ),
        color = Fundo,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = aoTocar,
            )
            .molduraPixel(UNIDADE_BOTAO, fundo = Ganho, borda = Ganho)
            .padding(horizontal = 18.dp, vertical = 11.dp),
    )
}

/** O triângulo de "continuar", desenhado em blocos para combinar com a moldura. */
@Composable
private fun SetaPiscando() {
    var aceso by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(MS_PISCA)
            aceso = !aceso
        }
    }
    Box(
        Modifier
            .size(width = 18.dp, height = 12.dp)
            .drawBehind {
                if (!aceso) return@drawBehind
                val bloco = size.width / 6f
                // Três fileiras que vão estreitando: 6, 4 e 2 blocos.
                repeat(3) { fileira ->
                    val largura = (6 - fileira * 2) * bloco
                    drawRect(
                        color = Neon,
                        topLeft = Offset(fileira * bloco, fileira * bloco),
                        size = Size(largura, bloco),
                    )
                }
            },
    )
}

/**
 * Moldura de janela 8 bits: uma borda de um bloco com os cantos em degrau, como se a
 * tela não tivesse resolução para arredondar.
 */
private fun Modifier.molduraPixel(unidade: Dp, fundo: Color, borda: Color): Modifier = drawBehind {
    val u = unidade.toPx()
    val l = size.width
    val a = size.height
    if (l < 4 * u || a < 4 * u) return@drawBehind

    // Fundo com os quatro cantos recortados.
    drawRect(fundo, Offset(u, 0f), Size(l - 2 * u, a))
    drawRect(fundo, Offset(0f, u), Size(l, a - 2 * u))

    // Borda de um bloco, sem os cantos.
    drawRect(borda, Offset(u, 0f), Size(l - 2 * u, u))
    drawRect(borda, Offset(u, a - u), Size(l - 2 * u, u))
    drawRect(borda, Offset(0f, u), Size(u, a - 2 * u))
    drawRect(borda, Offset(l - u, u), Size(u, a - 2 * u))

    // Os degraus que fecham cada canto.
    drawRect(borda, Offset(u, u), Size(u, u))
    drawRect(borda, Offset(l - 2 * u, u), Size(u, u))
    drawRect(borda, Offset(u, a - 2 * u), Size(u, u))
    drawRect(borda, Offset(l - 2 * u, a - 2 * u), Size(u, u))
}

/**
 * As listras horizontais de monitor antigo. Fica num nó próprio, que não lê nenhum
 * estado animado, então o desenho é gravado uma vez e reaproveitado em cada quadro.
 */
private fun Modifier.varredura(): Modifier = drawBehind {
    val passo = 3.dp.toPx()
    val espessura = 1.dp.toPx()
    var y = 0f
    while (y < size.height) {
        drawRect(VARREDURA, Offset(0f, y), Size(size.width, espessura))
        y += passo
    }
}

private enum class Fase { TERMINAL, DIALOGO }

private enum class EstiloLinha(val cor: Color, val msPorLetra: Long) {
    SISTEMA(TextoSecundario, 12L),
    OK(Ganho, 12L),
    NUMERO(Neon, 45L),
    NOME(Texto, 45L),
    SAUDACAO(Ganho, 32L),
}

private fun EstiloLinha.textStyle(): TextStyle = when (this) {
    EstiloLinha.NUMERO, EstiloLinha.NOME -> TextStyle(
        fontFamily = ChakraPetch,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 1.sp,
    )
    EstiloLinha.SAUDACAO -> TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 2.sp,
    )
    else -> TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
}

private class LinhaTerminal(
    val texto: String,
    val estilo: EstiloLinha,
    val pausaDepois: Long,
)

private class PaginaDialogo(val texto: String, val cor: Color)

/**
 * O que o terminal escreve. Sem o número do jogador — quando o contador não respondeu —
 * a linha some e a abertura segue só com o nome.
 */
private fun roteiroTerminal(nome: String, numero: Int): List<LinhaTerminal> = buildList {
    add(LinhaTerminal("> sistema.iniciar()", EstiloLinha.SISTEMA, 220L))
    add(LinhaTerminal("  nucleo ......... ok", EstiloLinha.OK, 80L))
    add(LinhaTerminal("  escrituras ..... ok", EstiloLinha.OK, 80L))
    add(LinhaTerminal("  lembretes ...... ok", EstiloLinha.OK, 80L))
    add(LinhaTerminal("  treinos ........ ok", EstiloLinha.OK, 240L))
    add(LinhaTerminal("> jogador.identificar()", EstiloLinha.SISTEMA, 520L))
    add(LinhaTerminal(" ", EstiloLinha.SISTEMA, 0L))
    if (numero > 0) {
        add(LinhaTerminal("JOGADOR N° ${numero.formatado()}", EstiloLinha.NUMERO, 260L))
    }
    add(LinhaTerminal(nome.trim().uppercase(), EstiloLinha.NOME, 620L))
    add(LinhaTerminal(" ", EstiloLinha.SISTEMA, 0L))
    add(LinhaTerminal("BEM-VINDO, JOGADOR.", EstiloLinha.SAUDACAO, 900L))
}

/** Três casas enquanto couber: 007, 042, 128, e depois o número inteiro. */
private fun Int.formatado(): String = toString().padStart(3, '0')

private val PAGINAS = listOf(
    PaginaDialogo("Agora, você e o sistema têm um compromisso.", Texto),
    PaginaDialogo(
        "Você tem a missão de se aproximar mais de Jesus, o Cristo, e de se tornar a melhor versão de si mesmo.",
        Texto,
    ),
    PaginaDialogo(
        "O sistema está aqui para te auxiliar nessa missão, te alertando e fornecendo tarefas para cumprir ao decorrer dela.",
        Texto,
    ),
    PaginaDialogo("A missão só termina quando Ele voltar.", Ganho),
)

private val CAIXA_FUNDO = Color(0xFF0E1726)
private val VARREDURA = Color(0x08000000)
private val ALTURA_TEXTO = 96.dp
private val UNIDADE = 4.dp
private val UNIDADE_BOTAO = 3.dp
private const val MS_POR_LETRA_DIALOGO = 20L
private const val MS_TRANSICAO = 420
private const val MS_PISCA = 450L
