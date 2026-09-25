package dev.pedro.pessoal.domain.model

/**
 * Faixas de nível de cada rank. Onde uma faixa termina, a próxima já começa
 * (nível 10 é D, não E) — por isso cada limite abaixo é o PRIMEIRO nível do rank.
 */
enum class Rank(val nivelMinimo: Int) {
    E(1),
    D(10),
    C(25),
    B(45),
    A(70),
    S(100);

    companion object {
        fun doNivel(nivel: Int): Rank = entries.last { nivel >= it.nivelMinimo }
    }
}
