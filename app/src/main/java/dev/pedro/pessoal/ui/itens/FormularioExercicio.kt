package dev.pedro.pessoal.ui.itens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pedro.pessoal.domain.model.ExercicioPessoal
import dev.pedro.pessoal.ui.componentes.CampoTexto
import dev.pedro.pessoal.ui.theme.Borda
import dev.pedro.pessoal.ui.theme.ChakraPetch
import dev.pedro.pessoal.ui.theme.Fundo
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.SobreNeon
import dev.pedro.pessoal.ui.theme.Superficie
import dev.pedro.pessoal.ui.theme.TextoSecundario
import java.time.DayOfWeek

/**
 * Criar ou editar um exercício. Mais curto que o do lembrete porque exercício não toca
 * alarme: ele aparece na lista do dia e pronto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioExercicio(
    original: ExercicioPessoal?,
    aoSalvar: (ExercicioPessoal) -> Unit,
    aoApagar: (Long) -> Unit,
    aoFechar: () -> Unit,
) {
    var nome by remember { mutableStateOf(original?.nome.orEmpty()) }
    var series by remember { mutableStateOf((original?.series ?: 3).toString()) }
    var repeticoes by remember { mutableStateOf((original?.repeticoes ?: 12).toString()) }
    var porTempo by remember { mutableStateOf(original?.porTempo ?: false) }
    var dias by remember { mutableStateOf(original?.dias ?: DIAS_UTEIS) }

    val numeroSeries = series.toIntOrNull() ?: 0
    val numeroRepeticoes = repeticoes.toIntOrNull() ?: 0
    val valido = nome.trim().length >= 2 && numeroSeries in 1..20 &&
        numeroRepeticoes in 1..600 && dias.isNotEmpty()

    ModalBottomSheet(
        onDismissRequest = aoFechar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Fundo,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Text(
                if (original == null) "NOVO EXERCÍCIO" else "EDITAR EXERCÍCIO",
                color = Neon,
                fontFamily = ChakraPetch,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 2.sp,
            )

            Spacer(Modifier.height(20.dp))
            CampoTexto(valor = nome, aoMudar = { nome = it }, rotulo = "Nome do exercício")

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoTexto(
                    valor = series,
                    aoMudar = { series = it.filter(Char::isDigit).take(2) },
                    rotulo = "Séries",
                    numero = true,
                    modifier = Modifier.weight(1f),
                )
                CampoTexto(
                    valor = repeticoes,
                    aoMudar = { repeticoes = it.filter(Char::isDigit).take(3) },
                    rotulo = if (porTempo) "Segundos" else "Repetições",
                    numero = true,
                    ultimo = true,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Alternativa("Repetições", !porTempo, Modifier.weight(1f)) { porTempo = false }
                Alternativa("Tempo", porTempo, Modifier.weight(1f)) { porTempo = true }
            }

            Spacer(Modifier.height(22.dp))
            Rotulo("DIAS")
            Spacer(Modifier.height(10.dp))
            SeletorDias(
                escolhidos = dias,
                aoAlternar = { dia -> dias = if (dia in dias) dias - dia else dias + dia },
            )

            Spacer(Modifier.height(26.dp))
            Button(
                onClick = {
                    aoSalvar(
                        ExercicioPessoal(
                            id = original?.id ?: 0L,
                            nome = nome.trim(),
                            series = numeroSeries,
                            repeticoes = numeroRepeticoes,
                            porTempo = porTempo,
                            dias = dias,
                        ),
                    )
                    aoFechar()
                },
                enabled = valido,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = SobreNeon),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Salvar", fontWeight = FontWeight.SemiBold)
            }

            if (original != null) {
                TextButton(
                    onClick = {
                        aoApagar(original.id)
                        aoFechar()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Apagar exercício", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Alternativa(
    texto: String,
    ativo: Boolean,
    modifier: Modifier = Modifier,
    aoEscolher: () -> Unit,
) {
    Text(
        texto,
        color = if (ativo) SobreNeon else TextoSecundario,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (ativo) Neon else Superficie)
            .border(1.dp, if (ativo) Neon else Borda, RoundedCornerShape(10.dp))
            .selectable(selected = ativo, role = Role.RadioButton, onClick = aoEscolher)
            .padding(vertical = 12.dp),
    )
}

private val DIAS_UTEIS = setOf(
    DayOfWeek.MONDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.FRIDAY,
)
