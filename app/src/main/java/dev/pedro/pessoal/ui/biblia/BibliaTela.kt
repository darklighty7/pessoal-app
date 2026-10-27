package dev.pedro.pessoal.ui.biblia

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.input.ImeAction
import dev.pedro.pessoal.ui.theme.Fundo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import dev.pedro.pessoal.data.GrifoListado
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import dev.pedro.pessoal.ui.componentes.BotaoIcone
import dev.pedro.pessoal.ui.componentes.fadeBordas
import dev.pedro.pessoal.ui.theme.ChakraPetch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pedro.pessoal.data.BibliaRepositorio
import dev.pedro.pessoal.data.LivroEntity
import dev.pedro.pessoal.data.VersiculoEntity
import dev.pedro.pessoal.domain.engine.Regras
import dev.pedro.pessoal.ui.componentes.BarraNeon
import dev.pedro.pessoal.ui.componentes.Cartao
import dev.pedro.pessoal.ui.componentes.Icones
import dev.pedro.pessoal.ui.theme.Borda
import dev.pedro.pessoal.ui.theme.Ganho
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.PessoalTema
import dev.pedro.pessoal.ui.theme.Superficie
import dev.pedro.pessoal.ui.theme.Texto
import dev.pedro.pessoal.ui.theme.TextoSecundario
import dev.pedro.pessoal.ui.theme.Trilho

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BibliaTela(
    estado: EstadoBiblia,
    aoAbrir: (LivroEntity, Int, Int?) -> Unit,
    aoAlternarCronometro: () -> Unit,
    aoAvancar: () -> Unit,
    aoVoltar: () -> Unit,
    aoSair: () -> Unit,
    modifier: Modifier = Modifier,
    aoAlternarSelecao: (Int) -> Unit = {},
    aoGrifar: (Int) -> Unit = {},
    aoRemoverGrifos: () -> Unit = {},
    aoLimparSelecao: () -> Unit = {},
    aoAbrirGrifos: () -> Unit = {},
    aoDefinirTamanho: (Float) -> Unit = {},
    aoSalvarTamanho: () -> Unit = {},
    aoZoom: (Float) -> Unit = {},
    aoAlvoAtingido: () -> Unit = {},
) {
    // Ao deixar a tela, fecha a leitura e entrega os minutos.
    DisposableEffect(Unit) { onDispose { aoSair() } }

    var mostrandoIndice by remember { mutableStateOf(false) }
    var mostrandoGrifos by remember { mutableStateOf(false) }
    var mostrandoTamanho by remember { mutableStateOf(false) }
    val estadoFolha = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    when (val situacao = estado.download) {
        is BibliaRepositorio.Estado.Baixando ->
            Aviso("Baixando a Bíblia", "Isso acontece só na primeira vez. Precisa de internet agora.", modifier)

        is BibliaRepositorio.Estado.Importando ->
            Aviso(
                "Preparando a Bíblia",
                "${situacao.livrosFeitos} de ${situacao.livrosTotal} livros",
                modifier,
                progresso = situacao.livrosFeitos.toFloat() / situacao.livrosTotal,
            )

        is BibliaRepositorio.Estado.Falhou ->
            Aviso("Não deu para baixar", "${situacao.motivo}. Confira a internet e abra a tela de novo.", modifier)

        is BibliaRepositorio.Estado.Pronta -> Leitor(
            estado = estado,
            aoAlternarCronometro = aoAlternarCronometro,
            aoAvancar = aoAvancar,
            aoVoltar = aoVoltar,
            aoAbrirIndice = { mostrandoIndice = true },
            aoAbrirGrifos = {
                aoAbrirGrifos()
                mostrandoGrifos = true
            },
            aoAlternarSelecao = aoAlternarSelecao,
            aoGrifar = aoGrifar,
            aoRemoverGrifos = aoRemoverGrifos,
            aoLimparSelecao = aoLimparSelecao,
            aoAbrirTamanho = { mostrandoTamanho = true },
            aoZoom = aoZoom,
            aoSalvarTamanho = aoSalvarTamanho,
            aoAlvoAtingido = aoAlvoAtingido,
            modifier = modifier,
        )
    }

    if (mostrandoTamanho) {
        ModalBottomSheet(
            onDismissRequest = { mostrandoTamanho = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Superficie,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            TamanhoTexto(
                tamanho = estado.tamanhoFonte,
                aoDefinir = aoDefinirTamanho,
                aoSalvar = aoSalvarTamanho,
            )
        }
    }

    if (mostrandoGrifos) {
        ModalBottomSheet(
            onDismissRequest = { mostrandoGrifos = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Superficie,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            ListaGrifos(
                grifos = estado.todosGrifos,
                aoEscolher = { grifo ->
                    estado.livros.firstOrNull { it.numero == grifo.livro }?.let { aoAbrir(it, grifo.capitulo, null) }
                    mostrandoGrifos = false
                },
            )
        }
    }

    if (mostrandoIndice) {
        ModalBottomSheet(
            onDismissRequest = { mostrandoIndice = false },
            sheetState = estadoFolha,
            containerColor = Superficie,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            Indice(
                livros = estado.livros,
                aoEscolher = { livro, capitulo, versiculo ->
                    aoAbrir(livro, capitulo, versiculo)
                    mostrandoIndice = false
                },
            )
        }
    }
}

@Composable
private fun Leitor(
    estado: EstadoBiblia,
    aoAlternarCronometro: () -> Unit,
    aoAvancar: () -> Unit,
    aoVoltar: () -> Unit,
    aoAbrirIndice: () -> Unit,
    aoAbrirGrifos: () -> Unit,
    aoAlternarSelecao: (Int) -> Unit,
    aoGrifar: (Int) -> Unit,
    aoRemoverGrifos: () -> Unit,
    aoLimparSelecao: () -> Unit,
    aoAbrirTamanho: () -> Unit,
    aoZoom: (Float) -> Unit,
    aoSalvarTamanho: () -> Unit,
    aoAlvoAtingido: () -> Unit,
    modifier: Modifier,
) {
    val minutos = estado.segundos / 60
    val meta = Regras.META_LEITURA_MIN

    // O gesto de pinça lê estas duas pelo State, para não reiniciar quando a tela recompõe no meio dele.
    val zoomAtual = rememberUpdatedState(aoZoom)
    val terminarAtual = rememberUpdatedState(aoSalvarTamanho)

    // Esta tela recompõe a cada segundo (cronômetro): um modificador novo por recomposição
    // invalidaria o desenho da lista inteira a cada tique.
    val esmaecer = remember { Modifier.fadeBordas(topo = 0.05f, base = 0.08f) }

    // A busca pode pedir um versículo ("Jo 3:16"): a lista rola até ele quando o capítulo abre.
    val lista = rememberLazyListState()
    // A chave é só o alvo: os versículos já vêm na mesma atualização de estado, e comparar
    // a lista inteira a cada tique do cronômetro seria trabalho à toa.
    LaunchedEffect(estado.versiculoAlvo) {
        val alvo = estado.versiculoAlvo ?: return@LaunchedEffect
        val posicao = estado.versiculos.indexOfFirst { it.numero == alvo }
        // Salto direto, não animado: em Salmo 119 a animação percorreria 176 versículos.
        if (posicao >= 0) lista.scrollToItem(posicao)
        aoAlvoAtingido()
    }

    val tamanho = estado.tamanhoFonte
    val densidade = LocalDensity.current
    val estiloBase = MaterialTheme.typography.bodyLarge
    val digitos = remember(estado.versiculos) { (estado.versiculos.maxOfOrNull { it.numero } ?: 1).toString().length }
    val medidas = remember(tamanho, digitos, estiloBase, densidade) {
        val numero = TamanhoFonte.numero(tamanho)
        MedidasVerso(
            estilo = estiloBase.copy(fontSize = tamanho.sp, lineHeight = TamanhoFonte.entrelinha(tamanho).sp),
            numero = numero.sp,
            // 0,77 em por dígito dá os 20dp de sempre (13sp, 2 dígitos); com 3 dígitos (Salmo 119) a coluna cresce.
            larguraNumero = with(densidade) { (numero.sp * 0.77f * digitos.coerceAtLeast(2).toFloat()).toDp() },
            topoNumero = (6f * tamanho / TamanhoFonte.PADRAO).dp,
        )
    }

    Column(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 24.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f).clickable(onClick = aoAbrirIndice), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${estado.livroAberto?.nome ?: ""} ${estado.capitulo}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Texto,
                    )
                    Text(
                        "Toque para escolher outro capítulo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotaoIcone(Icones.Marcador, "Meus grifos", aoTocar = aoAbrirGrifos, cor = Neon)
                    BotaoIcone(
                        icone = if (estado.cronometroLigado) Icones.Pausar else Icones.Tocar,
                        descricao = if (estado.cronometroLigado) "Pausar leitura" else "Começar leitura",
                        aoTocar = aoAlternarCronometro,
                        cor = if (estado.cronometroLigado) Ganho else Neon,
                    )
                }
            }

            Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        if (estado.cronometroLigado) "LENDO" else "PARADO",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                        color = if (estado.cronometroLigado) Ganho else TextoSecundario,
                    )
                    BarraNeon((minutos.toFloat() / meta).coerceAtMost(1f), Modifier.weight(1f), altura = 6.dp)
                    Text(
                        "%d:%02d".format(minutos, estado.segundos % 60),
                        fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        color = Texto,
                    )
                }
                Text(
                    if (minutos >= meta) {
                        "Meta do dia alcançada. Cada minuto a mais ainda rende 1 XP."
                    } else {
                        "Meta: $meta minutos de leitura"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (minutos >= meta) Ganho else TextoSecundario,
                )
            }
        }

        LazyColumn(
            Modifier
                .weight(1f)
                .padding(top = 12.dp)
                .then(esmaecer)
                .pinchParaZoom(zoomAtual, terminarAtual),
            state = lista,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(estado.versiculos, key = { it.numero }) { verso ->
                Versiculo(
                    verso = verso,
                    cor = estado.grifos[verso.numero]?.let { CorGrifo.deIndice(it) },
                    selecionado = verso.numero in estado.selecionados,
                    medidas = medidas,
                    aoTocar = { aoAlternarSelecao(verso.numero) },
                )
            }
        }

        if (estado.selecionados.isNotEmpty()) {
            BarraGrifo(
                quantidade = estado.selecionados.size,
                aoGrifar = aoGrifar,
                aoRemover = aoRemoverGrifos,
                aoFechar = aoLimparSelecao,
            )
        }

        Column(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(Borda))
            Row(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = aoVoltar, modifier = Modifier.size(48.dp)) {
                    Icon(Icones.Voltar, contentDescription = "Capítulo anterior", tint = Neon, modifier = Modifier.size(26.dp))
                }
                // O meio ocupa o que sobra (weight): as setas de 48dp nunca são espremidas, e o nome da versão
                // encolhe com reticências se faltar espaço (telas estreitas ou fonte grande do sistema).
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        BibliaRepositorio.NOME_VERSAO,
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp),
                        color = TextoSecundario,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    BotaoIcone(Icones.Fonte, "Tamanho do texto", aoTocar = aoAbrirTamanho, cor = Neon)
                }
                IconButton(onClick = aoAvancar, modifier = Modifier.size(48.dp)) {
                    Icon(Icones.Avancar, contentDescription = "Próximo capítulo", tint = Neon, modifier = Modifier.size(26.dp))
                }
            }
        }
    }
}

