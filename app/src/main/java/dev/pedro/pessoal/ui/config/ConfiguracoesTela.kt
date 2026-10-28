package dev.pedro.pessoal.ui.config

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import dev.pedro.pessoal.domain.model.Habito
import dev.pedro.pessoal.domain.model.PerfilUsuario
import dev.pedro.pessoal.domain.model.Status
import dev.pedro.pessoal.ui.componentes.BotaoIcone
import dev.pedro.pessoal.ui.componentes.CampoTexto
import dev.pedro.pessoal.ui.componentes.Cartao
import dev.pedro.pessoal.ui.componentes.Icones
import dev.pedro.pessoal.ui.theme.Alerta
import dev.pedro.pessoal.ui.theme.ChakraPetch
import dev.pedro.pessoal.ui.theme.Fundo
import dev.pedro.pessoal.ui.theme.Ganho
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.SobreNeon
import dev.pedro.pessoal.ui.theme.Superficie
import dev.pedro.pessoal.ui.theme.Texto
import dev.pedro.pessoal.ui.theme.TextoSecundario
import dev.pedro.pessoal.ui.theme.Trilho

/**
 * Configurações. Duas seções com donos diferentes: a conta é de quem está logado, e o
 * sistema é do dono do app.
 *
 * O que separa as duas não é esconder botão — é que ligar e desligar hábito fixo ou zerar
 * progresso alheio não são decisões de quem só usa o aplicativo.
 */
