package dev.pedro.pessoal.domain.engine

import dev.pedro.pessoal.domain.model.Atributo
import dev.pedro.pessoal.domain.model.Classe
import dev.pedro.pessoal.domain.model.Recompensa
import dev.pedro.pessoal.domain.model.ResultadoHabito
import dev.pedro.pessoal.domain.model.Status
import dev.pedro.pessoal.domain.model.Titulo

/**
 * Regras de evolução do personagem. Funções puras: recebem um Status e devolvem outro,
 * sem banco de dados nem Android, o que permite testar tudo com JUnit.
 */
object MotorPontuacao {

    /** Soma (ou subtrai) XP. Sobe quantos níveis couberem; ao perder, para no zero do nível. */
    fun adicionarXp(status: Status, delta: Int): Status {
        var nivel = status.nivel
        var xp = status.xp + delta
        if (xp <= 0) return status.copy(xp = 0)
        while (xp >= Regras.xpParaProximoNivel(nivel)) {
            xp -= Regras.xpParaProximoNivel(nivel)
            nivel++
        }
        return status.copy(nivel = nivel, xp = xp)
    }

    /** Altera um atributo; nunca fica abaixo do valor inicial (zero). */
    fun ajustarAtributo(status: Status, atributo: Atributo, delta: Int): Status {
        val novo = maxOf(Regras.VALOR_INICIAL_ATRIBUTO, status.valor(atributo) + delta)
        return status.copy(atributos = status.atributos + (atributo to novo))
    }

    fun aplicar(status: Status, recompensa: Recompensa): Status {
        var s = adicionarXp(status, recompensa.xp)
        for ((atributo, ganho) in recompensa.ganhos) s = ajustarAtributo(s, atributo, ganho)
        return s
    }

    /**
     * Leitura por tempo: abaixo da meta não rende; na meta rende o valor cheio e
     * +1 XP por minuto extra, até o teto diário. [minutos] é o total lido no dia.
     * O ganho de Espírito é fixo ao atingir a meta; o de Inteligência começa menor
     * exatamente nos 10 minutos e sobe a partir do 11º minuto.
     */
    fun recompensaLeitura(minutos: Int): Recompensa {
        if (minutos < Regras.META_LEITURA_MIN) return Recompensa(xp = 0)
        val xp = minOf(Regras.XP_MAX_LEITURA_DIA, Regras.XP_META_LEITURA + (minutos - Regras.META_LEITURA_MIN))
        val inteligencia = if (minutos > Regras.META_LEITURA_MIN) {
            Regras.INTELIGENCIA_ACIMA_META_LEITURA
        } else {
            Regras.INTELIGENCIA_META_LEITURA
        }
        return Recompensa(
            xp = xp,
            ganhos = mapOf(
                Atributo.ESPIRITO to Regras.ESPIRITO_META_LEITURA,
                Atributo.INTELIGENCIA to inteligencia,
            ),
        )
    }

    /**
     * Roda uma vez por dia (à meia-noite). [habitos] traz só os hábitos obrigatórios
     * programados para o dia que está sendo fechado.
     */
    fun fecharDia(status: Status, habitos: List<ResultadoHabito>): Status {
        var s = status

        // Perdas por falhas seguidas
        for (h in habitos.filter { !it.cumprido }) {
            when {
                h.falhasSeguidas >= 3 -> s = ajustarAtributo(s, h.atributo, -Regras.PERDA_TRES_OU_MAIS_FALHAS)
                h.falhasSeguidas == 2 -> s = ajustarAtributo(s, h.atributo, -Regras.PERDA_DUAS_FALHAS)
            }
        }
        if (habitos.any { !it.cumprido && it.falhasSeguidas >= 3 }) {
            s = ajustarAtributo(s, Atributo.DISCIPLINA, -Regras.PERDA_DISCIPLINA_SEQUENCIA)
        }

        // Bônus de sequência e títulos
        for (h in habitos.filter { it.cumprido }) {
            if (h.sequencia == 7) {
                s = ajustarAtributo(s, h.atributo, Regras.BONUS_SEQUENCIA_7)
                s = promoverTitulo(s, Titulo.GUERREIRO_CONSTANTE)
            }
            if (h.sequencia >= 30 && h.sequencia % 30 == 0) {
                s = ajustarAtributo(s, h.atributo, Regras.BONUS_SEQUENCIA_30)
                s = promoverTitulo(s, Titulo.INABALAVEL)
            }
        }

        // Dia perfeito
        if (habitos.isNotEmpty() && habitos.all { it.cumprido }) {
            s = adicionarXp(s, Regras.BONUS_DIA_PERFEITO_XP)
            s = ajustarAtributo(s, Atributo.DISCIPLINA, Regras.BONUS_DIA_PERFEITO_DISCIPLINA)
        }

        // Desgaste constante
        s = adicionarXp(s, -Regras.DESGASTE_DIARIO_XP)

        return atualizarClasse(s)
    }

