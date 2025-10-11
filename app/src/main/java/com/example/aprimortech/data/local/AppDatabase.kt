package com.example.aprimortech.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.aprimortech.data.local.converters.ContatoClienteListConverter
import com.example.aprimortech.data.local.converters.StringListConverter
import com.example.aprimortech.data.local.dao.ClienteDao
import com.example.aprimortech.data.local.dao.MaquinaDao
import com.example.aprimortech.data.local.dao.PecaDao
import com.example.aprimortech.data.local.dao.RelatorioDao
import com.example.aprimortech.data.local.entity.ClienteEntity
import com.example.aprimortech.data.local.entity.MaquinaEntity
import com.example.aprimortech.data.local.entity.PecaEntity
import com.example.aprimortech.data.local.entity.RelatorioEntity

/**
 * Banco de dados Room local para operação offline
 * Versão 1: Implementação inicial com suporte a clientes, máquinas, peças e relatórios
 */
@Database(
    entities = [
        ClienteEntity::class,
        MaquinaEntity::class,
        PecaEntity::class,
        RelatorioEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    ContatoClienteListConverter::class,
    StringListConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun maquinaDao(): MaquinaDao
    abstract fun pecaDao(): PecaDao
    abstract fun relatorioDao(): RelatorioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aprimortech.db"
                )
                    .fallbackToDestructiveMigration() // Em produção, use migrations apropriadas
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