/** Medidas do texto dos versículos para o tamanho escolhido; calculadas uma vez por tamanho. */
@Immutable
private data class MedidasVerso(
    val estilo: TextStyle,
    val numero: TextUnit,
    val larguraNumero: Dp,
    val topoNumero: Dp,
)

@Composable
private fun Versiculo(
    verso: VersiculoEntity,
    cor: CorGrifo?,
    selecionado: Boolean,
    medidas: MedidasVerso,
    aoTocar: () -> Unit,
) {
    val forma = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(cor?.fundo ?: Color.Transparent)
            .border(1.5.dp, if (selecionado) Neon else Color.Transparent, forma)
            .clickable(onClickLabel = "Selecionar versículo ${verso.numero}", onClick = aoTocar)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "${verso.numero}",
            fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = medidas.numero,
            color = Neon,
            softWrap = false,
            modifier = Modifier.width(medidas.larguraNumero).padding(top = medidas.topoNumero),
        )
        Text(
            verso.texto,
            style = medidas.estilo,
            color = Texto,
        )
    }
}

/**
 * Zoom por pinça (dois dedos) sobre a leitura. Só reage quando há dois dedos na tela: com um
 * dedo o toque passa direto, sem ser consumido, e a lista rola e os versículos selecionam como antes.
 * [zoom] recebe o fator de cada passo (maior que 1 aproxima); [terminar] roda ao soltar os dedos.
 * Os dois chegam como State (rememberUpdatedState) para o gesto não reiniciar quando a tela recompõe.
 */
