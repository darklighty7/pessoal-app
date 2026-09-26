package dev.pedro.pessoal.domain.engine

/**
 * Todos os números do sistema num lugar só, para ajustar depois de algumas semanas de uso.
 * Atributos em centésimos: 10 = 0,10.
 */
object Regras {
    const val VALOR_INICIAL_ATRIBUTO = 0   // tudo começa zerado

    // Níveis: nível 1 pede 100 XP, e cada nível seguinte pede 2 XP a mais que o anterior.
    // Crescimento fixo e suave: fazendo tudo certo todos os dias (~55 XP/dia), dá para
    // alcançar o rank S (nível 100) em torno de 1 ano; D em ~18 dias, C em ~2 meses,
    // B em ~4 meses, A em ~7 meses.
    const val XP_BASE = 100
    const val XP_INCREMENTO_POR_NIVEL = 2
    fun xpParaProximoNivel(nivel: Int): Int = XP_BASE + XP_INCREMENTO_POR_NIVEL * (nivel - 1)

    // Desgaste constante
    const val DESGASTE_DIARIO_XP = 2

    // Perdas por falhas seguidas de um mesmo hábito
    const val PERDA_DUAS_FALHAS = 10          // −0,10
    const val PERDA_TRES_OU_MAIS_FALHAS = 20  // −0,20 por dia
    const val PERDA_DISCIPLINA_SEQUENCIA = 5  // −0,05 por dia, se algum hábito estiver com 3+ falhas

    // Bônus
    const val BONUS_DIA_PERFEITO_XP = 15
    const val BONUS_DIA_PERFEITO_DISCIPLINA = 10
    const val BONUS_SEQUENCIA_7 = 30          // +0,30
    const val BONUS_SEQUENCIA_30 = 100        // +1,00 a cada 30 dias

    // Classes
    const val MARGEM_CLASSE = 50              // 0,50 de diferença
    const val DIAS_PARA_TROCAR_CLASSE = 3

    // Leitura da Bíblia (por tempo, dividida entre Espírito e Inteligência)
    const val META_LEITURA_MIN = 10
    const val XP_META_LEITURA = 10
    const val ESPIRITO_META_LEITURA = 10          // +0,10 ao atingir a meta
    const val INTELIGENCIA_META_LEITURA = 5       // +0,05 exatamente nos 10 minutos
    const val INTELIGENCIA_ACIMA_META_LEITURA = 10 // +0,10 a partir de 11 minutos
    const val XP_MAX_LEITURA_DIA = 30
}
