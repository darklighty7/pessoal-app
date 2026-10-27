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
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun statusDao(): StatusDao
    abstract fun registroDao(): RegistroDao
    abstract fun bibliaDao(): BibliaDao

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

        @Volatile private var instancia: AppDatabase? = null

        fun obter(contexto: Context): AppDatabase = instancia ?: synchronized(this) {
            instancia ?: Room
                .databaseBuilder(contexto.applicationContext, AppDatabase::class.java, "pessoal.db")
                .addMigrations(MIGRACAO_1_2, MIGRACAO_2_3)
                .build()
                .also { instancia = it }
        }
    }
}
