package dev.pedro.pessoal.ui.itens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pedro.pessoal.domain.engine.Regras
import dev.pedro.pessoal.domain.engine.SugestaoAtributo
import dev.pedro.pessoal.domain.model.Atributo
import dev.pedro.pessoal.domain.model.LembretePessoal
import dev.pedro.pessoal.ui.componentes.CampoTexto
import dev.pedro.pessoal.ui.theme.Borda
import dev.pedro.pessoal.ui.theme.ChakraPetch
import dev.pedro.pessoal.ui.theme.Fundo
import dev.pedro.pessoal.ui.theme.Ganho
import dev.pedro.pessoal.ui.theme.Neon
import dev.pedro.pessoal.ui.theme.SobreNeon
import dev.pedro.pessoal.ui.theme.Superficie
import dev.pedro.pessoal.ui.theme.Texto
import dev.pedro.pessoal.ui.theme.TextoSecundario
import java.time.LocalTime

/**
 * Criar ou editar um lembrete. Sobe de baixo, sobre a lista.
 *
 * [original] nulo quer dizer um lembrete novo; com valor, é edição — e aí aparece o botão
 * de apagar, que não faz sentido para algo que ainda não existe.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioLembrete(
    original: LembretePessoal?,
    aoSalvar: (LembretePessoal) -> Unit,
    aoApagar: (Long) -> Unit,
    aoFechar: () -> Unit,
) {
    var titulo by remember { mutableStateOf(original?.titulo.orEmpty()) }
    var descricao by remember { mutableStateOf(original?.descricao.orEmpty()) }
    var hora by remember { mutableStateOf(original?.hora ?: LocalTime.of(7, 0)) }
    var dias by remember { mutableStateOf(original?.dias ?: DIAS_TODOS) }
    var atributo by remember { mutableStateOf(original?.atributo ?: Atributo.DISCIPLINA) }
    var escolhendoHora by remember { mutableStateOf(false) }

    /**
     * Enquanto a pessoa não tocar num atributo, o título manda. Editar um lembrete que já
     * existe começa travado: quem escolheu Espírito para "Ler a Bíblia" não quer ver a
     * escolha mudar sozinha ao corrigir uma vírgula no título.
     */
    var escolhaManual by remember { mutableStateOf(original != null) }
    var veioDoTitulo by remember { mutableStateOf(false) }

    LaunchedEffect(titulo, escolhaManual) {
        if (escolhaManual) return@LaunchedEffect
        val sugerido = SugestaoAtributo.de(titulo)
        veioDoTitulo = sugerido != null
        if (sugerido != null) atributo = sugerido
    }

    val valido = titulo.trim().length >= 2 && dias.isNotEmpty()

    ModalBottomSheet(
        onDismissRequest = aoFechar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Fundo,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Text(
                if (original == null) "NOVO LEMBRETE" else "EDITAR LEMBRETE",
                color = Neon,
                fontFamily = ChakraPetch,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 2.sp,
            )

            Spacer(Modifier.height(20.dp))
            CampoTexto(valor = titulo, aoMudar = { titulo = it }, rotulo = "Título")
            Spacer(Modifier.height(12.dp))
            CampoTexto(valor = descricao, aoMudar = { descricao = it }, rotulo = "Descrição", ultimo = true)

            Spacer(Modifier.height(22.dp))
            Rotulo("HORÁRIO")
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "%02d:%02d".format(hora.hour, hora.minute),
                    color = Texto,
                    fontFamily = ChakraPetch,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Superficie)
                        .border(1.dp, Borda, RoundedCornerShape(12.dp))
                        .clickable { escolhendoHora = true }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    "O aviso chega ${Regras.AVISO_ANTES_MIN} minutos antes",
                    color = TextoSecundario,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(22.dp))
            Rotulo("DIAS")
            Spacer(Modifier.height(10.dp))
            SeletorDias(
                escolhidos = dias,
                aoAlternar = { dia -> dias = if (dia in dias) dias - dia else dias + dia },
            )

            Spacer(Modifier.height(22.dp))
            Rotulo("O QUE ISSO TREINA")
            Spacer(Modifier.height(4.dp))
            Text(
                "Cumprir rende +0,${Regras.GANHO_LEMBRETE_PESSOAL} no atributo escolhido.",
                color = TextoSecundario,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Atributo.entries.forEach { opcao ->
                    val ativo = opcao == atributo
                    Text(
                        opcao.sigla,
                        color = if (ativo) SobreNeon else TextoSecundario,
                        fontFamily = ChakraPetch,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (ativo) Neon else Superficie)
                            .border(1.dp, if (ativo) Neon else Borda, RoundedCornerShape(10.dp))
                            .selectable(
                                selected = ativo,
                                role = Role.RadioButton,
                                onClick = {
                                    atributo = opcao
                                    // A partir daqui o título não mexe mais na escolha.
                                    escolhaManual = true
                                    veioDoTitulo = false
                                },
                            )
                            .padding(vertical = 12.dp),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                // Dizer que veio do título é o que separa sugerir de decidir por alguém.
                if (veioDoTitulo) atributo.nome + " · sugerido pelo título" else atributo.nome,
                color = Ganho,
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(Modifier.height(26.dp))
            Button(
                onClick = {
                    aoSalvar(
                        LembretePessoal(
                            id = original?.id ?: 0L,
                            titulo = titulo.trim(),
                            descricao = descricao.trim(),
                            hora = hora,
                            dias = dias,
                            atributo = atributo,
                        ),
                    )
                    aoFechar()
                },
                enabled = valido,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = SobreNeon),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Salvar", fontWeight = FontWeight.SemiBold)
            }

            if (original != null) {
                TextButton(
                    onClick = {
                        aoApagar(original.id)
                        aoFechar()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Apagar lembrete", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (escolhendoHora) {
        val estado = rememberTimePickerState(
            initialHour = hora.hour,
            initialMinute = hora.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { escolhendoHora = false },
            containerColor = Superficie,
            confirmButton = {
                TextButton(onClick = {
                    hora = LocalTime.of(estado.hour, estado.minute)
                    escolhendoHora = false
                }) { Text("Pronto", color = Neon) }
            },
            dismissButton = {
                TextButton(onClick = { escolhendoHora = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            },
            text = { TimeInput(state = estado) },
        )
    }
}

@Composable
internal fun Rotulo(texto: String) {
    Text(
        texto,
        color = Texto,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.5.sp,
    )
}

private val DIAS_TODOS = java.time.DayOfWeek.entries.toSet()
