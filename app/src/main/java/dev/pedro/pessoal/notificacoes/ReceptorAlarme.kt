package dev.pedro.pessoal.notificacoes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.pedro.pessoal.data.AppDatabase
import dev.pedro.pessoal.domain.model.Habito
import dev.pedro.pessoal.domain.model.Status
import dev.pedro.pessoal.domain.model.textoParaDias
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Recebe o alarme, mostra a notificação certa e já reagenda para a próxima vez.
 */
class ReceptorAlarme : BroadcastReceiver() {
    override fun onReceive(contexto: Context, intencao: Intent) {
        val id = intencao.getIntExtra(Acoes.EXTRA_ID, -1)
        Notificacoes.criarCanais(contexto)

        // Acima da base ficam os lembretes que a pessoa criou. Tudo o que eles precisam veio
        // dentro da intenção, então nem a notificação nem o reagendamento abrem o banco.
        if (id >= Agendador.BASE_PESSOAL) {
            mostrarPessoal(contexto, id, intencao)
            return
        }

        val alarme = Alarme.porId(id) ?: return

        // O versículo não depende de quem está logado: é o mesmo texto para qualquer pessoa,
        // e aparecer antes do primeiro login não atrapalha ninguém. Sai pela via curta, sem
        // abrir o banco, que é o caminho mais rápido para a tela de bloqueio às 6h.
        if (alarme == Alarme.VERSICULO) {
            val (texto, referencia) = Versiculos.doDia(LocalDate.now())
            Notificacoes.versiculo(contexto, texto, referencia)
            Agendador.agendar(contexto, alarme, LocalDateTime.now().plusMinutes(1))
            return
        }

        // Os outros precisam saber de quem é o aparelho: o nome de quem chamar e quais
        // hábitos essa pessoa tem. O broadcast acabaria antes da consulta; goAsync segura.
        val pendente = goAsync()
        val aplicacao = contexto.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val status = AppDatabase.obter(aplicacao).statusDao().buscar()?.paraDominio()
                mostrarDoDia(aplicacao, alarme, status)
                Agendador.agendar(aplicacao, alarme, LocalDateTime.now().plusMinutes(1))
            } finally {
                pendente.finish()
            }
        }
    }

    /**
     * Mostra o aviso do dia, se ele for mesmo desta pessoa.
     *
     * Dois motivos para um alarme disparar e nenhuma notificação aparecer. O primeiro é o
     * dono: karatê, treino e oração são a rotina do Pedro, e quem não é administrador nem
     * enxerga esses hábitos na lista — receber o aviso de uma aula que não existe para ela
     * seria estranho. O segundo é o interruptor das configurações: desligar um hábito e
     * continuar sendo avisado dele todo dia faria o botão parecer quebrado.
     *
     * Sem [status] não há ninguém logado neste aparelho, e aí não há a quem avisar.
     */
    private fun mostrarDoDia(contexto: Context, alarme: Alarme, status: Status?) {
        if (status == null) return
        val hoje = LocalDate.now()
        val meus = Habito.doDia(hoje, status.admin, status.habitosDesligados).toSet()
        val nome = status.nome.trim().takeIf { it.isNotEmpty() }

        when (alarme) {
            Alarme.MANHA -> {
                // Os botões seguem os hábitos que sobraram: quem só tem a Bíblia vê um botão só.
                val habitos = listOf(Habito.BIBLIA, Habito.ORACAO).filter { it in meus }
                if (habitos.isEmpty()) return
                Notificacoes.lembrete(
                    contexto,
                    alarme.id,
                    titulo = if (nome != null) "Bom dia, $nome" else "Bom dia",
                    texto = textoDaManha(habitos),
                    habitos = habitos,
                )
            }

            Alarme.KARATE_AVISO -> {
                if (Habito.KARATE !in meus) return
                Notificacoes.lembrete(
                    contexto,
                    alarme.id,
                    titulo = "Karatê hoje",
                    texto = "A aula começa às 19h. Hora de se arrumar.",
                    habitos = emptyList(),
                )
            }

            Alarme.KARATE_CONFIRMA -> {
                if (Habito.KARATE !in meus) return
                Notificacoes.lembrete(
                    contexto,
                    alarme.id,
                    titulo = if (nome != null) "Como foi a aula, $nome?" else "Como foi a aula?",
                    texto = "Marque para somar os pontos do karatê.",
                    habitos = listOf(Habito.KARATE),
                )
            }

            Alarme.TREINO -> {
                val habito = listOf(Habito.TREINO_CASA, Habito.MOBILIDADE).firstOrNull { it in meus }
                    ?: return
                Notificacoes.lembrete(
                    contexto,
                    alarme.id,
                    titulo = habito.rotulo,
                    texto = if (nome != null) {
                        "$nome, abra o app para seguir as séries de hoje."
                    } else {
                        "Abra o app para seguir as séries de hoje."
                    },
                    habitos = listOf(habito),
                )
            }

            Alarme.VERSICULO -> Unit // Tratado antes, sem passar por aqui.
        }
    }

    private fun textoDaManha(habitos: List<Habito>): String = when {
        habitos.size > 1 -> "Bíblia e oração para começar o dia."
        habitos.first() == Habito.BIBLIA -> "Dez minutos de leitura para começar o dia."
        else -> "Um momento de oração para começar o dia."
    }

    private fun mostrarPessoal(contexto: Context, id: Int, intencao: Intent) {
        val titulo = intencao.getStringExtra(Agendador.EXTRA_TITULO) ?: return
        val texto = intencao.getStringExtra(Agendador.EXTRA_TEXTO).orEmpty()
        Notificacoes.lembrete(contexto, id, titulo, texto, habitos = emptyList())

        val dias = textoParaDias(intencao.getStringExtra(Agendador.EXTRA_DIAS).orEmpty())
        val minuto = intencao.getIntExtra(Agendador.EXTRA_MINUTO, -1)
        if (dias.isEmpty() || minuto < 0) return

        Agendador.reagendarPessoal(
            contexto = contexto,
            id = id,
            titulo = titulo,
            texto = texto,
            dias = dias,
            minutoDoDia = minuto,
            apartirDe = LocalDateTime.now().plusMinutes(1),
        )
    }
}
