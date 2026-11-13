package dev.pedro.pessoal.data

import android.content.Context
import com.youversion.platform.core.YouVersionPlatformConfiguration
import com.youversion.platform.core.bibles.domain.BibleReference
import dev.pedro.pessoal.BuildConfig

/**
 * A Bíblia do app: a Nova Versão Internacional (NVI) em português, servida pela YouVersion
 * Platform sob a licença Fast-track da Biblica.
 *
 * O texto nunca é guardado pelo app. Ele vem do SDK da YouVersion, que mantém o próprio cache
 * pelo tempo que o servidor permite. O app guarda só a estrutura (livros, capítulos e quantos
 * versículos cada um tem), que não é texto bíblico.
 *
 * Toda tela que mostra texto da NVI precisa mostrar também o crédito com link para a Biblica.
 */
object Escrituras {
    /** Nova Versão Internacional - Português, edição atual (a única que a Biblica licencia). */
    const val VERSAO_NVI = 129

    /** Crédito curto, mostrado junto de todo texto bíblico, sempre clicável para [LINK_BIBLICA]. */
    const val CREDITO = "NVI® · Biblica"

    const val LINK_BIBLICA = "https://www.biblica.com"

    @Volatile private var configurado = false

    /** Liga o SDK uma vez por processo. Seguro chamar de qualquer lugar e mais de uma vez. */
    fun configurar(contexto: Context) {
        if (configurado) return
        synchronized(this) {
            if (configurado) return
            YouVersionPlatformConfiguration.configure(
                context = contexto.applicationContext,
                appKey = BuildConfig.YVP_APP_KEY.ifBlank { null },
                permittedVersionIds = setOf(VERSAO_NVI),
                // O app não usa a conta YouVersion: os grifos ficam no aparelho.
                isSignInEnabled = false,
                appName = "Forja",
            )
            configurado = true
        }
    }

    /** A chave da YouVersion veio no build? Sem ela o texto não carrega. */
    val temChave: Boolean get() = BuildConfig.YVP_APP_KEY.isNotBlank()

    fun versiculo(livro: LivroEntity, capitulo: Int, numero: Int): BibleReference =
        BibleReference(VERSAO_NVI, livro.usfm, capitulo, numero)

    fun capitulo(livro: LivroEntity, capitulo: Int): BibleReference =
        BibleReference(VERSAO_NVI, livro.usfm, capitulo)
}
