package dev.pedro.pessoal.ui.status

import dev.pedro.pessoal.domain.engine.MotorPontuacao
import dev.pedro.pessoal.domain.model.Atributo
import dev.pedro.pessoal.domain.model.ResultadoHabito
import dev.pedro.pessoal.domain.model.Status
import dev.pedro.pessoal.domain.model.TipoTarefa

/**
 * Dados simulados usados só no Preview do Android Studio.
 * Simula 4 dias passando pelo motor real: Bíblia (15 min) e oração todos os dias,
 * treino perdido no dia 1 e feito no dia 3.
 */
object StatusExemplo {
    fun gerar(): Status {
        var s = Status()
        for (dia in 1..4) {
            s = MotorPontuacao.aplicar(s, MotorPontuacao.recompensaLeitura(15))
            s = MotorPontuacao.aplicar(s, TipoTarefa.ORACAO.recompensa)
            val habitos = mutableListOf(ResultadoHabito(Atributo.ESPIRITO, cumprido = true, falhasSeguidas = 0, sequencia = dia))
            when (dia) {
                1 -> habitos += ResultadoHabito(Atributo.FORCA, cumprido = false, falhasSeguidas = 1, sequencia = 0)
                3 -> {
                    s = MotorPontuacao.aplicar(s, TipoTarefa.TREINO_CASA.recompensa)
                    habitos += ResultadoHabito(Atributo.FORCA, cumprido = true, falhasSeguidas = 0, sequencia = 1)
                }
            }
            s = MotorPontuacao.fecharDia(s, habitos)
        }
        return s
    }
}