    /**
     * Com que atributos um personagem recém-criado começa, conforme a classe escolhida.
     *
     * O total é o mesmo para qualquer escolha das que a tela de criação oferece: muda só
     * como ele se distribui. Aprendiz espalha pelos quatro — escolher "sem inclinação"
     * não pode custar pontos, senão vira a opção dos distraídos.
     *
     * As híbridas recebem o bônus cheio em cada um dos dois atributos, e não metade em
     * cada: pela regra de [calcularClasse], meio bônus deixaria a diferença dentro da
     * margem e o motor leria Aprendiz. Elas não aparecem na criação — só se chega nelas
     * jogando —, mas a conta precisa fechar se um dia aparecerem.
     */
    fun atributosIniciais(escolhida: Classe): Map<Atributo, Int> {
        val alvos = Classe.atributosDe(escolhida)
        if (alvos.isEmpty()) {
            val fatia = Regras.BONUS_CLASSE_INICIAL / Atributo.entries.size
            return Atributo.entries.associateWith { Regras.VALOR_INICIAL_ATRIBUTO + fatia }
        }
        return Atributo.entries.associateWith { atributo ->
            Regras.VALOR_INICIAL_ATRIBUTO + if (atributo in alvos) Regras.BONUS_CLASSE_INICIAL else 0
        }
    }

    /** Classe que os atributos indicam hoje, sem considerar a espera de 3 dias. */
    fun calcularClasse(atributos: Map<Atributo, Int>): Classe {
        val ordenados = atributos.entries.sortedByDescending { it.value }
        val maior = ordenados.first()
        val segundo = ordenados[1]
        val menor = ordenados.last()
        return when {
            maior.value - menor.value <= Regras.MARGEM_CLASSE -> Classe.APRENDIZ
            maior.value - segundo.value <= Regras.MARGEM_CLASSE -> Classe.hibrida(maior.key, segundo.key)
            else -> Classe.pura(maior.key)
        }
    }

    /** A classe só muda depois que a nova se mantém por N dias seguidos. */
    fun atualizarClasse(status: Status): Status {
        val calculada = calcularClasse(status.atributos)
        // Começo de jogo: com os atributos ainda parecidos o cálculo devolve Aprendiz, e sem
        // isto a classe escolhida na criação sumiria em três dias. A escolha segura o lugar
        // até os atributos apontarem uma classe de verdade.
        val indicada = if (calculada == Classe.APRENDIZ && status.classeEscolhida != null) {
            status.classeEscolhida
        } else {
            calculada
        }
        if (indicada == status.classe) return status.copy(classeCandidata = null, diasCandidata = 0)
        val dias = if (indicada == status.classeCandidata) status.diasCandidata + 1 else 1
        return if (dias >= Regras.DIAS_PARA_TROCAR_CLASSE) {
            status.copy(classe = indicada, classeCandidata = null, diasCandidata = 0)
        } else {
            status.copy(classeCandidata = indicada, diasCandidata = dias)
        }
    }

    private fun promoverTitulo(status: Status, titulo: Titulo): Status =
        if (titulo.ordinal > status.titulo.ordinal) status.copy(titulo = titulo) else status
}
