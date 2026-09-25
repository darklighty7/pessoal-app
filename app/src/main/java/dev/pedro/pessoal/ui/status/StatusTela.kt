package dev.pedro.pessoal.ui.status

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pedro.pessoal.domain.engine.Regras
import dev.pedro.pessoal.domain.model.Atributo
import dev.pedro.pessoal.domain.model.Classe
import dev.pedro.pessoal.domain.model.Status
import dev.pedro.pessoal.domain.model.formatarAtributo
import dev.pedro.pessoal.ui.componentes.BarraNeon
import dev.pedro.pessoal.ui.componentes.Cartao
import dev.pedro.pessoal.ui.componentes.Icones
import dev.pedro.pessoal.ui.theme.Alerta
import dev.pedro.pessoal.ui.theme.Borda
import dev.pedro.pessoal.ui.theme.ChakraPetch
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.PessoalTema
import dev.pedro.pessoal.ui.theme.SuperficieBaixa
import dev.pedro.pessoal.ui.theme.TextoSecundario
import dev.pedro.pessoal.ui.theme.Trilho

@Composable
fun StatusTela(status: Status, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Cabecalho()
        CartaoPerfil(status)
        BarraExperiencia(status)
        GradeAtributos(status)
        Cartao(Modifier.fillMaxWidth(), fundo = SuperficieBaixa, cantos = 14.dp) {
            GraficoRadar(status.atributos, Modifier.align(Alignment.Center).padding(vertical = 8.dp))
        }
        ProgressoClasse(status)
    }
}

@Composable
private fun Cabecalho() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("STATUS", style = MaterialTheme.typography.headlineMedium, color = Neon)
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = { /* Configurações: próximas etapas */ },
            modifier = Modifier.border(1.dp, Borda, RoundedCornerShape(10.dp)),
        ) {
            Icon(Icones.Ajustes, contentDescription = "Configurações", tint = TextoSecundario, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun CartaoPerfil(status: Status) {
    Cartao(Modifier.fillMaxWidth(), corBorda = Neon.copy(alpha = 0.35f), cantos = 14.dp) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinhaPerfil("NOME", status.nome)
                LinhaPerfil("NÍVEL", status.nivel.toString())
                LinhaPerfil("CLASSE", status.classe.nome)
                LinhaPerfil("TÍTULO", status.titulo.nome)
            }
            SeloComRotulo(status.rank)
        }
    }
}

@Composable
private fun LinhaPerfil(rotulo: String, valor: String) {
    Row {
        Text(rotulo, style = MaterialTheme.typography.labelMedium, color = TextoSecundario, modifier = Modifier.width(80.dp))
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SeloComRotulo(rank: dev.pedro.pessoal.domain.model.Rank) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SeloRank(rank)
        Text(
            "RANK · ${nomeMaterial(rank).uppercase()}",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = TextoSecundario,
        )
    }
}

@Composable
private fun BarraExperiencia(status: Status) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text("EXPERIÊNCIA", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
            Spacer(Modifier.weight(1f))
            Text(
                "${status.xp} / ${status.xpNecessario} XP",
                fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
            )
        }
        BarraNeon(status.xp.toFloat() / status.xpNecessario)
    }
}

@Composable
private fun GradeAtributos(status: Status) {
    val icones: Map<Atributo, ImageVector> = mapOf(
        Atributo.FORCA to Icones.Halter,
        Atributo.INTELIGENCIA to Icones.Lampada,
        Atributo.DISCIPLINA to Icones.Alvo,
        Atributo.ESPIRITO to Icones.Cruz,
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Atributo.entries.chunked(2).forEach { linha ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                linha.forEach { atributo ->
                    Cartao(Modifier.weight(1f)) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icones.getValue(atributo), contentDescription = null, tint = Neon, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(atributo.nome.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                            }
                            Text(
                                formatarAtributo(status.valor(atributo)),
                                fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = 22.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressoClasse(status: Status) {
    val candidata = status.classeCandidata
    Cartao(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).background(Trilho, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icones.Chama, contentDescription = null, tint = Alerta, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (candidata != null) {
                    Text("Rumo a ${candidata.nome}", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${status.diasCandidata} de ${Regras.DIAS_PARA_TROCAR_CLASSE} dias",
                        style = MaterialTheme.typography.bodySmall, color = TextoSecundario,
                    )
                } else if (status.classe == Classe.APRENDIZ) {
                    Text("Classe ainda não revelada", style = MaterialTheme.typography.titleMedium)
                    Text("Seus hábitos vão definir quem você se torna", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                } else {
                    Text("Classe estável", style = MaterialTheme.typography.titleMedium)
                    Text("Seus hábitos definem a próxima classe", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                }
            }
        }
    }
}

@Preview(name = "Início", showBackground = true, backgroundColor = 0xFF0A0F1C, widthDp = 390, heightDp = 844)
@Composable
private fun StatusInicioPreview() {
    PessoalTema { StatusTela(Status()) }
}

@Preview(name = "Após 4 dias (simulado)", showBackground = true, backgroundColor = 0xFF0A0F1C, widthDp = 390, heightDp = 844)
@Composable
private fun StatusSimuladoPreview() {
    PessoalTema { StatusTela(StatusExemplo.gerar()) }
}
