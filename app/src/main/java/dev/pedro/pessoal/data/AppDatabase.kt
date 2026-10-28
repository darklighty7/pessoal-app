package dev.pedro.pessoal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        StatusEntity::class,
        RegistroEntity::class,
        DiaFechadoEntity::class,
        ProgressoExercicioEntity::class,
        LivroEntity::class,
        VersiculoEntity::class,
        GrifoEntity::class,
        LembreteEntity::class,
        ExercicioEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun statusDao(): StatusDao
    abstract fun registroDao(): RegistroDao
    abstract fun bibliaDao(): BibliaDao
    abstract fun itensDao(): ItensDao

    companion object {
        /** v1.3.0: cria a tabela de grifos sem tocar em nenhum dado existente. */
        private val MIGRACAO_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `grifo` (" +
                        "`livro` INTEGER NOT NULL, `capitulo` INTEGER NOT NULL, `numero` INTEGER NOT NULL, " +
                        "`cor` INTEGER NOT NULL, `criadoEm` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`livro`, `capitulo`, `numero`))",
                )
            }
        }

        /** v1.5.0: guarda a classe escolhida na criação do personagem. */
        private val MIGRACAO_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `status` ADD COLUMN `classeEscolhida` TEXT")
            }
        }

        /**
         * v1.12.0: as tabelas dos lembretes e exercícios criados pela pessoa, mais a cópia
         * local do `admin` — o fechamento do dia precisa saber quais hábitos fixos valem, e
         * ele não fala com a nuvem.
         */
        private val MIGRACAO_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `lembrete` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`usuarioId` TEXT NOT NULL, `titulo` TEXT NOT NULL, `descricao` TEXT NOT NULL, " +
                        "`minutoDoDia` INTEGER NOT NULL, `dias` TEXT NOT NULL, `atributo` TEXT NOT NULL, " +
                        "`criadoEm` INTEGER NOT NULL)",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `exercicio` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`usuarioId` TEXT NOT NULL, `nome` TEXT NOT NULL, `series` INTEGER NOT NULL, " +
                        "`repeticoes` INTEGER NOT NULL, `porTempo` INTEGER NOT NULL, `dias` TEXT NOT NULL, " +
                        "`criadoEm` INTEGER NOT NULL)",
                )
                db.execSQL("ALTER TABLE `status` ADD COLUMN `admin` INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v1.13.0: os hábitos fixos que o dono desligou nas configurações. */
        private val MIGRACAO_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `status` ADD COLUMN `habitosDesligados` TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile private var instancia: AppDatabase? = null

        fun obter(contexto: Context): AppDatabase = instancia ?: synchronized(this) {
            instancia ?: Room
                .databaseBuilder(contexto.applicationContext, AppDatabase::class.java, "pessoal.db")
                .addMigrations(MIGRACAO_1_2, MIGRACAO_2_3, MIGRACAO_3_4, MIGRACAO_4_5)
                .build()
                .also { instancia = it }
        }
    }
}
