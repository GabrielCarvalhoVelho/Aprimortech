package com.example.aprimortech

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aprimortech.data.local.AppDatabase
import com.example.aprimortech.data.remote.ClienteRemoteDataSource
import com.example.aprimortech.data.remote.MaquinaRemoteDataSource
import com.example.aprimortech.data.remote.PecaRemoteDataSource
import com.example.aprimortech.data.repository.ClienteRepository
import com.example.aprimortech.data.repository.MaquinaRepository
import com.example.aprimortech.data.repository.PecaRepository
import com.example.aprimortech.domain.usecase.BuscarClientesUseCase
import com.example.aprimortech.domain.usecase.ExcluirClienteUseCase
import com.example.aprimortech.domain.usecase.SalvarClienteUseCase
import com.example.aprimortech.domain.usecase.SincronizarClientesUseCase
import com.example.aprimortech.domain.usecase.BuscarMaquinasUseCase
import com.example.aprimortech.domain.usecase.SalvarMaquinaUseCase
import com.example.aprimortech.domain.usecase.ExcluirMaquinaUseCase
import com.example.aprimortech.domain.usecase.SincronizarMaquinasUseCase
import com.example.aprimortech.domain.usecase.BuscarPecasUseCase
import com.example.aprimortech.domain.usecase.SalvarPecaUseCase
import com.example.aprimortech.domain.usecase.ExcluirPecaUseCase
import com.example.aprimortech.domain.usecase.SincronizarPecasUseCase
import com.google.firebase.firestore.FirebaseFirestore

class AprimortechApplication : Application() {

