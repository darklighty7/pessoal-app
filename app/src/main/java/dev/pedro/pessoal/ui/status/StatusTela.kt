package dev.pedro.pessoal.ui.status

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import dev.pedro.pessoal.ui.componentes.Brasa
import dev.pedro.pessoal.ui.componentes.Cartao
import dev.pedro.pessoal.ui.componentes.Icones
import dev.pedro.pessoal.ui.theme.Alerta
import dev.pedro.pessoal.ui.theme.BordaForte
import dev.pedro.pessoal.ui.theme.ChakraPetch
import dev.pedro.pessoal.ui.theme.Fundo
import dev.pedro.pessoal.ui.theme.Ganho
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

/**
 * Cartão de perfil em formato de placar: o selo e o nome no topo, e nível, classe e título
 * em três blocos iguais, cada um com a sua cor (azul = progresso, verde = conquista).
 * A barra de XP fica dentro do cartão, junto do nível que ela faz subir.
 */
@Composable
private fun CartaoPerfil(status: Status) {
    Cartao(Modifier.fillMaxWidth(), cantos = 22.dp) {
        Column(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // O selo e o nome encostam na borda esquerda do cartão; os três blocos abaixo é que
            // são simétricos. Centralizar esta linha deixava um vão à esquerda do selo.
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SeloRank(status.rank, tamanho = 80.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        status.nome,
                        style = MaterialTheme.typography.titleLarge,
                        color = Texto,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "RANK ${status.rank.name} · ${nomeMaterial(status.rank).uppercase()}",
                        fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                        letterSpacing = 1.2.sp,
                        color = corMaterial(status.rank),
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BlocoPlacar("NÍVEL", Neon.copy(alpha = 0.35f), Modifier.weight(1f).fillMaxHeight()) {
                    Text(
                        "${status.nivel}",
                        fontFamily = ChakraPetch, fontWeight = FontWeight.Bold, fontSize = 30.sp,
                        color = Neon,
                        style = TextStyle(shadow = Shadow(Neon.copy(alpha = 0.55f), blurRadius = 28f)),
                    )
                }
                BlocoPlacar("CLASSE", BordaForte, Modifier.weight(1f).fillMaxHeight()) {
                    Icon(iconeDaClasse(status.classe), contentDescription = null, tint = Neon, modifier = Modifier.size(20.dp))
                    Text(
                        status.classe.nome,
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        color = Texto,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                }
                BlocoPlacar("TÍTULO", Ganho.copy(alpha = 0.30f), Modifier.weight(1f).fillMaxHeight()) {
                    Brasa(12.dp, Modifier.padding(vertical = 4.dp))
                    Text(
                        status.titulo.nome,
                        fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 16.sp,
                        color = Ganho,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                }
            }

            BarraExperiencia(status)
        }
    }
}

/** Um dos três blocos do placar: rótulo pequeno em cima e o valor embaixo, com borda colorida. */
@Composable
private fun BlocoPlacar(
    rotulo: String,
    corBorda: Color,
    modifier: Modifier = Modifier,
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier
            .background(Fundo, forma)
            .border(1.dp, corBorda, forma)
            .padding(horizontal = 6.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            rotulo,
            fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.2.sp,
            color = TextoSecundario,
        )
        conteudo()
    }
}

/** Ícone do atributo principal da classe; Aprendiz, sem inclinação, fica com o escudo. */
private fun iconeDaClasse(classe: Classe): ImageVector =
    Classe.atributosDe(classe).firstOrNull()?.let(::iconeDoAtributo) ?: Icones.Escudo

private fun iconeDoAtributo(atributo: Atributo): ImageVector = when (atributo) {
    Atributo.FORCA -> Icones.Halter
    Atributo.INTELIGENCIA -> Icones.Lampada
    Atributo.DISCIPLINA -> Icones.Alvo
    Atributo.ESPIRITO -> Icones.Cruz
}

@Composable
private fun BarraExperiencia(status: Status) {
    Column(Modifier.fillMaxWidth().padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                    Icon(iconeDoAtributo(atributo), contentDescription = null, tint = Neon, modifier = Modifier.size(18.dp))
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
