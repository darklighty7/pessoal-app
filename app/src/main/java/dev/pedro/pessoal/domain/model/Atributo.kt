package dev.pedro.pessoal.domain.model

/** Os quatro atributos do personagem. Valores guardados em centésimos (10 = 0,10). Começam em zero. */
enum class Atributo(val nome: String, val sigla: String) {
    FORCA("Força", "FOR"),
    INTELIGENCIA("Inteligência", "INT"),
    DISCIPLINA("Disciplina", "DIS"),
    ESPIRITO("Espírito", "ESP"),
}
