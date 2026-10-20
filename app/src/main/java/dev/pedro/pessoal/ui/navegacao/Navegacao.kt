package dev.pedro.pessoal.ui.navegacao

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import dev.pedro.pessoal.ui.theme.Borda
import dev.pedro.pessoal.ui.theme.Superficie
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.pedro.pessoal.domain.model.Habito
import dev.pedro.pessoal.domain.model.Treino
import dev.pedro.pessoal.ui.AppViewModel
import dev.pedro.pessoal.ui.biblia.BibliaTela
import dev.pedro.pessoal.ui.biblia.BibliaViewModel
import dev.pedro.pessoal.ui.componentes.Icones
import dev.pedro.pessoal.ui.lembretes.ItemLembrete
import dev.pedro.pessoal.ui.lembretes.LembretesTela
import dev.pedro.pessoal.ui.status.StatusTela
import dev.pedro.pessoal.ui.theme.Fundo
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.TextoSecundario
import dev.pedro.pessoal.ui.treinos.ProgressoExercicio
import dev.pedro.pessoal.ui.treinos.TreinosTela

private enum class Aba(val rota: String, val rotulo: String, val icone: ImageVector) {
    STATUS("status", "Status", Icones.Escudo),
    LEMBRETES("lembretes", "Lembretes", Icones.Sino),
    TREINOS("treinos", "Treinos", Icones.Halter),
}

private const val ROTA_BIBLIA = "biblia"

@Composable
fun AppNavegacao(appViewModel: AppViewModel, bibliaViewModel: BibliaViewModel) {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rotaAtual = entrada?.destination?.route

    val status by appViewModel.status.collectAsStateWithLifecycle()
    val hoje by appViewModel.hoje.collectAsStateWithLifecycle()
    val registros by appViewModel.registrosDeHoje.collectAsStateWithLifecycle()
    val progresso by appViewModel.progressoDoTreino.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Fundo,
        bottomBar = {
            // A tela da Bíblia ocupa tudo: sem barra embaixo.
            if (rotaAtual != ROTA_BIBLIA) {
                BarraInferior(rotaAtual) { aba ->
                    nav.navigate(aba.rota) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        },
    ) { margens ->
        NavHost(nav, startDestination = Aba.STATUS.rota, modifier = Modifier.padding(margens)) {
            composable(Aba.STATUS.rota) { StatusTela(status) }

            composable(Aba.LEMBRETES.rota) {
                val porHabito = registros.associateBy { it.habito }
                LembretesTela(
                    data = hoje,
                    itens = Habito.doDia(hoje).map { habito ->
                        val registro = porHabito[habito.name]
                        ItemLembrete(
                            habito = habito,
                            concluido = registro?.concluido == true,
                            minutosLeitura = registro?.minutosLeitura ?: 0,
                        )
                    },
                    aoMarcar = appViewModel::marcar,
                    aoAbrirBiblia = { nav.navigate(ROTA_BIBLIA) },
                )
            }

            composable(Aba.TREINOS.rota) {
                val treino = Treino.doDia(hoje)
                val habito = if (treino == Treino.MOBILIDADE) Habito.MOBILIDADE else Habito.TREINO_CASA
                TreinosTela(
                    treino = treino,
                    progresso = progresso.mapValues { (_, p) -> ProgressoExercicio(p.nivel, p.treinosCompletos) },
                    concluido = registros.any { it.habito == habito.name && it.concluido },
                    aoConcluir = { treino?.let(appViewModel::concluirTreino) },
                )
            }

            composable(ROTA_BIBLIA) {
                val estadoBiblia by bibliaViewModel.estado.collectAsStateWithLifecycle()
                BibliaTela(
                    estado = estadoBiblia,
                    aoAbrir = bibliaViewModel::abrir,
                    aoAlternarCronometro = bibliaViewModel::alternarCronometro,
                    aoAvancar = bibliaViewModel::proximoCapitulo,
                    aoVoltar = bibliaViewModel::capituloAnterior,
                    aoSair = bibliaViewModel::encerrarLeitura,
                    aoAlternarSelecao = bibliaViewModel::alternarSelecao,
                    aoGrifar = bibliaViewModel::grifarSelecionados,
                    aoRemoverGrifos = bibliaViewModel::removerGrifosSelecionados,
                    aoLimparSelecao = bibliaViewModel::limparSelecao,
                    aoAbrirGrifos = bibliaViewModel::carregarTodosGrifos,
                    aoDefinirTamanho = bibliaViewModel::definirTamanhoFonte,
                    aoSalvarTamanho = bibliaViewModel::salvarTamanhoFonte,
                    aoZoom = bibliaViewModel::aplicarZoom,
                )
            }
        }
    }
}

/** Barra inferior: três abas, o item ativo ganha um indicador fino no topo. */
@Composable
private fun BarraInferior(rotaAtual: String?, aoEscolher: (Aba) -> Unit) {
    Column(Modifier.fillMaxWidth().background(Superficie)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Borda))
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().height(72.dp).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Aba.entries.forEach { aba ->
                val ativa = rotaAtual == aba.rota
                val cor = if (ativa) Neon else TextoSecundario
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(selected = ativa, role = Role.Tab, onClick = { aoEscolher(aba) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .width(28.dp)
                            .height(3.dp)
                            .background(if (ativa) Neon else Color.Transparent, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)),
                    )
                    Spacer(Modifier.height(11.dp))
                    Icon(aba.icone, contentDescription = null, tint = cor, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        aba.rotulo,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (ativa) FontWeight.SemiBold else FontWeight.Medium,
                        color = cor,
                    )
                }
            }
        }
    }
}
