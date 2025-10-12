package com.example.aprimortech.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
 * Versão 9: Correção da migração 7→8 (sintaxe SQL corrigida)
 * Versão 8: Adicionados campos tintaId e solventeId em RelatorioEntity
 * Versão 7: Removidos campos codigoTinta e codigoSolvente da tabela MaquinaEntity
 * Versão 6: Adicionados campos codigoTinta e codigoSolvente em RelatorioEntity
 */
@Database(
    entities = [
        ClienteEntity::class,
        MaquinaEntity::class,
        PecaEntity::class,
        RelatorioEntity::class
    ],
    version = 9,
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
        private const val TAG = "AppDatabase"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migração da versão 6 para 7: Remove codigoTinta e codigoSolvente da tabela maquinas
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Criar tabela temporária sem os campos codigoTinta e codigoSolvente
                database.execSQL("""
                    CREATE TABLE maquinas_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        clienteId TEXT NOT NULL,
                        fabricante TEXT NOT NULL,
                        numeroSerie TEXT NOT NULL,
                        modelo TEXT NOT NULL,
                        identificacao TEXT NOT NULL,
                        anoFabricacao TEXT NOT NULL,
                        dataProximaPreventiva TEXT NOT NULL,
                        codigoConfiguracao TEXT NOT NULL,
                        horasProximaPreventiva TEXT NOT NULL,
                        pendenteSincronizacao INTEGER NOT NULL,
                        timestampAtualizacao INTEGER NOT NULL
                    )
                """.trimIndent())

                // Copiar dados da tabela antiga para a nova (sem os campos removidos)
                database.execSQL("""
                    INSERT INTO maquinas_new (
                        id, clienteId, fabricante, numeroSerie, modelo, identificacao,
                        anoFabricacao, dataProximaPreventiva, codigoConfiguracao,
                        horasProximaPreventiva, pendenteSincronizacao, timestampAtualizacao
                    )
                    SELECT 
                        id, clienteId, fabricante, numeroSerie, modelo, identificacao,
                        anoFabricacao, dataProximaPreventiva, codigoConfiguracao,
                        horasProximaPreventiva, pendenteSincronizacao, timestampAtualizacao
                    FROM maquinas
                """.trimIndent())

                // Remover tabela antiga
                database.execSQL("DROP TABLE maquinas")

                // Renomear tabela nova
                database.execSQL("ALTER TABLE maquinas_new RENAME TO maquinas")
            }
        }

        // Migração da versão 7 para 8: Adiciona tintaId e solventeId em RelatorioEntity
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Adicionar novos campos tintaId e solventeId na tabela relatorios
                // CORRIGIDO: SQL não permite múltiplos ADD COLUMN separados por vírgula
                database.execSQL("ALTER TABLE relatorios ADD COLUMN tintaId TEXT")
                database.execSQL("ALTER TABLE relatorios ADD COLUMN solventeId TEXT")
                Log.d(TAG, "✅ Migração 7→8 concluída com sucesso")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aprimortech.db"
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration()
                    .build()

                Log.d(TAG, "✅ Banco de dados inicializado")
                INSTANCE = instance
                instance
            }
        }
    }
}
