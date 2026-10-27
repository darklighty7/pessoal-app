package dev.pedro.pessoal.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pedro.pessoal.domain.engine.MotorPontuacao
import dev.pedro.pessoal.domain.model.Classe
import dev.pedro.pessoal.domain.model.PerfilUsuario
import dev.pedro.pessoal.domain.model.formatarVariacao
import dev.pedro.pessoal.ui.componentes.Cartao
import dev.pedro.pessoal.ui.componentes.Icones
import dev.pedro.pessoal.ui.componentes.MarcaForja
import dev.pedro.pessoal.ui.theme.Borda
import dev.pedro.pessoal.ui.theme.Fundo
import dev.pedro.pessoal.ui.theme.Ganho
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.SobreNeon
import dev.pedro.pessoal.ui.theme.Texto
import dev.pedro.pessoal.ui.theme.TextoSecundario

/**
 * Classes oferecidas na criação. São as puras mais o Aprendiz: as híbridas e o Mestre
 * continuam sendo conquistadas pelo motor, conforme os atributos crescem.
 */
private data class OpcaoClasse(val classe: Classe, val icone: ImageVector, val descricao: String)

/**
 * O que a escolha rende de saída, lido do próprio motor em vez de escrito à mão — assim o
 * texto do cartão nunca descola da regra se o bônus mudar.
 */
private fun bonusDe(classe: Classe): String {
    val alvos = Classe.atributosDe(classe)
    val iniciais = MotorPontuacao.atributosIniciais(classe)
    return if (alvos.isEmpty()) {
        formatarVariacao(iniciais.values.first()) + " em cada atributo"
    } else {
        alvos.joinToString(" e ") { formatarVariacao(iniciais.getValue(it)) + " de " + it.nome }
    }
}

private val OPCOES = listOf(
    OpcaoClasse(Classe.APRENDIZ, Icones.Alvo, "Sem inclinação. Deixa os atributos decidirem o caminho."),
    OpcaoClasse(Classe.GUERREIRO, Icones.Halter, "Caminho da Força: treino e constância no corpo."),
    OpcaoClasse(Classe.SACERDOTE, Icones.Cruz, "Caminho do Espírito: leitura e oração em primeiro lugar."),
    OpcaoClasse(Classe.SABIO, Icones.Lampada, "Caminho da Inteligência: estudo e leitura longa."),
    OpcaoClasse(Classe.SENTINELA, Icones.Escudo, "Caminho da Disciplina: nunca quebrar a sequência."),
)

/**
 * Último passo da entrada: por onde começar.
 *
 * Vem depois da abertura de propósito. Escolher um caminho antes de ouvir qual é a missão
 * seria responder a uma pergunta que ninguém fez — aqui a pessoa já sabe para quê.
 */
@Composable
fun ClasseTela(
    perfil: PerfilUsuario,
    aoConfirmar: (Classe) -> Unit,
    aoSair: () -> Unit,
) {
    var escolhida by remember { mutableStateOf(Classe.APRENDIZ) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Fundo)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(32.dp))
        MarcaForja(altura = 24.dp)

        // Quem a abertura acabou de anunciar. Sem isto, a tela pareceria um formulário
        // solto; com isto, é a continuação da conversa que o sistema começou.
        Spacer(Modifier.height(24.dp))
        Text(
            perfil.nomePersonagem.uppercase(),
            color = Texto,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
        if (perfil.numeroJogador > 0) {
            Spacer(Modifier.height(2.dp))
            Text(
                "JOGADOR N° " + perfil.numeroJogador.toString().padStart(3, '0'),
                color = Neon,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 2.sp,
            )
        }

        Spacer(Modifier.height(26.dp))
        Text("ESCOLHA O SEU CAMINHO", color = Texto, style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "A escolha rende um bônus nos atributos da classe e vale como ponto de partida. " +
                "Conforme os atributos crescerem, a classe pode mudar sozinha.",
            color = TextoSecundario,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(14.dp))

        OPCOES.forEach { opcao ->
            val ativa = opcao.classe == escolhida
            Cartao(
                corBorda = if (ativa) Neon else Borda,
                cantos = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .selectable(selected = ativa, role = Role.RadioButton, onClick = { escolhida = opcao.classe }),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(
                        opcao.icone,
                        contentDescription = null,
                        tint = if (ativa) Neon else TextoSecundario,
                        modifier = Modifier.size(26.dp),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            opcao.classe.nome,
                            color = if (ativa) Neon else Texto,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(opcao.descricao, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        Text(bonusDe(opcao.classe), color = Ganho, style = MaterialTheme.typography.labelSmall)
                    }
                    if (ativa) {
                        Icon(Icones.Confirmar, contentDescription = "Selecionada", tint = Neon, modifier = Modifier.size(20.dp))
                    } else {
                        Box(Modifier.size(20.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { aoConfirmar(escolhida) },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = SobreNeon),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("Começar", fontWeight = FontWeight.SemiBold)
        }

        TextButton(onClick = aoSair, modifier = Modifier.fillMaxWidth()) {
            Text("Sair desta conta", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(32.dp))
    }
}
