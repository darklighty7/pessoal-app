package dev.pedro.pessoal.ui.itens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.pedro.pessoal.ui.theme.Borda
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.SobreNeon
import dev.pedro.pessoal.ui.theme.Superficie
import dev.pedro.pessoal.ui.theme.TextoSecundario
import java.time.DayOfWeek

/**
 * Os sete dias da semana como círculos que acendem.
 *
 * Começa na segunda porque é assim que [DayOfWeek] numera e é como a semana é lida aqui.
 * As iniciais repetem — três "S" e dois "Q" —, então a posição é que distingue; por isso
 * cada botão leva o nome inteiro para o leitor de tela.
 */
@Composable
fun SeletorDias(
    escolhidos: Set<DayOfWeek>,
    aoAlternar: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DayOfWeek.entries.forEach { dia ->
            val ativo = dia in escolhidos
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (ativo) Neon else Superficie)
                    .border(1.dp, if (ativo) Neon else Borda, CircleShape)
                    .toggleable(value = ativo, onValueChange = { aoAlternar(dia) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    INICIAIS.getValue(dia),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (ativo) FontWeight.Bold else FontWeight.Medium,
                    color = if (ativo) SobreNeon else TextoSecundario,
                )
            }
        }
    }
}

private val INICIAIS = mapOf(
    DayOfWeek.MONDAY to "S",
    DayOfWeek.TUESDAY to "T",
    DayOfWeek.WEDNESDAY to "Q",
    DayOfWeek.THURSDAY to "Q",
    DayOfWeek.FRIDAY to "S",
    DayOfWeek.SATURDAY to "S",
    DayOfWeek.SUNDAY to "D",
)

/** "Todo dia", "Seg, Qua e Sex", "Nenhum dia" — como a lista aparece no cartão. */
fun resumoDeDias(dias: Set<DayOfWeek>): String = when {
    dias.isEmpty() -> "Nenhum dia"
    dias.size == 7 -> "Todo dia"
    dias.size == 5 && DayOfWeek.SATURDAY !in dias && DayOfWeek.SUNDAY !in dias -> "De segunda a sexta"
    dias.size == 2 && DayOfWeek.SATURDAY in dias && DayOfWeek.SUNDAY in dias -> "Fim de semana"
    else -> {
        val nomes = dias.sortedBy { it.value }.map { CURTOS.getValue(it) }
        if (nomes.size == 1) nomes.first()
        else nomes.dropLast(1).joinToString(", ") + " e " + nomes.last()
    }
}

private val CURTOS = mapOf(
    DayOfWeek.MONDAY to "Seg",
    DayOfWeek.TUESDAY to "Ter",
    DayOfWeek.WEDNESDAY to "Qua",
    DayOfWeek.THURSDAY to "Qui",
    DayOfWeek.FRIDAY to "Sex",
    DayOfWeek.SATURDAY to "Sáb",
    DayOfWeek.SUNDAY to "Dom",
)
