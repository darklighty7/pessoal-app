package dev.pedro.pessoal.domain.engine

import dev.pedro.pessoal.domain.model.Atributo
import java.text.Normalizer
import java.util.Locale

/**
 * Adivinha qual atributo um lembrete treina, a partir do título.
 *
 * É sugestão, nunca decisão: devolve nulo quando não reconhece nada, e a tela só move a
 * escolha enquanto a pessoa não tiver tocado em nenhum atributo. Assim, quando erra, o erro
 * acontece na frente de quem está escrevendo e dá para corrigir antes de salvar.
 *
 * As listas são de começos de palavra, não de palavras inteiras: "estud" pega estudar,
 * estudo e estudando sem precisar de uma entrada para cada conjugação.
 */
object SugestaoAtributo {

    fun de(titulo: String): Atributo? {
        val palavras = normalizar(titulo).split(SEPARADORES).filter { it.length >= 3 }
        if (palavras.isEmpty()) return null

        var melhor: Atributo? = null
        var tamanhoDoAchado = 0

        // Vence o começo de palavra mais longo, porque é o mais específico. Em "ler a
        // Bíblia", "bibli" (5) ganha de "ler" (3), e o lembrete vai para Espírito — que é
        // o que alguém quer dizer ao escrever isso.
        for ((atributo, inicios) in PISTAS) {
            for (inicio in inicios) {
                if (palavras.any { it.startsWith(inicio) } && inicio.length > tamanhoDoAchado) {
                    melhor = atributo
                    tamanhoDoAchado = inicio.length
                }
            }
        }
        return melhor
    }

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto.lowercase(PT_BR), Normalizer.Form.NFD)
            .replace(ACENTOS, "")

    private val PT_BR: Locale = Locale.forLanguageTag("pt-BR")
    private val ACENTOS = Regex("\\p{Mn}+")
    private val SEPARADORES = Regex("[^a-z0-9]+")

    /**
     * A ordem importa só para desempate: com dois começos do mesmo tamanho, fica o
     * primeiro encontrado. Espírito vem antes porque as palavras dele são as menos
     * ambíguas — "oracao" e "culto" não significam outra coisa.
     */
    private val PISTAS: List<Pair<Atributo, List<String>>> = listOf(
        Atributo.ESPIRITO to listOf(
            "ora", "orei", "bibli", "deus", "jesus", "cristo", "igreja", "culto", "louvor",
            "adora", "devocional", "jejum", "gratid", "salmo", "versicul", "missa", "celula",
            "dizimo", "evangelh", "espirit", "palavra", "testemunh", "discipul",
        ),
        Atributo.FORCA to listOf(
            "corr", "caminha", "trein", "academia", "musculac", "flexa", "abdomin",
            "agacha", "along", "karate", "luta", "nata", "nadar", "bicicl", "pedal",
            "esporte", "futebol", "exercici", "malhar", "peso", "forca", "pranch",
            "polichinel", "barra", "jiu", "judo", "boxe", "crossfit", "yoga", "pilates",
            "serie", "repetic", "cardio", "corrida", "alongamento",
        ),
        Atributo.INTELIGENCIA to listOf(
            "estud", "ler", "leit", "livro", "curso", "aula", "faculdade", "prova",
            "ingles", "idioma", "program", "codigo", "aprend", "anota", "revis", "licao",
            "escrev", "redac", "matemati", "pesquis", "podcast", "documentari",
            "apostila", "resumo", "caderno", "vocabul",
        ),
        Atributo.DISCIPLINA to listOf(
            "acord", "dormir", "agua", "beber", "arrum", "limp", "organiz", "planej",
            "rotina", "horario", "cedo", "celular", "dieta", "comer", "refeic", "economiz",
            "poup", "tarefa", "meta", "lista", "banho", "roupa", "louca", "cama", "orcamento",
        ),
    )
}
