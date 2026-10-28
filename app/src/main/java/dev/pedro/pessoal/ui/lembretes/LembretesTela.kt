package dev.pedro.pessoal.ui.lembretes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pedro.pessoal.domain.engine.Regras
import dev.pedro.pessoal.domain.model.Habito
import dev.pedro.pessoal.ui.componentes.BarraNeon
import dev.pedro.pessoal.ui.componentes.Cartao
import dev.pedro.pessoal.ui.componentes.Icones
import dev.pedro.pessoal.ui.theme.ChakraPetch
import dev.pedro.pessoal.ui.theme.SobreGanho
import dev.pedro.pessoal.ui.theme.Ganho
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.PessoalTema
import dev.pedro.pessoal.ui.theme.Texto
import dev.pedro.pessoal.ui.theme.TextoSecundario
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** O que a tela precisa saber sobre um hábito do dia. */
data class ItemLembrete(
    val habito: Habito,
    val concluido: Boolean,
    val minutosLeitura: Int = 0,
)

@Composable
fun LembretesTela(
    data: LocalDate,
    itens: List<ItemLembrete>,
    aoMarcar: (Habito, Boolean) -> Unit,
    aoAbrirBiblia: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val obrigatorios = itens.filter { it.habito.obrigatorio }
    val feitos = obrigatorios.count { it.concluido }

    Column(modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 24.dp)) {
        Text("Lembretes", style = MaterialTheme.typography.headlineMedium, color = Texto)
        Text(
            data.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("pt-BR")))
                .replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario,
            modifier = Modifier.padding(top = 4.dp),
        )

        Cartao(Modifier.fillMaxWidth().padding(top = 24.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Missões do dia", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                    Text(
                        "$feitos / ${obrigatorios.size}",
                        fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                        color = if (feitos == obrigatorios.size && obrigatorios.isNotEmpty()) Ganho else Texto,
                    )
                }
                val diaPerfeito = feitos == obrigatorios.size && obrigatorios.isNotEmpty()
                BarraNeon(
                    if (obrigatorios.isEmpty()) 0f else feitos.toFloat() / obrigatorios.size,
                    cor = if (diaPerfeito) Ganho else Neon,
                )
                if (diaPerfeito) {
                    Text(
                        "Dia perfeito: +${Regras.BONUS_DIA_PERFEITO_XP} XP na virada da meia-noite",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Ganho,
                    )
                }
            }
        }

        LazyColumn(
            Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(itens, key = { it.habito.name }) { item ->
                LinhaLembrete(
                    item = item,
                    aoMarcar = { aoMarcar(item.habito, it) },
                    aoAbrirBiblia = aoAbrirBiblia,
                )
            }
        }
    }
}

@Composable
private fun LinhaLembrete(item: ItemLembrete, aoMarcar: (Boolean) -> Unit, aoAbrirBiblia: () -> Unit) {
    val ehBiblia = item.habito == Habito.BIBLIA
    Cartao(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 76.dp).padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Caixa(
                marcada = item.concluido,
                // A Bíblia é marcada pelo cronômetro da leitura, não pela mão.
                habilitada = !ehBiblia,
                aoMudar = aoMarcar,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    item.habito.rotulo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (item.concluido) TextoSecundario else Texto,
                    textDecoration = if (item.concluido) TextDecoration.LineThrough else null,
                )
                Text(
                    if (ehBiblia && item.minutosLeitura > 0) {
                        "${item.minutosLeitura} min lidos hoje"
                    } else {
                        item.habito.descricao
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                )
            }
            if (ehBiblia) {
                TextButton(onClick = aoAbrirBiblia, modifier = Modifier.heightIn(min = 44.dp)) {
                    Text("Ler", color = Neon, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            } else {
                Text(
                    item.habito.hora.toString(),
                    fontFamily = ChakraPetch, fontWeight = FontWeight.Medium, fontSize = 15.sp,
                    color = TextoSecundario,
                )
            }
        }
    }
}

@Composable
private fun Caixa(marcada: Boolean, habilitada: Boolean, aoMudar: (Boolean) -> Unit) {
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (marcada) Ganho else Color.Transparent)
            .border(2.dp, if (marcada) Ganho else TextoSecundario.copy(alpha = 0.4f), CircleShape)
            .then(if (habilitada) Modifier.clickable { aoMudar(!marcada) } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (marcada) {
            Icon(Icones.Confirmar, contentDescription = null, tint = SobreGanho, modifier = Modifier.size(18.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0E14, widthDp = 390, heightDp = 844)
@Composable
private fun LembretesPreview() {
    val hoje = LocalDate.of(2026, 9, 22) // uma segunda-feira
    PessoalTema {
        LembretesTela(
            data = hoje,
            itens = Habito.doDia(hoje, admin = true).map {
                ItemLembrete(it, concluido = it == Habito.ORACAO, minutosLeitura = if (it == Habito.BIBLIA) 4 else 0)
            },
            aoMarcar = { _, _ -> },
            aoAbrirBiblia = {},
        )
    }
}
