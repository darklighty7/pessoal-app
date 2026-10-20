package dev.pedro.pessoal.ui.biblia

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BibliaTela(
    estado: EstadoBiblia,
    aoAbrir: (LivroEntity, Int) -> Unit,
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
) {
    // Ao deixar a tela, fecha a leitura e entrega os minutos.
    DisposableEffect(Unit) { onDispose { aoSair() } }

    var mostrandoIndice by remember { mutableStateOf(false) }
    var mostrandoGrifos by remember { mutableStateOf(false) }
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
            modifier = modifier,
        )
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
                    estado.livros.firstOrNull { it.numero == grifo.livro }?.let { aoAbrir(it, grifo.capitulo) }
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
                aoEscolher = { livro, capitulo ->
                    aoAbrir(livro, capitulo)
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
    modifier: Modifier,
) {
    val minutos = estado.segundos / 60
    val meta = Regras.META_LEITURA_MIN

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
            Modifier.weight(1f).padding(top = 12.dp).fadeBordas(topo = 0.05f, base = 0.08f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(estado.versiculos, key = { it.numero }) { verso ->
                Versiculo(
                    verso = verso,
                    cor = estado.grifos[verso.numero]?.let { CorGrifo.deIndice(it) },
                    selecionado = verso.numero in estado.selecionados,
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
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = aoVoltar, modifier = Modifier.size(48.dp)) {
                    Icon(Icones.Voltar, contentDescription = "Capítulo anterior", tint = Neon, modifier = Modifier.size(26.dp))
                }
                Text(
                    BibliaRepositorio.NOME_VERSAO,
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp),
                    color = TextoSecundario,
                )
                IconButton(onClick = aoAvancar, modifier = Modifier.size(48.dp)) {
                    Icon(Icones.Avancar, contentDescription = "Próximo capítulo", tint = Neon, modifier = Modifier.size(26.dp))
                }
            }
        }
    }
}

@Composable
private fun Versiculo(verso: VersiculoEntity, cor: CorGrifo?, selecionado: Boolean, aoTocar: () -> Unit) {
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
            fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
            color = Neon,
            modifier = Modifier.width(20.dp).padding(top = 6.dp),
        )
        Text(
            verso.texto,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 29.sp),
            color = Texto,
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
private fun Indice(livros: List<LivroEntity>, aoEscolher: (LivroEntity, Int) -> Unit) {
    var escolhido by remember { mutableStateOf<LivroEntity?>(null) }
    val livro = escolhido

    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
        Text(
            if (livro == null) "ESCOLHA O LIVRO" else "CAPÍTULO DE ${livro.nome.uppercase()}",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
            color = TextoSecundario,
            modifier = Modifier.padding(horizontal = 4.dp).padding(bottom = 8.dp),
        )
        if (livro == null) {
            LazyColumn(Modifier.fillMaxWidth()) {
                items(livros, key = { it.numero }) { item ->
                    Column(Modifier.fillMaxWidth().clickable { escolhido = item }) {
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                item.nome,
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
                    Cartao(Modifier.clickable { aoEscolher(livro, numero) }, cantos = 14.dp, corBorda = Borda) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text("$numero", style = MaterialTheme.typography.bodyMedium, color = Texto)
                        }
                    }
                }
            }
        }
    }
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
            aoAbrir = { _, _ -> },
            aoAlternarCronometro = {},
            aoAvancar = {},
            aoVoltar = {},
            aoSair = {},
        )
    }
}