private fun Modifier.pinchParaZoom(zoom: State<(Float) -> Unit>, terminar: State<() -> Unit>): Modifier =
    pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var deuZoom = false
            try {
                do {
                    val evento = awaitPointerEvent(PointerEventPass.Initial)
                    if (evento.changes.count { it.pressed } >= 2) {
                        val fator = evento.calculateZoom()
                        if (fator != 1f) {
                            zoom.value(fator)
                            deuZoom = true
                        }
                        // Consome o movimento dos dois dedos para a lista não rolar junto com a pinça.
                        evento.changes.forEach { if (it.positionChanged()) it.consume() }
                    }
                } while (evento.changes.any { it.pressed })
            } finally {
                // Também roda se a tela sair no meio da pinça (o gesto é cancelado): grava o tamanho.
                if (deuZoom) terminar.value()
            }
        }
    }

/** Folha do tamanho do texto: A−, controle deslizante, A+, prévia e volta ao padrão. */
@Composable
private fun TamanhoTexto(tamanho: Float, aoDefinir: (Float) -> Unit, aoSalvar: () -> Unit) {
    val mudado = tamanho != TamanhoFonte.PADRAO

    // Rola: em paisagem, ou com fonte grande no sistema, a folha pode ser mais alta que a tela.
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "TAMANHO DO TEXTO",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
                color = TextoSecundario,
            )
            Text(
                "${TamanhoFonte.percentual(tamanho)}%",
                fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                color = Texto,
            )
        }

        Cartao(Modifier.fillMaxWidth()) {
            Text(
                "No princípio Deus criou os céus e a terra.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = tamanho.sp,
                    lineHeight = TamanhoFonte.entrelinha(tamanho).sp,
                ),
                color = Texto,
                modifier = Modifier.padding(16.dp),
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BotaoLetra(14.sp, "Diminuir o texto", habilitado = tamanho > TamanhoFonte.MINIMO) {
                aoDefinir(TamanhoFonte.diminuir(tamanho))
                aoSalvar()
            }
            Slider(
                value = tamanho,
                onValueChange = aoDefinir,
                valueRange = TamanhoFonte.MINIMO..TamanhoFonte.MAXIMO,
                onValueChangeFinished = aoSalvar,
                colors = SliderDefaults.colors(
                    thumbColor = Neon,
                    activeTrackColor = Neon,
                    inactiveTrackColor = TextoSecundario.copy(alpha = 0.3f),
                ),
                modifier = Modifier.weight(1f).semantics {
                    contentDescription = "Tamanho do texto"
                    stateDescription = "${TamanhoFonte.percentual(tamanho)}%"
                },
            )
            BotaoLetra(24.sp, "Aumentar o texto", habilitado = tamanho < TamanhoFonte.MAXIMO) {
                aoDefinir(TamanhoFonte.aumentar(tamanho))
                aoSalvar()
            }
        }

        Text(
            "Voltar ao padrão",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (mudado) Neon else TextoSecundario.copy(alpha = 0.5f),
            modifier = Modifier
                .heightIn(min = 44.dp)
                .clickable(enabled = mudado, onClickLabel = "Voltar ao tamanho padrão") {
                    aoDefinir(TamanhoFonte.PADRAO)
                    aoSalvar()
                }
                .padding(horizontal = 4.dp)
                .wrapContentHeight(Alignment.CenterVertically),
        )

        Text(
            "Na leitura, aproxime ou afaste dois dedos sobre o texto para dar zoom.",
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

/** Botão quadrado de 44dp com a letra A no tamanho de [tamanhoLetra]; apagado quando não pode mais mudar. */
@Composable
private fun BotaoLetra(tamanhoLetra: TextUnit, descricao: String, habilitado: Boolean, aoTocar: () -> Unit) {
    val forma = RoundedCornerShape(14.dp)
    Box(
        Modifier
            .size(44.dp)
            .clip(forma)
            .background(Trilho)
            .border(1.dp, Borda, forma)
            .clickable(enabled = habilitado, role = Role.Button, onClick = aoTocar),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "A",
            fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = tamanhoLetra,
            color = if (habilitado) Texto else TextoSecundario.copy(alpha = 0.4f),
            // O leitor de tela diz "Diminuir o texto" em vez de só "A".
            modifier = Modifier.semantics { contentDescription = descricao },
        )
    }
}

