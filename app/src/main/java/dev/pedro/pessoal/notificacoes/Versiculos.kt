package dev.pedro.pessoal.notificacoes

import java.time.LocalDate

/**
 * Versículos que giram na tela de bloqueio, um por dia. Ficam no código porque a
 * notificação precisa funcionar mesmo antes de a Bíblia ter sido baixada.
 */
object Versiculos {
    private val lista = listOf(
        "Tudo posso naquele que me fortalece." to "Filipenses 4:13",
        "O Senhor é o meu pastor; de nada terei falta." to "Salmos 23:1",
        "Sejam fortes e corajosos. Não tenham medo." to "Deuteronômio 31:6",
        "Entregue o seu caminho ao Senhor; confie nele, e ele agirá." to "Salmos 37:5",
        "Tudo o que fizerem, façam de todo o coração, como para o Senhor." to "Colossenses 3:23",
        "Buscai primeiro o Reino de Deus e a sua justiça." to "Mateus 6:33",
        "A disciplina produz uma colheita de justiça e paz." to "Hebreus 12:11",
        "Corramos com perseverança a corrida que nos é proposta." to "Hebreus 12:1",
        "Não se amoldem ao padrão deste mundo, mas transformem-se." to "Romanos 12:2",
        "Este é o dia que o Senhor fez; alegremo-nos e exultemos nele." to "Salmos 118:24",
        "Confie no Senhor de todo o seu coração." to "Provérbios 3:5",
        "Os que esperam no Senhor renovam as suas forças." to "Isaías 40:31",
        "Combati o bom combate, terminei a corrida, guardei a fé." to "2 Timóteo 4:7",
        "O Senhor lutará por vocês; tão somente acalmem-se." to "Êxodo 14:14",
    )

    /** O versículo do dia: sempre o mesmo para uma data, girando pela lista. */
    fun doDia(data: LocalDate = LocalDate.now()): Pair<String, String> =
        lista[(data.toEpochDay().mod(lista.size))]
}
