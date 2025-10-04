package com.example.aprimortech.data.repository

import android.util.Log
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
    companion object {
        private const val TAG = "ClienteRepository"
    }

    suspend fun getAllLocal(): List<ClienteEntity> = withContext(Dispatchers.IO) {
        clienteDao.getAll()
    }

    suspend fun getAllRemote(): List<Cliente> = withContext(Dispatchers.IO) {
        remoteDataSource.getAll()
    }

    suspend fun insertLocal(cliente: ClienteEntity) = withContext(Dispatchers.IO) {
        clienteDao.insert(cliente)
    }

    suspend fun insertRemote(cliente: Cliente): Boolean = withContext(Dispatchers.IO) {
        remoteDataSource.insert(cliente)
    }

    suspend fun updateLocal(cliente: ClienteEntity) = withContext(Dispatchers.IO) {
        clienteDao.update(cliente)
    }

    suspend fun updateRemote(cliente: Cliente): Boolean = withContext(Dispatchers.IO) {
        remoteDataSource.update(cliente)
    }

    suspend fun deleteLocal(cliente: ClienteEntity) = withContext(Dispatchers.IO) {
        clienteDao.delete(cliente)
    }

    suspend fun deleteRemote(id: String): Boolean = withContext(Dispatchers.IO) {
        remoteDataSource.delete(id)
    }

    // Método melhorado para salvar tanto local quanto remoto
    suspend fun salvarCliente(cliente: ClienteEntity): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Salvando cliente: ${cliente.nome}")

            // Primeiro salva localmente
            clienteDao.insert(cliente)
            Log.d(TAG, "Cliente salvo localmente com sucesso")

            // Depois tenta salvar no Firestore
            val clienteDominio = Cliente(
                id = cliente.id,
                nome = cliente.nome,
                cnpjCpf = cliente.cnpjCpf,
                contatos = cliente.contatos, // Corrigido para contatos
                endereco = cliente.endereco,
                cidade = cliente.cidade,
                estado = cliente.estado,
                telefone = cliente.telefone,
                celular = cliente.celular,
                latitude = cliente.latitude,
                longitude = cliente.longitude
            )

            val sucessoFirestore = remoteDataSource.insert(clienteDominio)
            if (sucessoFirestore) {
                Log.d(TAG, "Cliente salvo com sucesso no Firestore e localmente")
            } else {
                Log.w(TAG, "Cliente salvo localmente, mas falhou no Firestore")
            }

            sucessoFirestore
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar cliente: ${cliente.nome}", e)
            false
        }
    }

    // Exemplo de sincronização simples
    suspend fun syncLocalToRemote(): Int = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Iniciando sincronização local -> remoto")
            val localClientes = clienteDao.getAll()
            var sincronizados = 0

            localClientes.forEach { entity ->
                val cliente = Cliente(
                    id = entity.id,
                    nome = entity.nome,
                    cnpjCpf = entity.cnpjCpf,
                    contatos = entity.contatos, // Corrigido para contatos
                    endereco = entity.endereco,
                    cidade = entity.cidade,
                    estado = entity.estado,
                    telefone = entity.telefone,
                    celular = entity.celular,
                    latitude = entity.latitude,
                    longitude = entity.longitude
                )

                if (remoteDataSource.insert(cliente)) {
                    sincronizados++
                }
            }

            Log.d(TAG, "Sincronização concluída: $sincronizados/${localClientes.size} clientes")
            sincronizados
        } catch (e: Exception) {
            Log.e(TAG, "Erro durante sincronização", e)
            0
        }
    }

    // Método para sincronizar dados existentes que não foram salvos no Firestore
    suspend fun sincronizarDadosExistentes(): Int = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Verificando dados locais que não estão no Firestore...")

            val clientesLocais = clienteDao.getAll()
            val clientesRemotos = remoteDataSource.getAll()
            val idsRemotos = clientesRemotos.map { it.id }.toSet()

            // Encontrar clientes locais que não estão no Firestore
            val clientesParaSincronizar = clientesLocais.filter { it.id !in idsRemotos }

            Log.d(TAG, "Encontrados ${clientesParaSincronizar.size} clientes para sincronizar")

            var sincronizados = 0
            clientesParaSincronizar.forEach { entity ->
                val cliente = Cliente(
                    id = entity.id,
                    nome = entity.nome,
                    cnpjCpf = entity.cnpjCpf,
                    contatos = entity.contatos, // Corrigido para contatos
                    endereco = entity.endereco,
                    cidade = entity.cidade,
                    estado = entity.estado,
                    telefone = entity.telefone,
                    celular = entity.celular,
                    latitude = entity.latitude,
                    longitude = entity.longitude
                )

                if (remoteDataSource.insert(cliente)) {
                    sincronizados++
                    Log.d(TAG, "Cliente sincronizado: ${entity.nome}")
                }
            }

            Log.d(TAG, "Sincronização concluída: $sincronizados/${clientesParaSincronizar.size} clientes")
            sincronizados
        } catch (e: Exception) {
            Log.e(TAG, "Erro durante sincronização de dados existentes", e)
            0
        }
    }
}