/** Barra que aparece com versículos selecionados: escolhe a cor, remove o grifo ou fecha. */
@Composable
private fun BarraGrifo(quantidade: Int, aoGrifar: (Int) -> Unit, aoRemover: () -> Unit, aoFechar: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(Superficie)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Borda))
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (quantidade == 1) "1 versículo" else "$quantidade versículos",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextoSecundario,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
                IconButton(onClick = aoRemover, modifier = Modifier.size(48.dp)) {
                    Icon(Icones.Borracha, contentDescription = "Remover grifo", tint = TextoSecundario, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = aoFechar, modifier = Modifier.size(48.dp)) {
                    Icon(Icones.Fechar, contentDescription = "Cancelar seleção", tint = TextoSecundario, modifier = Modifier.size(22.dp))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CorGrifo.entries.forEach { cor ->
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(onClickLabel = "Grifar de ${cor.nome}") { aoGrifar(cor.ordinal) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(Modifier.size(32.dp).background(cor.cor, CircleShape))
                    }
                }
            }
        }
    }
}

/** Todos os grifos, com filtro por cor; tocar leva ao capítulo do versículo. */
@Composable
private fun ListaGrifos(grifos: List<GrifoListado>, aoEscolher: (GrifoListado) -> Unit) {
    var filtro by remember { mutableStateOf<CorGrifo?>(null) }
    val visiveis = remember(grifos, filtro) { grifos.filter { filtro == null || it.cor == filtro?.ordinal } }

    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
        Text(
            "MEUS GRIFOS",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
            color = TextoSecundario,
            modifier = Modifier.padding(horizontal = 4.dp).padding(bottom = 8.dp),
        )
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Todos",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (filtro == null) FontWeight.SemiBold else FontWeight.Medium,
                color = if (filtro == null) Neon else TextoSecundario,
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .clickable(onClickLabel = "Mostrar todos os grifos") { filtro = null }
                    .padding(horizontal = 8.dp)
                    .wrapContentHeight(Alignment.CenterVertically),
            )
            CorGrifo.entries.forEach { cor ->
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClickLabel = "Filtrar por ${cor.nome}") { filtro = if (filtro == cor) null else cor },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .background(cor.cor, CircleShape)
                            .border(2.dp, if (filtro == cor) Texto else Color.Transparent, CircleShape),
                    )
                }
            }
        }
        if (visiveis.isEmpty()) {
            Text(
                if (grifos.isEmpty()) "Nenhum versículo grifado ainda. Toque em um versículo para marcar."
                else "Nenhum grifo dessa cor.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario,
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 4.dp),
            )
        } else {
            LazyColumn(Modifier.fillMaxWidth()) {
                items(visiveis, key = { "${it.livro}-${it.capitulo}-${it.numero}" }) { grifo ->
                    Column(Modifier.fillMaxWidth().clickable(onClickLabel = "Abrir capítulo") { aoEscolher(grifo) }) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CorGrifo.deIndice(grifo.cor)?.let { Box(Modifier.size(10.dp).background(it.cor, CircleShape)) }
                                Text(
                                    "${grifo.livroNome} ${grifo.capitulo}:${grifo.numero}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextoSecundario,
                                )
                            }
                            Text(
                                grifo.texto,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
                                color = Texto,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Borda))
                    }
                }
            }
        }
    }
}

