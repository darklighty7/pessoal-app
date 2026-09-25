package dev.pedro.pessoal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StatusEntity::class,
        RegistroEntity::class,
        DiaFechadoEntity::class,
        ProgressoExercicioEntity::class,
        LivroEntity::class,
        VersiculoEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun statusDao(): StatusDao
    abstract fun registroDao(): RegistroDao
    abstract fun bibliaDao(): BibliaDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun obter(contexto: Context): AppDatabase = instancia ?: synchronized(this) {
            instancia ?: Room
                .databaseBuilder(contexto.applicationContext, AppDatabase::class.java, "pessoal.db")
                .build()
                .also { instancia = it }
        }
    }
}
