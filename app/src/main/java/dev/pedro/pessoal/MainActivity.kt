package dev.pedro.pessoal

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.pedro.pessoal.data.AppDatabase
import dev.pedro.pessoal.data.BibliaRepositorio
import dev.pedro.pessoal.data.PreferenciasLeitura
import dev.pedro.pessoal.data.Repositorio
import dev.pedro.pessoal.notificacoes.Agendador
import dev.pedro.pessoal.notificacoes.Notificacoes
import dev.pedro.pessoal.ui.AppViewModel
import dev.pedro.pessoal.ui.biblia.BibliaViewModel
import dev.pedro.pessoal.ui.navegacao.AppNavegacao
import dev.pedro.pessoal.ui.theme.PessoalTema

class MainActivity : ComponentActivity() {

    private val pedirNotificacoes = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* Se recusar, o app continua funcionando, só sem os avisos. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        val banco = AppDatabase.obter(this)
        val repositorio = Repositorio(banco.statusDao(), banco.registroDao())
        val biblia = BibliaRepositorio(banco.bibliaDao())
        val preferencias = PreferenciasLeitura(this)

        Notificacoes.criarCanais(this)
        pedirPermissaoDeNotificacao()
        Agendador.agendarTodos(this)

        setContent {
            PessoalTema {
                val app: AppViewModel = viewModel(factory = AppViewModel.fabrica(repositorio))
                val leitor: BibliaViewModel = viewModel(
                    factory = BibliaViewModel.fabrica(biblia, preferencias, app::registrarLeitura),
                )
                // Se o app ficou aberto durante a virada da meia-noite, o dia troca aqui.
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { app.revisarData() }
                AppNavegacao(app, leitor)
            }
        }
    }

    private fun pedirPermissaoDeNotificacao() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val concedida = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!concedida) pedirNotificacoes.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