@Composable
private fun Indice(livros: List<LivroEntity>, aoEscolher: (LivroEntity, Int, Int?) -> Unit) {
    var escolhido by remember { mutableStateOf<LivroEntity?>(null) }
    var busca by remember { mutableStateOf("") }
    val livro = escolhido

    // "1João 3:16" vira livro + capítulo + versículo; só o nome também serve.
    val referencia = remember(busca) { BuscaLivro.interpretar(busca) }
    // Normalizar os 66 nomes é feito uma vez; a cada tecla só roda a comparação.
    val indice = remember(livros) { BuscaLivro.indexar(livros) }
    val encontrados = remember(busca, indice) { BuscaLivro.filtrar(indice, busca) }

    Column(
        Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        if (livro == null) {
            CampoBusca(busca, aoMudar = { busca = it })
            Spacer(Modifier.height(14.dp))
        }

        Text(
            if (livro == null) "ESCOLHA O LIVRO" else "CAPÍTULO DE ${livro.nome.uppercase()}",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
            color = TextoSecundario,
            modifier = Modifier
                .then(if (livro != null) Modifier.clickable(onClickLabel = "Voltar para os livros") { escolhido = null } else Modifier)
                .padding(horizontal = 4.dp)
                .padding(bottom = 8.dp),
        )

        if (livro == null) {
            if (encontrados.isEmpty()) {
                Text(
                    "Nenhum livro com esse nome.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 16.dp),
                )
            }
            LazyColumn(Modifier.fillMaxWidth()) {
                items(encontrados, key = { it.numero }) { item ->
                    // Quando a busca já trouxe o capítulo, tocar no livro abre direto nele.
                    val capitulo = referencia.capitulo?.takeIf { it in 1..item.capitulos }
                    Column(
                        Modifier.fillMaxWidth().clickable {
                            if (capitulo != null) {
                                aoEscolher(item, capitulo, referencia.versiculo)
                            } else {
                                escolhido = item
                            }
                        },
                    ) {
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                if (capitulo == null) item.nome else "${item.nome} $capitulo",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = Texto,
                            )
                            Icon(Icones.Avancar, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(18.dp))
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Borda))
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items((1..livro.capitulos).toList()) { numero ->
                    Cartao(Modifier.clickable { aoEscolher(livro, numero, null) }, cantos = 14.dp, corBorda = Borda) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text("$numero", style = MaterialTheme.typography.bodyMedium, color = Texto)
                        }
                    }
                }
            }
        }
    }
}

