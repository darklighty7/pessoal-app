package dev.pedro.pessoal.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.pedro.pessoal.R

/** Fonte de destaque para títulos e números. */
val ChakraPetch = FontFamily(
    Font(R.font.chakra_petch_semibold, FontWeight.SemiBold),
    Font(R.font.chakra_petch_bold, FontWeight.Bold),
)

/** Fonte de leitura para o resto. */
val IbmPlexSans = FontFamily(
    Font(R.font.ibm_plex_sans_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_semibold, FontWeight.SemiBold),
)

val Tipografia = Typography(
    // Título de tela: sem caixa alta e sem espaçamento largo entre as letras.
    headlineMedium = TextStyle(fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, letterSpacing = (-0.3).sp),
    titleLarge = TextStyle(fontFamily = ChakraPetch, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleMedium = TextStyle(fontFamily = IbmPlexSans, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyLarge = TextStyle(fontFamily = IbmPlexSans, fontWeight = FontWeight.Normal, fontSize = 17.sp),
    bodyMedium = TextStyle(fontFamily = IbmPlexSans, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = IbmPlexSans, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelSmall = TextStyle(fontFamily = IbmPlexSans, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelMedium = TextStyle(fontFamily = IbmPlexSans, fontWeight = FontWeight.Medium, fontSize = 13.sp),
)
