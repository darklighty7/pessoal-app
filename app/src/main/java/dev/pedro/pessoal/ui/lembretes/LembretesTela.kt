package dev.pedro.pessoal.ui.lembretes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.pedro.pessoal.domain.model.Atributo
import dev.pedro.pessoal.domain.model.LembretePessoal
import dev.pedro.pessoal.ui.componentes.BotaoIcone
import dev.pedro.pessoal.ui.itens.FormularioLembrete
import dev.pedro.pessoal.ui.itens.resumoDeDias
import dev.pedro.pessoal.ui.theme.Borda
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** O que a tela precisa saber sobre um hábito fixo do dia. */
data class ItemLembrete(
    val habito: Habito,
    val concluido: Boolean,
    val minutosLeitura: Int = 0,
)

/** O mesmo, para um lembrete que a própria pessoa criou. */
data class ItemCriado(
    val lembrete: LembretePessoal,
    val concluido: Boolean,
)

@Composable
fun LembretesTela(
    data: LocalDate,
    itens: List<ItemLembrete>,
    pessoais: List<ItemCriado>,
    aoMarcar: (Habito, Boolean) -> Unit,
    aoMarcarPessoal: (LembretePessoal, Boolean) -> Unit,
    aoAbrirBiblia: () -> Unit,
    aoSalvarLembrete: (LembretePessoal) -> Unit,
    aoApagarLembrete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Nulo = formulário fechado. Um lembrete de id zero é um novo; com id, é edição.
    var emEdicao by remember { mutableStateOf<LembretePessoal?>(null) }

    // Os criados contam na barra junto com os fixos, porque contam também no fechamento
    // do dia. Para quem não é o dono, são eles que fazem o dia existir.
    val obrigatorios = itens.filter { it.habito.obrigatorio }
    val feitos = obrigatorios.count { it.concluido } + pessoais.count { it.concluido }
    val total = obrigatorios.size + pessoais.size

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
                        "$feitos / $total",
                        fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                        color = if (feitos == total && total > 0) Ganho else Texto,
                    )
                }
                val diaPerfeito = feitos == total && total > 0
                BarraNeon(
                    if (total == 0) 0f else feitos.toFloat() / total,
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

            items(pessoais, key = { it.lembrete.chaveRegistro }) { item ->
                LinhaCriada(
                    item = item,
                    aoMarcar = { aoMarcarPessoal(item.lembrete, it) },
                    aoEditar = { emEdicao = item.lembrete },
                )
            }

            item(key = "novo") {
                BotaoNovo("Novo lembrete") {
                    emEdicao = LembretePessoal(
                        id = 0L,
                        titulo = "",
                        descricao = "",
                        hora = LocalTime.of(7, 0),
                        dias = DayOfWeek.entries.toSet(),
                        atributo = Atributo.DISCIPLINA,
                    )
                }
            }

            item(key = "respiro") { Spacer(Modifier.height(16.dp)) }
        }
    }

    emEdicao?.let { atual ->
        FormularioLembrete(
            // Id zero é um lembrete que ainda não existe: o formulário abre vazio e sem
            // o botão de apagar.
            original = atual.takeIf { it.id > 0 },
            aoSalvar = aoSalvarLembrete,
            aoApagar = aoApagarLembrete,
            aoFechar = { emEdicao = null },
        )
    }
}

@Composable
private fun LinhaCriada(item: ItemCriado, aoMarcar: (Boolean) -> Unit, aoEditar: () -> Unit) {
    Cartao(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 76.dp).padding(start = 20.dp, end = 8.dp, top = 18.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Caixa(marcada = item.concluido, habilitada = true, aoMudar = aoMarcar)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    item.lembrete.titulo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (item.concluido) TextoSecundario else Texto,
                    textDecoration = if (item.concluido) TextDecoration.LineThrough else null,
                )
                Text(
                    item.lembrete.descricao.ifBlank { resumoDeDias(item.lembrete.dias) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                )
            }
            Text(
                "%02d:%02d".format(item.lembrete.hora.hour, item.lembrete.hora.minute),
                fontFamily = ChakraPetch, fontWeight = FontWeight.Medium, fontSize = 15.sp,
                color = TextoSecundario,
            )
            BotaoIcone(Icones.Lapis, "Editar ${item.lembrete.titulo}", aoEditar)
        }
    }
}

@Composable
private fun BotaoNovo(texto: String, aoTocar: () -> Unit) {
    Cartao(Modifier.fillMaxWidth(), corBorda = Borda) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 60.dp).clickable(onClick = aoTocar).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icones.Mais, contentDescription = null, tint = Neon, modifier = Modifier.size(20.dp))
            Text(texto, style = MaterialTheme.typography.bodyLarge, color = Neon, fontWeight = FontWeight.Medium)
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
            pessoais = listOf(
                ItemCriado(
                    lembrete = LembretePessoal(
                        id = 1,
                        titulo = "Estudar inglês",
                        descricao = "Meia hora de leitura em voz alta",
                        hora = LocalTime.of(21, 0),
                        dias = DayOfWeek.entries.toSet(),
                        atributo = Atributo.INTELIGENCIA,
                    ),
                    concluido = false,
                ),
            ),
            aoMarcar = { _, _ -> },
            aoMarcarPessoal = { _, _ -> },
            aoAbrirBiblia = {},
            aoSalvarLembrete = {},
            aoApagarLembrete = {},
        )
    }
}
