package dev.pedro.pessoal.data.nuvem

/**
 * Para onde vai o contador quando uma conta é apagada.
 *
 * Só o dono do último número pode devolvê-lo: se ninguém se cadastrou depois dele, o
 * contador volta um, e quem criar a próxima conta (inclusive ele mesmo) recebe o mesmo
 * número de novo. Se alguém já chegou depois, o número fica vago — renumerar os outros
 * mudaria a identidade de quem não apagou nada.
 *
 * Devolve o novo total, ou `null` quando o contador deve ficar como está.
 */
internal fun totalAposApagar(numeroDoPerfil: Long?, totalAtual: Long?): Long? {
    if (numeroDoPerfil == null || totalAtual == null) return null
    if (numeroDoPerfil <= 0L) return null
    return if (numeroDoPerfil == totalAtual) totalAtual - 1 else null
}
