package dev.pedro.pessoal.notificacoes

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.pedro.pessoal.MainActivity
import dev.pedro.pessoal.R
import dev.pedro.pessoal.domain.model.Habito

object Canais {
    const val LEMBRETES = "lembretes"
    const val VERSICULO = "versiculo"
}

object Acoes {
    const val MARCAR = "dev.pedro.pessoal.MARCAR"
    const val EXTRA_HABITO = "habito"
    const val EXTRA_ID = "id"
}

/**
 * Monta e mostra as notificações. As de lembrete trazem botões que marcam o hábito
 * direto da tela de bloqueio; a do versículo fica fixa e sem som.
 */
object Notificacoes {

    const val ID_MANHA = 1
    const val ID_KARATE_AVISO = 2
    const val ID_KARATE_CONFIRMA = 3
    const val ID_TREINO = 4
    const val ID_VERSICULO = 10

    fun criarCanais(contexto: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val gerente = contexto.getSystemService(NotificationManager::class.java)
        gerente.createNotificationChannel(
            NotificationChannel(Canais.LEMBRETES, "Lembretes", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Avisos dos hábitos do dia"
            },
        )
        gerente.createNotificationChannel(
            NotificationChannel(Canais.VERSICULO, "Versículo do dia", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Mensagem fixa na tela de bloqueio"
                setShowBadge(false)
            },
        )
    }

    /** Lembrete de um ou mais hábitos, com um botão de concluir para cada um. */
    fun lembrete(contexto: Context, id: Int, titulo: String, texto: String, habitos: List<Habito>) {
        val construtor = NotificationCompat.Builder(contexto, Canais.LEMBRETES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(abrirApp(contexto, id))

        habitos.forEach { habito ->
            construtor.addAction(
                R.drawable.ic_launcher_foreground,
                habito.rotulo,
                marcarHabito(contexto, habito, id),
            )
        }
        mostrar(contexto, id, construtor.build())
    }

    /** Versículo fixo na tela de bloqueio: sem som e sem sumir ao arrastar. */
    fun versiculo(contexto: Context, texto: String, referencia: String) {
        val notificacao = NotificationCompat.Builder(contexto, Canais.VERSICULO)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(referencia)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(abrirApp(contexto, ID_VERSICULO))
            .build()
        mostrar(contexto, ID_VERSICULO, notificacao)
    }

    fun fechar(contexto: Context, id: Int) {
        NotificationManagerCompat.from(contexto).cancel(id)
    }

    private fun mostrar(contexto: Context, id: Int, notificacao: Notification) {
        // Em Android 13+ a permissão pode ter sido negada; o sistema recusa com uma
        // exceção de segurança, que não deve derrubar o alarme.
        try {
            NotificationManagerCompat.from(contexto).notify(id, notificacao)
        } catch (_: SecurityException) {
        }
    }

    private fun abrirApp(contexto: Context, id: Int): PendingIntent = PendingIntent.getActivity(
        contexto,
        id,
        Intent(contexto, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun marcarHabito(contexto: Context, habito: Habito, idNotificacao: Int): PendingIntent {
        val intencao = Intent(contexto, ReceptorAcao::class.java).apply {
            action = Acoes.MARCAR
            putExtra(Acoes.EXTRA_HABITO, habito.name)
            putExtra(Acoes.EXTRA_ID, idNotificacao)
        }
        return PendingIntent.getBroadcast(
            contexto,
            habito.ordinal * 100 + idNotificacao,
            intencao,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
