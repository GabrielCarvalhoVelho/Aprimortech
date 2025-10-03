package com.example.aprimortech.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.aprimortech.data.local.AppDatabase
import com.example.aprimortech.data.local.entity.ClienteEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ClienteDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: ClienteDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = db.clienteDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndGetCliente() = runBlocking {
        val cliente = ClienteEntity(
            id = "1",
            nome = "Cliente Teste",
            cnpjCpf = "12345678900",
            contato = "Contato Teste",
            endereco = "Rua Teste, 123",
            cidade = "Cidade Teste",
            estado = "Estado Teste",
            telefone = "1111-1111",
            celular = "99999-9999",
            latitude = null,
            longitude = null
        )
        dao.insert(cliente)
        val result = dao.getById("1")
        assertEquals(cliente, result)
    }

    @Test
    fun updateCliente() = runBlocking {
        val cliente = ClienteEntity("2", "A", "", "", "", "", "", "", "", null, null)
        dao.insert(cliente)
        val updated = cliente.copy(nome = "Novo Nome")
        dao.update(updated)
        val result = dao.getById("2")
        assertEquals("Novo Nome", result?.nome)
    }

    @Test
    fun deleteCliente() = runBlocking {
        val cliente = ClienteEntity("3", "B", "", "", "", "", "", "", "", null, null)
        dao.insert(cliente)
        dao.delete(cliente)
        val result = dao.getById("3")
        assertEquals(null, result)
    }
}
