package com.example.aprimortech.data.repository

import com.example.aprimortech.data.local.dao.ClienteDao
import com.example.aprimortech.data.local.entity.ClienteEntity
import com.example.aprimortech.data.remote.ClienteRemoteDataSource
import com.example.aprimortech.model.Cliente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClienteRepository(
    private val clienteDao: ClienteDao,
    private val remoteDataSource: ClienteRemoteDataSource
) {
    suspend fun getAllLocal(): List<ClienteEntity> = withContext(Dispatchers.IO) {
        clienteDao.getAll()
    }

    suspend fun getAllRemote(): List<Cliente> = withContext(Dispatchers.IO) {
        remoteDataSource.getAll()
    }

    suspend fun insertLocal(cliente: ClienteEntity) = withContext(Dispatchers.IO) {
        clienteDao.insert(cliente)
    }

    suspend fun insertRemote(cliente: Cliente) = withContext(Dispatchers.IO) {
        remoteDataSource.insert(cliente)
    }

    suspend fun updateLocal(cliente: ClienteEntity) = withContext(Dispatchers.IO) {
        clienteDao.update(cliente)
    }

    suspend fun updateRemote(cliente: Cliente) = withContext(Dispatchers.IO) {
        remoteDataSource.update(cliente)
    }

    suspend fun deleteLocal(cliente: ClienteEntity) = withContext(Dispatchers.IO) {
        clienteDao.delete(cliente)
    }

    suspend fun deleteRemote(id: String) = withContext(Dispatchers.IO) {
        remoteDataSource.delete(id)
    }

    // Exemplo de sincronização simples
    suspend fun syncLocalToRemote() = withContext(Dispatchers.IO) {
        val localClientes = clienteDao.getAll()
        localClientes.forEach { entity ->
            val cliente = Cliente(
                id = entity.id,
                nome = entity.nome,
                cnpjCpf = entity.cnpjCpf,
                contato = entity.contato,
                endereco = entity.endereco,
                cidade = entity.cidade,
                estado = entity.estado,
                telefone = entity.telefone,
                celular = entity.celular,
                latitude = entity.latitude,
                longitude = entity.longitude
            )
            remoteDataSource.insert(cliente)
        }
    }
}
