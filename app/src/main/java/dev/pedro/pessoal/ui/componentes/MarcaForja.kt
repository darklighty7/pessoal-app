package dev.pedro.pessoal.ui.componentes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import dev.pedro.pessoal.R
import dev.pedro.pessoal.ui.theme.ChakraPetch
import dev.pedro.pessoal.ui.theme.Texto

/**
 * A marca: o F ao lado da palavra.
 *
 * A palavra é texto de verdade, na Chakra Petch que o app já usa nos títulos — não um
 * desenho. Assim ela acompanha qualquer altura sem precisar de um vetor por tamanho.
 *
 * O tamanho do texto sai de [altura] convertido pela densidade, e não em `sp` direto: um
 * logotipo não deve crescer junto com o ajuste de fonte do sistema, senão a palavra
 * passa por cima do F.
 *
 * Tudo vira um elemento só para o leitor de tela, que anuncia "Forja" em vez de ler um
 * desenho sem nome seguido de uma palavra solta.
 */
@Composable
fun MarcaForja(altura: Dp, modifier: Modifier = Modifier) {
    val tamanhoDaPalavra = with(LocalDensity.current) { (altura * 0.80f).toSp() }

    Row(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = "Forja" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(altura * 0.26f),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_marca_forja),
            contentDescription = null,
            modifier = Modifier.height(altura).aspectRatio(PROPORCAO),
        )
        Text(
            text = "forja",
            fontFamily = ChakraPetch,
            fontWeight = FontWeight.Bold,
            fontSize = tamanhoDaPalavra,
            letterSpacing = (-0.5).sp,
            color = Texto,
        )
    }
}

/** A caixa do vetor da marca: 68,01 por 74. */
private const val PROPORCAO = 68.01f / 74f