    // Migração 1 -> 2 (clientes contatos vira lista)
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE clientes_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    nome TEXT NOT NULL,
                    cnpjCpf TEXT NOT NULL,
                    contatos TEXT NOT NULL,
                    endereco TEXT NOT NULL,
                    cidade TEXT NOT NULL,
                    estado TEXT NOT NULL,
                    telefone TEXT NOT NULL,
                    celular TEXT NOT NULL,
                    latitude REAL,
                    longitude REAL
                )
            """.trimIndent())

            database.execSQL("""
                INSERT INTO clientes_new (id, nome, cnpjCpf, contatos, endereco, cidade, estado, telefone, celular, latitude, longitude)
                SELECT id, nome, cnpjCpf, '["' || contato || '"]', endereco, cidade, estado, telefone, celular, latitude, longitude
                FROM clientes
            """.trimIndent())

            database.execSQL("DROP TABLE clientes")
            database.execSQL("ALTER TABLE clientes_new RENAME TO clientes")
        }
    }

    // Migração 2 -> 3 (adição da tabela maquinas)
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS maquinas (
                    id TEXT PRIMARY KEY NOT NULL,
                    fabricante TEXT NOT NULL,
                    codigoConfiguracao TEXT NOT NULL,
                    numeroSerie TEXT NOT NULL,
                    modelo TEXT NOT NULL,
                    codigoTinta TEXT NOT NULL,
                    anoFabricacao TEXT NOT NULL,
                    identificacao TEXT NOT NULL,
                    codigoSolvente TEXT NOT NULL,
                    dataProximaPreventiva TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    // Migração 3 -> 4 (atualizar estrutura da tabela maquinas)
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Criar nova tabela com estrutura atualizada
            database.execSQL(
                """
                CREATE TABLE maquinas_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    clienteId TEXT NOT NULL,
                    nomeMaquina TEXT NOT NULL,
                    fabricante TEXT NOT NULL,
                    numeroSerie TEXT NOT NULL,
                    modelo TEXT NOT NULL,
                    identificacao TEXT NOT NULL,
                    anoFabricacao TEXT NOT NULL,
                    codigoTinta TEXT NOT NULL,
                    codigoSolvente TEXT NOT NULL,
                    dataProximaPreventiva TEXT NOT NULL,
                    codigoConfiguracao TEXT NOT NULL DEFAULT ''
                )
                """.trimIndent()
            )

            // Migrar dados existentes (se houver) com valores padrão para novos campos
            database.execSQL(
                """
                INSERT INTO maquinas_new (
                    id, clienteId, nomeMaquina, fabricante, numeroSerie, modelo, 
                    identificacao, anoFabricacao, codigoTinta, codigoSolvente, 
                    dataProximaPreventiva, codigoConfiguracao
                )
                SELECT 
                    id, 
                    '' as clienteId,
                    modelo as nomeMaquina,
                    fabricante, 
                    numeroSerie, 
                    modelo, 
                    identificacao, 
                    anoFabricacao, 
                    codigoTinta, 
                    codigoSolvente, 
                    dataProximaPreventiva,
                    codigoConfiguracao
                FROM maquinas
                """.trimIndent()
            )

            // Remover tabela antiga e renomear nova
            database.execSQL("DROP TABLE maquinas")
            database.execSQL("ALTER TABLE maquinas_new RENAME TO maquinas")
        }
    }

    // Migração 4 -> 5 (adição da tabela pecas)
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS pecas (
                    id TEXT PRIMARY KEY NOT NULL,
                    nome TEXT NOT NULL,
                    codigo TEXT NOT NULL,
                    descricao TEXT NOT NULL,
                    fabricante TEXT NOT NULL,
                    categoria TEXT NOT NULL,
                    preco REAL NOT NULL,
                    estoque INTEGER NOT NULL,
                    unidadeMedida TEXT NOT NULL,
                    observacoes TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    // Database
    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "aprimortech_database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .fallbackToDestructiveMigration()
            .build()
    }

    // Repositórios
    val clienteRepository by lazy {
        ClienteRepository(
            database.clienteDao(),
            ClienteRemoteDataSource()
        )
    }

    val maquinaRepository by lazy {
        MaquinaRepository(
            database.maquinaDao(),
            MaquinaRemoteDataSource()
        )
    }

    val pecaRepository by lazy {
        PecaRepository(
            database.pecaDao(),
            PecaRemoteDataSource()
        )
    }

    // Use Cases Clientes
    val buscarClientesUseCase by lazy { BuscarClientesUseCase(clienteRepository) }
    val salvarClienteUseCase by lazy { SalvarClienteUseCase(clienteRepository) }
    val excluirClienteUseCase by lazy { ExcluirClienteUseCase(clienteRepository) }
    val sincronizarClientesUseCase by lazy { SincronizarClientesUseCase(clienteRepository) }

    // Use Cases Máquinas
    val buscarMaquinasUseCase by lazy { BuscarMaquinasUseCase(maquinaRepository) }
    val salvarMaquinaUseCase by lazy { SalvarMaquinaUseCase(maquinaRepository) }
    val excluirMaquinaUseCase by lazy { ExcluirMaquinaUseCase(maquinaRepository) }
    val sincronizarMaquinasUseCase by lazy { SincronizarMaquinasUseCase(maquinaRepository) }

    // Use Cases Peças
    val buscarPecasUseCase by lazy { BuscarPecasUseCase(pecaRepository) }
    val salvarPecaUseCase by lazy { SalvarPecaUseCase(pecaRepository) }
    val excluirPecaUseCase by lazy { ExcluirPecaUseCase(pecaRepository) }
    val sincronizarPecasUseCase by lazy { SincronizarPecasUseCase(pecaRepository) }

    override fun onCreate() {
        super.onCreate()

        android.util.Log.d("AprimortechApp", "=== INICIALIZANDO APLICAÇÃO ===")

        // Habilita logging detalhado do Firestore para diagnóstico
        try {
            FirebaseFirestore.setLoggingEnabled(true)
            android.util.Log.d("AprimortechApp", "Firestore logging habilitado com sucesso")

            // Teste de conectividade do Firebase
            val firestore = FirebaseFirestore.getInstance()
            android.util.Log.d("AprimortechApp", "Instância do Firestore obtida: $firestore")

            // Verifica se o Firebase está configurado
            try {
                val app = com.google.firebase.FirebaseApp.getInstance()
                android.util.Log.d("AprimortechApp", "Firebase App configurado: ${app.name}")
                android.util.Log.d("AprimortechApp", "Firebase App Options: ${app.options}")
            } catch (e: Exception) {
                android.util.Log.e("AprimortechApp", "ERRO: Firebase não está configurado corretamente", e)
            }

        } catch (e: Exception) {
            android.util.Log.e("AprimortechApp", "ERRO: Não foi possível configurar Firestore", e)
        }

        android.util.Log.d("AprimortechApp", "=== APLICAÇÃO INICIALIZADA ===")
    }
}
