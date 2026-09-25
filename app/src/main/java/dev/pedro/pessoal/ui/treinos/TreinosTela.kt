package dev.pedro.pessoal.ui.treinos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pedro.pessoal.domain.model.Exercicio
import dev.pedro.pessoal.domain.model.TREINOS_PARA_PROGREDIR
import dev.pedro.pessoal.domain.model.Treino
import dev.pedro.pessoal.ui.componentes.BarraNeon
import dev.pedro.pessoal.ui.componentes.Cartao
import dev.pedro.pessoal.ui.componentes.Icones
import dev.pedro.pessoal.ui.componentes.TelaEmConstrucao
import dev.pedro.pessoal.ui.theme.Borda
import dev.pedro.pessoal.ui.theme.Ganho
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.PessoalTema
import dev.pedro.pessoal.ui.theme.Texto
import dev.pedro.pessoal.ui.theme.TextoSecundario
import dev.pedro.pessoal.ui.theme.Trilho

/** Em que variação cada exercício está e quantos treinos faltam para subir. */
data class ProgressoExercicio(val nivel: Int, val treinosCompletos: Int)

@Composable
fun TreinosTela(
    treino: Treino?,
    progresso: Map<String, ProgressoExercicio>,
    concluido: Boolean,
    aoConcluir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (treino == null) {
        TelaEmConstrucao(
            "TREINOS",
            Icones.Halter,
            "Hoje é dia de descanso. O próximo treino aparece aqui no dia certo.",
            modifier,
        )
        return
    }

    // Quais séries já foram marcadas nesta sessão: "nome do exercício" para conjunto de índices.
    val marcadas = remember(treino) { mutableStateMapOf<String, Set<Int>>() }
    val totalSeries = treino.exercicios.sumOf { it.series }
    val feitas by remember { derivedStateOf { marcadas.values.sumOf { it.size } } }

    Column(modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("TREINOS", style = MaterialTheme.typography.headlineMedium, color = Neon)
        Text(
            treino.rotulo,
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario,
            modifier = Modifier.padding(top = 2.dp),
        )

        Cartao(Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "SÉRIES",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                        color = TextoSecundario,
                    )
                    Text(
                        "$feitas / $totalSeries",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (feitas == totalSeries) Ganho else Texto,
                    )
                }
                BarraNeon(feitas.toFloat() / totalSeries)
            }
        }

        LazyColumn(
            Modifier.weight(1f).padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(treino.exercicios, key = { it.nome }) { exercicio ->
                CartaoExercicio(
                    exercicio = exercicio,
                    progresso = progresso[exercicio.nome] ?: ProgressoExercicio(0, 0),
                    marcadas = marcadas[exercicio.nome] ?: emptySet(),
                    aoAlternar = { serie ->
                        val atual = marcadas[exercicio.nome] ?: emptySet()
                        marcadas[exercicio.nome] = if (serie in atual) atual - serie else atual + serie
                    },
                )
            }
        }

        Button(
            onClick = aoConcluir,
            enabled = !concluido && feitas == totalSeries,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Neon,
                contentColor = Color(0xFF07101F),
                disabledContainerColor = Trilho,
                disabledContentColor = TextoSecundario,
            ),
        ) {
            Text(
                when {
                    concluido -> "TREINO CONCLUÍDO HOJE"
                    feitas == totalSeries -> "CONCLUIR TREINO"
                    else -> "FALTAM ${totalSeries - feitas} SÉRIES"
                },
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun CartaoExercicio(
    exercicio: Exercicio,
    progresso: ProgressoExercicio,
    marcadas: Set<Int>,
    aoAlternar: (Int) -> Unit,
) {
    Cartao(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${exercicio.nome} ${exercicio.variacao(progresso.nivel)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Texto,
                    )
                    Text(
                        "${exercicio.series} séries de ${exercicio.repeticoes}" +
                            if (exercicio.porTempo) " segundos" else " repetições",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(exercicio.series) { i ->
                    Botao(numero = i + 1, marcada = i in marcadas, aoTocar = { aoAlternar(i) })
                }
            }

            if (exercicio.podeProgredir(progresso.nivel)) {
                val faltam = TREINOS_PARA_PROGREDIR - progresso.treinosCompletos
                Text(
                    if (faltam <= 0) {
                        "Pronto para subir de variação no próximo treino"
                    } else {
                        "Faltam $faltam treinos para subir de variação"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (faltam <= 0) Ganho else TextoSecundario,
                )
            }
        }
    }
}

@Composable
private fun Botao(numero: Int, marcada: Boolean, aoTocar: () -> Unit) {
    val forma = RoundedCornerShape(8.dp)
    Box(
        Modifier
            .size(40.dp)
            .clip(forma)
            .background(if (marcada) Ganho else Color.Transparent)
            .border(1.dp, if (marcada) Ganho else Borda, forma)
            .clickable(onClick = aoTocar),
        contentAlignment = Alignment.Center,
    ) {
        if (marcada) {
            Icon(Icones.Confirmar, contentDescription = null, tint = Color(0xFF0A0F1C), modifier = Modifier.size(18.dp))
        } else {
            Text("$numero", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0F1C, widthDp = 390, heightDp = 844)
@Composable
private fun TreinosPreview() {
    PessoalTema {
        TreinosTela(
            treino = Treino.A,
            progresso = mapOf("Flexão" to ProgressoExercicio(nivel = 1, treinosCompletos = 2)),
            concluido = false,
            aoConcluir = {},
        )
    }
}
