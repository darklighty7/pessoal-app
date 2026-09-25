package dev.pedro.pessoal.ui.navegacao

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import dev.pedro.pessoal.ui.theme.SuperficieBaixa
import dev.pedro.pessoal.ui.theme.TextoSecundario
import dev.pedro.pessoal.ui.treinos.ProgressoExercicio
import dev.pedro.pessoal.ui.treinos.TreinosTela

private enum class Aba(val rota: String, val rotulo: String, val icone: ImageVector) {
    STATUS("status", "STATUS", Icones.Escudo),
    LEMBRETES("lembretes", "LEMBRETES", Icones.Sino),
    TREINOS("treinos", "TREINOS", Icones.Halter),
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
                NavigationBar(containerColor = SuperficieBaixa, tonalElevation = 0.dp) {
                    Aba.entries.forEach { aba ->
                        NavigationBarItem(
                            selected = rotaAtual == aba.rota,
                            onClick = {
                                nav.navigate(aba.rota) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(aba.icone, contentDescription = null, modifier = Modifier.size(22.dp)) },
                            label = { Text(aba.rotulo) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Neon,
                                selectedTextColor = Neon,
                                unselectedIconColor = TextoSecundario,
                                unselectedTextColor = TextoSecundario,
                                indicatorColor = Color.Transparent,
                            ),
                        )
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
                )
            }
        }
    }
}