/** Campo de busca do índice: aceita "1João", "jo 3" ou "Jo 3:16". */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CampoBusca(valor: String, aoMudar: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        singleLine = true,
        placeholder = { Text("Buscar livro, ex.: 1João 3", style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(Icones.Lupa, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(20.dp)) },
        trailingIcon = {
            if (valor.isNotEmpty()) {
                Box(Modifier.clickable(onClickLabel = "Limpar busca") { aoMudar("") }.padding(8.dp)) {
                    Icon(Icones.Fechar, contentDescription = "Limpar busca", tint = TextoSecundario, modifier = Modifier.size(18.dp))
                }
            }
        },
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Texto,
            unfocusedTextColor = Texto,
            focusedContainerColor = Fundo,
            unfocusedContainerColor = Fundo,
            focusedBorderColor = Neon,
            unfocusedBorderColor = Borda,
            focusedPlaceholderColor = TextoSecundario,
            unfocusedPlaceholderColor = TextoSecundario,
            cursorColor = Neon,
        ),
    )
}

@Composable
private fun Aviso(titulo: String, texto: String, modifier: Modifier, progresso: Float? = null) {
    Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icones.Livro, contentDescription = null, tint = Neon, modifier = Modifier.size(40.dp))
            Text(titulo, style = MaterialTheme.typography.headlineMedium, color = Neon, fontWeight = FontWeight.Bold)
            Text(texto, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario, textAlign = TextAlign.Center)
            if (progresso != null) BarraNeon(progresso, Modifier.padding(top = 4.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0E14, widthDp = 390, heightDp = 844)
@Composable
private fun BibliaPreview() {
    PessoalTema {
        BibliaTela(
            estado = EstadoBiblia(
                download = BibliaRepositorio.Estado.Pronta,
                livroAberto = LivroEntity(1, "gn", "Gênesis", 50),
                capitulo = 1,
                versiculos = listOf(
                    VersiculoEntity(1, 1, 1, "No princípio Deus criou os céus e a terra."),
                    VersiculoEntity(1, 1, 2, "Era a terra sem forma e vazia; trevas cobriam a face do abismo."),
                ),
                segundos = 185,
                cronometroLigado = true,
                grifos = mapOf(2 to CorGrifo.AMARELO.ordinal),
                selecionados = setOf(1),
            ),
            aoAbrir = { _, _, _ -> },
            aoAlternarCronometro = {},
            aoAvancar = {},
            aoVoltar = {},
            aoSair = {},
        )
    }
}
