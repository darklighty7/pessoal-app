package dev.pedro.pessoal.ui.biblia

import androidx.compose.foundation.clickable
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
) {
    // Ao deixar a tela, fecha a leitura e entrega os minutos.
    DisposableEffect(Unit) { onDispose { aoSair() } }

    var mostrandoIndice by remember { mutableStateOf(false) }
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
            modifier = modifier,
        )
    }

    if (mostrandoIndice) {
        ModalBottomSheet(
            onDismissRequest = { mostrandoIndice = false },
            sheetState = estadoFolha,
            containerColor = Superficie,
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
    modifier: Modifier,
) {
    val minutos = estado.segundos / 60
    val meta = Regras.META_LEITURA_MIN

    Column(modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.clickable(onClick = aoAbrirIndice)) {
                Text(
                    "${estado.livroAberto?.nome ?: ""} ${estado.capitulo}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Neon,
                )
                Text(
                    "Toque para escolher outro capítulo",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                )
            }
            IconButton(onClick = aoAlternarCronometro) {
                Icon(
                    if (estado.cronometroLigado) Icones.Pausar else Icones.Tocar,
                    contentDescription = if (estado.cronometroLigado) "Pausar leitura" else "Começar leitura",
                    tint = if (estado.cronometroLigado) Ganho else Neon,
                    modifier = Modifier.size(26.dp),
                )
            }
        }

        Cartao(Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        if (estado.cronometroLigado) "LENDO" else "CRONÔMETRO PARADO",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                        color = if (estado.cronometroLigado) Ganho else TextoSecundario,
                    )
                    Text(
                        "%d:%02d".format(minutos, estado.segundos % 60),
                        style = MaterialTheme.typography.labelSmall,
                        color = Texto,
                    )
                }
                BarraNeon((minutos.toFloat() / meta).coerceAtMost(1f))
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
            Modifier.weight(1f).padding(top = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(estado.versiculos, key = { it.numero }) { verso -> Versiculo(verso) }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = aoVoltar) {
                Icon(Icones.Voltar, contentDescription = "Capítulo anterior", tint = TextoSecundario)
            }
            Text(
                BibliaRepositorio.NOME_VERSAO,
                style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
            IconButton(onClick = aoAvancar) {
                Icon(Icones.Avancar, contentDescription = "Próximo capítulo", tint = TextoSecundario)
            }
        }
    }
}

@Composable
private fun Versiculo(verso: VersiculoEntity) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "${verso.numero}",
            style = MaterialTheme.typography.labelSmall,
            color = Neon,
            modifier = Modifier.padding(top = 3.dp),
        )
        Text(verso.texto, style = MaterialTheme.typography.bodyLarge, color = Texto)
    }
}

@Composable
private fun Indice(livros: List<LivroEntity>, aoEscolher: (LivroEntity, Int) -> Unit) {
    var escolhido by remember { mutableStateOf<LivroEntity?>(null) }
    val livro = escolhido

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
        Text(
            if (livro == null) "ESCOLHA O LIVRO" else "CAPÍTULO DE ${livro.nome.uppercase()}",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = TextoSecundario,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        if (livro == null) {
            LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(livros, key = { it.numero }) { item ->
                    Text(
                        item.nome,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Texto,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { escolhido = item }
                            .padding(vertical = 10.dp),
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items((1..livro.capitulos).toList()) { numero ->
                    Cartao(Modifier.clickable { aoEscolher(livro, numero) }, corBorda = Borda) {
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

@Preview(showBackground = true, backgroundColor = 0xFF0A0F1C, widthDp = 390, heightDp = 844)
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
            ),
            aoAbrir = { _, _ -> },
            aoAlternarCronometro = {},
            aoAvancar = {},
            aoVoltar = {},
            aoSair = {},
        )
    }
}
