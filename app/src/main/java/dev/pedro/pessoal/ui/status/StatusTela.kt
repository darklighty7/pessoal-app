package dev.pedro.pessoal.ui.status

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pedro.pessoal.domain.engine.Regras
import dev.pedro.pessoal.domain.model.Atributo
import dev.pedro.pessoal.domain.model.Classe
import dev.pedro.pessoal.domain.model.Status
import dev.pedro.pessoal.domain.model.formatarAtributo
import dev.pedro.pessoal.ui.componentes.BarraNeon
import dev.pedro.pessoal.ui.componentes.BotaoIcone
import dev.pedro.pessoal.ui.componentes.Cartao
import dev.pedro.pessoal.ui.componentes.Icones
import dev.pedro.pessoal.ui.theme.Alerta
import dev.pedro.pessoal.ui.theme.ChakraPetch
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.PessoalTema
import dev.pedro.pessoal.ui.theme.Texto
import dev.pedro.pessoal.ui.theme.TextoSecundario
import dev.pedro.pessoal.ui.theme.Trilho

@Composable
fun StatusTela(status: Status, aoAbrirConfiguracoes: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Cabecalho(aoAbrirConfiguracoes)
        CartaoPerfil(status)
        BarraExperiencia(status)
        GradeAtributos(status)
        Cartao(Modifier.fillMaxWidth()) {
            GraficoRadar(status.atributos, Modifier.align(Alignment.Center).padding(vertical = 8.dp))
        }
        ProgressoClasse(status)
    }
}

@Composable
private fun Cabecalho(aoAbrirConfiguracoes: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Status", style = MaterialTheme.typography.headlineMedium, color = Texto)
        Spacer(Modifier.weight(1f))
        BotaoIcone(Icones.Ajustes, "Configurações", aoTocar = aoAbrirConfiguracoes)
    }
}

@Composable
private fun CartaoPerfil(status: Status) {
    Cartao(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SeloRank(status.rank, tamanho = 68.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(status.nome, style = MaterialTheme.typography.titleLarge, color = Texto)
                Text(
                    "Nível ${status.nivel} · ${status.classe.nome}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                )
                Text(status.titulo.nome, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
            }
            EtiquetaRank(status.rank, Modifier.align(Alignment.Top))
        }
    }
}

/** Etiqueta pequena com o material do rank (FERRO, BRONZE...). */
@Composable
private fun EtiquetaRank(rank: dev.pedro.pessoal.domain.model.Rank, modifier: Modifier = Modifier) {
    Text(
        nomeMaterial(rank).uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.9.sp),
        color = TextoSecundario,
        modifier = modifier
            .background(Trilho, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun BarraExperiencia(status: Status) {
    Column(Modifier.padding(horizontal = 2.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text("Experiência", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
            Spacer(Modifier.weight(1f))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Texto)) { append("${status.xp} ") }
                    withStyle(SpanStyle(color = TextoSecundario, fontWeight = FontWeight.Medium)) { append("/ ${status.xpNecessario} XP") }
                },
                fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
            )
        }
        BarraNeon(status.xp.toFloat() / status.xpNecessario, brasa = true)
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Atributo.entries.chunked(2).forEach { linha ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                linha.forEach { atributo ->
                    Cartao(Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    Modifier.size(32.dp).background(Neon.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(icones.getValue(atributo), contentDescription = null, tint = Neon, modifier = Modifier.size(18.dp))
                                }
                                Text(atributo.nome, style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                            }
                            Text(
                                formatarAtributo(status.valor(atributo)),
                                fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = 30.sp,
                                color = Texto,
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
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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

@Preview(name = "Início", showBackground = true, backgroundColor = 0xFF0B0E14, widthDp = 390, heightDp = 844)
@Composable
private fun StatusInicioPreview() {
    PessoalTema { StatusTela(Status(), aoAbrirConfiguracoes = {}) }
}

@Preview(name = "Após 4 dias (simulado)", showBackground = true, backgroundColor = 0xFF0B0E14, widthDp = 390, heightDp = 844)
@Composable
private fun StatusSimuladoPreview() {
    PessoalTema { StatusTela(StatusExemplo.gerar(), aoAbrirConfiguracoes = {}) }
}
