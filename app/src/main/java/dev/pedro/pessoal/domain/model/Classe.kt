package dev.pedro.pessoal.domain.model

import dev.pedro.pessoal.domain.model.Atributo.DISCIPLINA
import dev.pedro.pessoal.domain.model.Atributo.ESPIRITO
import dev.pedro.pessoal.domain.model.Atributo.FORCA
import dev.pedro.pessoal.domain.model.Atributo.INTELIGENCIA

enum class Classe(val nome: String) {
    APRENDIZ("Aprendiz"),

    // Puras: um atributo claramente acima dos outros
    GUERREIRO("Guerreiro"),
    SACERDOTE("Sacerdote"),
    SABIO("Sábio"),
    SENTINELA("Sentinela"),

    // Híbridas: os dois maiores atributos estão próximos
    PALADINO("Paladino"),
    SAMURAI("Samurai"),
    ESTRATEGISTA("Estrategista"),
    MONGE("Monge"),
    TEOLOGO("Teólogo"),
    MESTRE("Mestre");

    companion object {
        /**
         * Os atributos que cada classe representa — o caminho inverso de [pura] e
         * [hibrida]. Aprendiz não aponta para nenhum, de propósito: é a escolha de
         * quem prefere não se inclinar.
         */
        fun atributosDe(classe: Classe): Set<Atributo> = when (classe) {
            APRENDIZ -> emptySet()
            GUERREIRO -> setOf(FORCA)
            SACERDOTE -> setOf(ESPIRITO)
            SABIO -> setOf(INTELIGENCIA)
            SENTINELA -> setOf(DISCIPLINA)
            PALADINO -> setOf(FORCA, ESPIRITO)
            SAMURAI -> setOf(FORCA, DISCIPLINA)
            ESTRATEGISTA -> setOf(FORCA, INTELIGENCIA)
            MONGE -> setOf(ESPIRITO, DISCIPLINA)
            TEOLOGO -> setOf(ESPIRITO, INTELIGENCIA)
            MESTRE -> setOf(INTELIGENCIA, DISCIPLINA)
        }

        fun pura(atributo: Atributo): Classe = when (atributo) {
            FORCA -> GUERREIRO
            ESPIRITO -> SACERDOTE
            INTELIGENCIA -> SABIO
            DISCIPLINA -> SENTINELA
        }

        fun hibrida(a: Atributo, b: Atributo): Classe = when (setOf(a, b)) {
            setOf(FORCA, ESPIRITO) -> PALADINO
            setOf(FORCA, DISCIPLINA) -> SAMURAI
            setOf(FORCA, INTELIGENCIA) -> ESTRATEGISTA
            setOf(ESPIRITO, DISCIPLINA) -> MONGE
            setOf(ESPIRITO, INTELIGENCIA) -> TEOLOGO
            setOf(INTELIGENCIA, DISCIPLINA) -> MESTRE
            else -> throw IllegalArgumentException("Par de atributos inválido: $a, $b")
        }
    }
}
