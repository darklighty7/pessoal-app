package dev.pedro.pessoal.ui.biblia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.pedro.pessoal.data.BibliaRepositorio
import dev.pedro.pessoal.data.LivroEntity
import dev.pedro.pessoal.data.VersiculoEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tudo que a tela da Bíblia mostra de uma vez. */
data class EstadoBiblia(
    val download: BibliaRepositorio.Estado = BibliaRepositorio.Estado.Baixando,
    val livros: List<LivroEntity> = emptyList(),
    val livroAberto: LivroEntity? = null,
    val capitulo: Int = 1,
    val versiculos: List<VersiculoEntity> = emptyList(),
    /** Segundos já cronometrados nesta sessão de leitura. */
    val segundos: Int = 0,
    val cronometroLigado: Boolean = false,
)

/**
 * Cuida do download da Bíblia, da navegação por livro e capítulo, e do cronômetro.
 * O cronômetro só conta enquanto a leitura está em andamento; ao sair da tela, os
 * minutos inteiros são entregues ao repositório principal e viram XP.
 */
class BibliaViewModel(
    private val repositorio: BibliaRepositorio,
    private val aoFecharLeitura: (minutos: Int) -> Unit,
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoBiblia())
    val estado: StateFlow<EstadoBiblia> = _estado.asStateFlow()

    private var contador: Job? = null

    init {
        viewModelScope.launch {
            repositorio.baixarSeFaltar { situacao ->
                _estado.update { it.copy(download = situacao) }
            }
            if (repositorio.jaImportada()) carregarLivros()
        }
    }

    private suspend fun carregarLivros() {
        val livros = repositorio.livros()
        _estado.update { it.copy(livros = livros) }
        if (livros.isNotEmpty()) abrir(livros.first(), capitulo = 1)
    }

    fun abrir(livro: LivroEntity, capitulo: Int) {
        viewModelScope.launch {
            val versos = repositorio.capitulo(livro.numero, capitulo)
            _estado.update { it.copy(livroAberto = livro, capitulo = capitulo, versiculos = versos) }
        }
    }

    fun proximoCapitulo() {
        val e = _estado.value
        val livro = e.livroAberto ?: return
        if (e.capitulo < livro.capitulos) {
            abrir(livro, e.capitulo + 1)
        } else {
            // numero é 1-based, então o índice do próximo livro na lista é o próprio numero
            e.livros.getOrNull(livro.numero)?.let { abrir(it, 1) }
        }
    }

    fun capituloAnterior() {
        val e = _estado.value
        val livro = e.livroAberto ?: return
        if (e.capitulo > 1) {
            abrir(livro, e.capitulo - 1)
        } else {
            e.livros.getOrNull(livro.numero - 2)?.let { abrir(it, it.capitulos) }
        }
    }

    fun alternarCronometro() {
        if (_estado.value.cronometroLigado) pausar() else iniciar()
    }

    private fun iniciar() {
        if (contador != null) return
        _estado.update { it.copy(cronometroLigado = true) }
        contador = viewModelScope.launch {
            while (true) {
                delay(1_000)
                _estado.update { it.copy(segundos = it.segundos + 1) }
            }
        }
    }

    private fun pausar() {
        contador?.cancel()
        contador = null
        _estado.update { it.copy(cronometroLigado = false) }
    }

    /** Ao sair da tela: entrega os minutos inteiros lidos e guarda o resto dos segundos. */
    fun encerrarLeitura() {
        pausar()
        val minutos = _estado.value.segundos / 60
        if (minutos > 0) aoFecharLeitura(minutos)
        _estado.update { it.copy(segundos = it.segundos % 60) }
    }

    override fun onCleared() {
        contador?.cancel()
        super.onCleared()
    }

    companion object {
        fun fabrica(repositorio: BibliaRepositorio, aoFecharLeitura: (Int) -> Unit) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    BibliaViewModel(repositorio, aoFecharLeitura) as T
            }
    }
}
