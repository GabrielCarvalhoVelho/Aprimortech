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
 * Versão 11: Adicionado campo valorHoraTecnica em RelatorioEntity
 * Versão 10: Adicionados campos defeitosIdentificados, servicosRealizados e observacoesDefeitosServicos em RelatorioEntity
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
    version = 11,
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

        // Migração da versão 8 para 9: Corrige sintaxe da migração anterior (7→8)
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Sem alterações na estrutura do banco, apenas correção da sintaxe SQL
                database.execSQL("PRAGMA foreign_keys=OFF;")
                database.execSQL("BEGIN TRANSACTION;")
                database.execSQL("CREATE TABLE IF NOT EXISTS `relatorios_new` (`id` TEXT NOT NULL, `clienteId` TEXT NOT NULL, `maquinaId` TEXT NOT NULL, `data` TEXT NOT NULL, `hora` TEXT NOT NULL, `tempoGasto` INTEGER NOT NULL, `tipoServico` TEXT NOT NULL, `defeitosIdentificados` TEXT, `servicosRealizados` TEXT, `observacoesDefeitosServicos` TEXT, `tintaId` TEXT, `solventeId` TEXT, PRIMARY KEY(`id`))")
                database.execSQL("INSERT INTO relatorios_new (id, clienteId, maquinaId, data, hora, tempoGasto, tipoServico, defeitosIdentificados, servicosRealizados, observacoesDefeitosServicos, tintaId, solventeId) SELECT id, clienteId, maquinaId, data, hora, tempoGasto, tipoServico, defeitosIdentificados, servicosRealizados, observacoesDefeitosServicos, tintaId, solventeId FROM relatorios")
                database.execSQL("DROP TABLE relatorios")
                database.execSQL("ALTER TABLE relatorios_new RENAME TO relatorios")
                database.execSQL("COMMIT;")
                database.execSQL("PRAGMA foreign_keys=ON;")
                Log.d(TAG, "✅ Migração 8→9 concluída com sucesso")
            }
        }

        // Migração da versão 9 para 10: Adiciona campos defeitosIdentificados, servicosRealizados e observacoesDefeitosServicos
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Adicionar os novos campos na tabela relatorios
                database.execSQL("ALTER TABLE relatorios ADD COLUMN defeitosIdentificados TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE relatorios ADD COLUMN servicosRealizados TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE relatorios ADD COLUMN observacoesDefeitosServicos TEXT NOT NULL DEFAULT ''")
                Log.d(TAG, "✅ Migração 9→10 concluída com sucesso - Campos de defeitos e serviços adicionados")
            }
        }

        // Migração da versão 10 para 11: Adiciona campo valorHoraTecnica
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Adicionar o campo valorHoraTecnica na tabela relatorios
                database.execSQL("ALTER TABLE relatorios ADD COLUMN valorHoraTecnica REAL")
                Log.d(TAG, "✅ Migração 10→11 concluída com sucesso - Campo valorHoraTecnica adicionado")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aprimortech.db"
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                    .fallbackToDestructiveMigration()
                    .build()

                Log.d(TAG, "✅ Banco de dados inicializado")
                INSTANCE = instance
                instance
            }
        }
    }
}
