package com.example.aprimortech

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aprimortech.data.local.AppDatabase
import com.example.aprimortech.data.remote.ClienteRemoteDataSource
import com.example.aprimortech.data.repository.ClienteRepository
import com.example.aprimortech.domain.usecase.BuscarClientesUseCase
import com.example.aprimortech.domain.usecase.ExcluirClienteUseCase
import com.example.aprimortech.domain.usecase.SalvarClienteUseCase
import com.example.aprimortech.domain.usecase.SincronizarClientesUseCase

class AprimortechApplication : Application() {

    // Migração do banco de dados
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Criar nova tabela com a estrutura atualizada
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
            """)

            // Migrar dados da tabela antiga para a nova
            database.execSQL("""
                INSERT INTO clientes_new (id, nome, cnpjCpf, contatos, endereco, cidade, estado, telefone, celular, latitude, longitude)
                SELECT id, nome, cnpjCpf, '["' || contato || '"]', endereco, cidade, estado, telefone, celular, latitude, longitude
                FROM clientes
            """)

            // Remover tabela antiga
            database.execSQL("DROP TABLE clientes")

            // Renomear nova tabela
            database.execSQL("ALTER TABLE clientes_new RENAME TO clientes")
        }
    }

    // Database
    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "aprimortech_database"
        )
        .addMigrations(MIGRATION_1_2)
        .build()
    }

    // Repository
    val clienteRepository by lazy {
        ClienteRepository(
            database.clienteDao(),
            ClienteRemoteDataSource()
        )
    }

    // Use Cases
    val buscarClientesUseCase by lazy {
        BuscarClientesUseCase(clienteRepository)
    }

    val salvarClienteUseCase by lazy {
        SalvarClienteUseCase(clienteRepository)
    }

    val excluirClienteUseCase by lazy {
        ExcluirClienteUseCase(clienteRepository)
    }

    val sincronizarClientesUseCase by lazy {
        SincronizarClientesUseCase(clienteRepository)
    }
}
