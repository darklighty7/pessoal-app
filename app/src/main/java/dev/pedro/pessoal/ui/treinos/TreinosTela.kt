package dev.pedro.pessoal.ui.treinos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import dev.pedro.pessoal.ui.componentes.fadeInferior
import dev.pedro.pessoal.ui.theme.BordaForte
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.pedro.pessoal.domain.model.ExercicioPessoal
import dev.pedro.pessoal.ui.componentes.BotaoIcone
import dev.pedro.pessoal.ui.itens.FormularioExercicio
import dev.pedro.pessoal.ui.itens.resumoDeDias
import dev.pedro.pessoal.ui.theme.Borda
import java.time.DayOfWeek
import java.time.LocalDate
import dev.pedro.pessoal.ui.theme.ChakraPetch
import dev.pedro.pessoal.ui.theme.SobreGanho
import dev.pedro.pessoal.ui.theme.SobreNeon
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
    pessoais: List<ExercicioPessoal>,
    aoConcluir: () -> Unit,
    aoSalvarExercicio: (ExercicioPessoal) -> Unit,
    aoApagarExercicio: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Nulo = formulário fechado. Um exercício de id zero é novo; com id, é edição.
    var emEdicao by remember { mutableStateOf<ExercicioPessoal?>(null) }
    val abrirNovo = {
        emEdicao = ExercicioPessoal(
            id = 0L,
            nome = "",
            series = 3,
            repeticoes = 12,
            porTempo = false,
            dias = setOf(LocalDate.now().dayOfWeek),
        )
    }

    emEdicao?.let { atual ->
        FormularioExercicio(
            original = atual.takeIf { it.id > 0 },
            aoSalvar = aoSalvarExercicio,
            aoApagar = aoApagarExercicio,
            aoFechar = { emEdicao = null },
        )
    }

    if (treino == null) {
        // Sem treino fixo hoje: ou é descanso, ou a pessoa montou o próprio.
        Column(modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)) {
            Text("Treinos", style = MaterialTheme.typography.headlineMedium, color = Texto)
            Text(
                if (pessoais.isEmpty()) "Hoje é dia de descanso." else "Os seus exercícios de hoje.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario,
                modifier = Modifier.padding(top = 4.dp),
            )
            LazyColumn(
                Modifier.weight(1f).padding(top = 20.dp).fadeInferior(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                meusExercicios(pessoais, abrirNovo) { emEdicao = it }
            }
        }
        return
    }

    // Quais séries já foram marcadas nesta sessão: "nome do exercício" para conjunto de índices.
    val marcadas = remember(treino) { mutableStateMapOf<String, Set<Int>>() }
    val totalSeries = treino.exercicios.sumOf { it.series }
    val feitas by remember { derivedStateOf { marcadas.values.sumOf { it.size } } }

    Column(modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)) {
        Text("Treinos", style = MaterialTheme.typography.headlineMedium, color = Texto)
        Text(
            treino.rotulo,
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario,
            modifier = Modifier.padding(top = 4.dp),
        )

        Cartao(Modifier.fillMaxWidth().padding(top = 24.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Séries", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                    Text(
                        "$feitas / $totalSeries",
                        fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                        color = if (feitas == totalSeries) Ganho else Texto,
                    )
                }
                BarraNeon(feitas.toFloat() / totalSeries, cor = if (feitas == totalSeries) Ganho else Neon)
            }
        }

        LazyColumn(
            Modifier.weight(1f).padding(top = 12.dp).fadeInferior(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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

            meusExercicios(pessoais, abrirNovo) { emEdicao = it }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = aoConcluir,
            enabled = !concluido && feitas == totalSeries,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Neon,
                contentColor = SobreNeon,
                disabledContainerColor = Trilho,
                disabledContentColor = TextoSecundario,
            ),
        ) {
            Text(
                when {
                    concluido -> "Treino concluído hoje"
                    feitas == totalSeries -> "Concluir treino"
                    else -> "Faltam ${totalSeries - feitas} séries"
                },
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                letterSpacing = 0.6.sp,
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
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "${exercicio.nome} ${exercicio.variacao(progresso.nivel)}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = Texto,
                    )
                    Text(
                        "${exercicio.series} séries de ${exercicio.repeticoes}" +
                            if (exercicio.porTempo) " segundos" else " repetições",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(exercicio.series) { i ->
                    Botao(numero = i + 1, marcada = i in marcadas, aoTocar = { aoAlternar(i) }, modifier = Modifier.weight(1f))
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
private fun Botao(numero: Int, marcada: Boolean, aoTocar: () -> Unit, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(14.dp)
    Box(
        modifier
            .height(48.dp)
            .clip(forma)
            .background(if (marcada) Ganho else Trilho)
            .border(1.dp, if (marcada) Ganho else BordaForte, forma)
            .clickable(onClick = aoTocar),
        contentAlignment = Alignment.Center,
    ) {
        if (marcada) {
            Icon(Icones.Confirmar, contentDescription = null, tint = SobreGanho, modifier = Modifier.size(20.dp))
        } else {
            Text("$numero", fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Texto)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0E14, widthDp = 390, heightDp = 844)
@Composable
private fun TreinosPreview() {
    PessoalTema {
        TreinosTela(
            treino = Treino.A,
            progresso = mapOf("Flexão" to ProgressoExercicio(nivel = 1, treinosCompletos = 2)),
            concluido = false,
            pessoais = listOf(
                ExercicioPessoal(
                    id = 1,
                    nome = "Prancha lateral",
                    series = 3,
                    repeticoes = 40,
                    porTempo = true,
                    dias = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                ),
            ),
            aoConcluir = {},
            aoSalvarExercicio = {},
            aoApagarExercicio = {},
        )
    }
}

/**
 * A parte da lista que é da pessoa: o que ela criou para hoje, mais o botão de criar.
 *
 * Os exercícios criados não entram na contagem de séries nem liberam o botão de concluir —
 * isso pertence ao treino fixo. Aqui eles são o conteúdo do treino, não a pontuação: quem
 * quiser que treinar valha pontos cria um lembrete de Força, que é onde a conta acontece.
 */
private fun LazyListScope.meusExercicios(
    pessoais: List<ExercicioPessoal>,
    aoCriar: () -> Unit,
    aoEditar: (ExercicioPessoal) -> Unit,
) {
    if (pessoais.isNotEmpty()) {
        item(key = "titulo-meus") {
            Text(
                "MEUS EXERCÍCIOS",
                style = MaterialTheme.typography.labelMedium,
                color = TextoSecundario,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }

    items(pessoais, key = { "pessoal-" + it.id }) { exercicio ->
        Cartao(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        exercicio.nome,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Texto,
                    )
                    Text(
                        exercicio.resumo + " · " + resumoDeDias(exercicio.dias),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario,
                    )
                }
                BotaoIcone(Icones.Lapis, "Editar " + exercicio.nome, { aoEditar(exercicio) })
            }
        }
    }

    item(key = "novo-exercicio") {
        Cartao(Modifier.fillMaxWidth(), corBorda = Borda) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 60.dp).clickable(onClick = aoCriar)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icones.Mais, contentDescription = null, tint = Neon, modifier = Modifier.size(20.dp))
                Text(
                    "Novo exercício",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Neon,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }

    item(key = "respiro-exercicios") { Spacer(Modifier.height(8.dp)) }
}
