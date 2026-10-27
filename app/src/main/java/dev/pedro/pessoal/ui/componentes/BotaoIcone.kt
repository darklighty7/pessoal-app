package dev.pedro.pessoal.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.pedro.pessoal.ui.theme.Borda
import dev.pedro.pessoal.ui.theme.BordaForte
import dev.pedro.pessoal.ui.theme.Superficie
import dev.pedro.pessoal.ui.theme.Texto

/** Botão quadrado de 44dp com ícone de linha, usado nos cabeçalhos das telas. */
@Composable
fun BotaoIcone(
    icone: ImageVector,
    descricao: String,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier,
    cor: Color = Texto,
) {
    val forma = RoundedCornerShape(14.dp)
    Box(
        modifier
            .size(44.dp)
            .clip(forma)
            .background(Superficie)
            .border(1.dp, Borda, forma)
            .clickable(role = Role.Button, onClickLabel = descricao, onClick = aoTocar),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icone, contentDescription = descricao, tint = cor, modifier = Modifier.size(22.dp))
    }
}

/**
 * Botão redondo, usado no rodapé da leitura. Os três controles de lá (capítulo anterior,
 * tamanho do texto e próximo capítulo) usam o mesmo círculo, para o olho reconhecer que
 * são a mesma coisa.
 */
@Composable
fun BotaoRedondo(
    icone: ImageVector,
    descricao: String,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier,
    cor: Color = Texto,
    tamanho: Dp = 40.dp,
    tamanhoIcone: Dp = 20.dp,
) {
    Box(
        modifier
            .size(tamanho)
            .clip(CircleShape)
            .background(Superficie)
            .border(1.dp, BordaForte, CircleShape)
            .clickable(role = Role.Button, onClickLabel = descricao, onClick = aoTocar),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icone, contentDescription = descricao, tint = cor, modifier = Modifier.size(tamanhoIcone))
    }
}