@Composable
fun ConfiguracoesTela(
    perfil: PerfilUsuario,
    status: Status,
    estado: EstadoConfiguracoes,
    versao: String,
    aoVoltar: () -> Unit,
    aoRenomear: (String) -> Unit,
    aoAlternarHabito: (Habito) -> Unit,
    aoZerarProgresso: () -> Unit,
    aoApagarConta: () -> Unit,
    aoSair: () -> Unit,
    aoPedirTotal: () -> Unit,
    aoLimparRecado: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contexto = LocalContext.current
    var nome by remember(perfil.nomePersonagem) { mutableStateOf(perfil.nomePersonagem) }
    var confirmando by remember { mutableStateOf<Confirmacao?>(null) }

    LaunchedEffect(perfil.admin) { if (perfil.admin) aoPedirTotal() }

    Column(
        modifier
            .fillMaxSize()
            .background(Fundo)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BotaoIcone(Icones.Voltar, "Voltar", aoVoltar)
            Spacer(Modifier.size(12.dp))
            Text("Configurações", style = MaterialTheme.typography.headlineMedium, color = Texto)
        }

        if (estado.recado != null) {
            Spacer(Modifier.height(16.dp))
            Cartao(Modifier.fillMaxWidth(), corBorda = Alerta) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        estado.recado,
                        color = Texto,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = aoLimparRecado) { Text("Ok", color = Neon) }
                }
            }
        }

        // ----- A conta, de quem está logado -----
        Secao("SUA CONTA")

        Cartao(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                CampoTexto(valor = nome, aoMudar = { nome = it }, rotulo = "Nome do personagem", ultimo = true)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = { aoRenomear(nome) },
                        enabled = nome.trim().length >= 2 && nome.trim() != perfil.nomePersonagem,
                    ) {
                        Text("Salvar nome", color = Neon, fontWeight = FontWeight.SemiBold)
                    }
                }
                Linha("E-mail", perfil.email)
                if (perfil.numeroJogador > 0) {
                    Linha("Jogador", "N° " + perfil.numeroJogador.toString().padStart(3, '0'))
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Permissoes(contexto)

        Spacer(Modifier.height(12.dp))
        Acao("Sair da conta", "Você continua existindo; só este aparelho esquece.", aoSair)

        Spacer(Modifier.height(10.dp))
        Acao(
            titulo = "Apagar a conta",
            descricao = "Apaga o personagem, os lembretes e o acesso. Não tem volta.",
            cor = Alerta,
            habilitado = !estado.ocupado,
            aoTocar = { confirmando = Confirmacao.APAGAR_CONTA },
        )

        // ----- O sistema, do dono do app -----
        if (perfil.admin) {
            Secao("SISTEMA")
            Text(
                "Visível só para o administrador.",
                color = TextoSecundario,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Cartao(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(vertical = 8.dp)) {
                    Text(
                        "HÁBITOS FIXOS",
                        color = TextoSecundario,
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 4.dp),
                    )
                    Text(
                        "Desligar tira o hábito das Missões do dia e cancela o aviso. " +
                            "O histórico que já existe não é apagado.",
                        color = TextoSecundario,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
                    )
                    Habito.entries.forEach { habito ->
                        InterruptorHabito(
                            habito = habito,
                            ligado = habito !in status.habitosDesligados,
                            aoAlternar = { aoAlternarHabito(habito) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Cartao(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Linha(
                        "Jogadores cadastrados",
                        estado.totalJogadores?.toString() ?: "—",
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Acao(
                titulo = "Zerar o progresso",
                descricao = "Volta ao nível 1 e apaga o histórico. O personagem e a classe ficam.",
                cor = Alerta,
                aoTocar = { confirmando = Confirmacao.ZERAR },
            )
        }

        Spacer(Modifier.height(28.dp))
        Text(
            "Forja $versao",
            color = TextoSecundario,
            fontFamily = ChakraPetch,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(40.dp))
    }

    confirmando?.let { qual ->
        AlertDialog(
            onDismissRequest = { confirmando = null },
            containerColor = Superficie,
            title = { Text(qual.titulo, color = Texto, fontWeight = FontWeight.SemiBold) },
            text = { Text(qual.texto, color = TextoSecundario, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    confirmando = null
                    when (qual) {
                        Confirmacao.APAGAR_CONTA -> aoApagarConta()
                        Confirmacao.ZERAR -> aoZerarProgresso()
                    }
                }) { Text(qual.confirmar, color = Alerta, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = null }) { Text("Cancelar", color = TextoSecundario) }
            },
        )
    }
}

private enum class Confirmacao(val titulo: String, val texto: String, val confirmar: String) {
    APAGAR_CONTA(
        "Apagar a conta?",
        "O personagem, o progresso, os lembretes e o acesso somem deste aparelho e da nuvem. " +
            "Não há como desfazer.",
        "Apagar tudo",
    ),
    ZERAR(
        "Zerar o progresso?",
        "O nível volta a 1 e o histórico de dias é apagado. O nome, a classe e os lembretes ficam.",
        "Zerar",
    ),
}

@Composable
private fun Secao(titulo: String) {
    Spacer(Modifier.height(28.dp))
    Text(
        titulo,
        color = Texto,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(bottom = 12.dp),
    )
}

@Composable
private fun Linha(rotulo: String, valor: String) {
    Row(
        Modifier.fillMaxWidth().padding(top = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(rotulo, color = TextoSecundario, style = MaterialTheme.typography.bodyMedium)
        Text(valor, color = Texto, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun Acao(
    titulo: String,
    descricao: String,
    aoTocar: () -> Unit,
    cor: androidx.compose.ui.graphics.Color = Texto,
    habilitado: Boolean = true,
) {
    Cartao(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .clickable(enabled = habilitado, onClick = aoTocar)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(titulo, color = if (habilitado) cor else TextoSecundario, fontWeight = FontWeight.Medium)
            Text(descricao, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun InterruptorHabito(habito: Habito, ligado: Boolean, aoAlternar: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                habito.rotulo,
                color = if (ligado) Texto else TextoSecundario,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(habito.descricao, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = ligado,
            onCheckedChange = { aoAlternar() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = SobreNeon,
                checkedTrackColor = Neon,
                uncheckedThumbColor = TextoSecundario,
                uncheckedTrackColor = Trilho,
            ),
        )
    }
}

/**
 * Notificações e alarmes exatos. O app não consegue conceder nenhum dos dois sozinho — o
 * máximo que dá para fazer é dizer como está e abrir a tela certa do sistema.
 */
@Composable
private fun Permissoes(contexto: Context) {
    val notificacoes = NotificationManagerCompat.from(contexto).areNotificationsEnabled()
    val exatos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        contexto.getSystemService(AlarmManager::class.java)?.canScheduleExactAlarms() ?: false
    } else {
        true
    }

    Cartao(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 6.dp)) {
            Permissao(
                titulo = "Notificações",
                liberada = notificacoes,
                textoQuandoFalta = "Sem isso, nenhum lembrete aparece.",
                aoAbrir = {
                    contexto.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, contexto.packageName)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                },
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Permissao(
                    titulo = "Alarmes exatos",
                    liberada = exatos,
                    textoQuandoFalta = "Sem isso, os avisos podem atrasar alguns minutos.",
                    aoAbrir = {
                        contexto.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                .setData(Uri.parse("package:" + contexto.packageName))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun Permissao(titulo: String, liberada: Boolean, textoQuandoFalta: String, aoAbrir: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(enabled = !liberada, onClick = aoAbrir)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, color = Texto, style = MaterialTheme.typography.bodyLarge)
            Text(
                if (liberada) "Liberado" else textoQuandoFalta,
                color = if (liberada) Ganho else Alerta,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (liberada) {
            Icon(Icones.Confirmar, contentDescription = null, tint = Ganho, modifier = Modifier.size(20.dp))
        } else {
            Text(
                "Abrir",
                color = Neon,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(Superficie, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}
