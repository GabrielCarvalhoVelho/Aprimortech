package com.example.aprimortech.data.repository

import android.util.Log
import com.example.aprimortech.data.local.dao.ClienteDao
import com.example.aprimortech.data.local.entity.toEntity
import com.example.aprimortech.data.local.entity.toModel
import com.example.aprimortech.model.Cliente
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Repositório de Clientes com suporte completo para operação offline
 *
 * Estratégia:
 * 1. SEMPRE salva localmente primeiro (garantia de persistência offline)
 * 2. Tenta sincronizar com Firebase quando há conexão
 * 3. Marca itens como pendentes se sincronização falhar
 * 4. Lê dados locais como fonte primária
 */
class ClienteRepository(
    private val firestore: FirebaseFirestore,
    private val clienteDao: ClienteDao
) {
    companion object {
        private const val TAG = "ClienteRepository"
        private const val COLLECTION_CLIENTES = "clientes"
    }

    /**
     * Busca todos os clientes - Prioriza cache local
     */
    suspend fun buscarClientes(): List<Cliente> {
        return try {
            Log.d(TAG, "📂 Buscando clientes do cache local...")
            val clientesLocais = clienteDao.buscarTodosClientes()

            Log.d(TAG, "📊 ${clientesLocais.size} clientes no cache local")

            if (clientesLocais.isEmpty()) {
                Log.d(TAG, "⚠️ Cache vazio, buscando do Firebase...")
                // CORREÇÃO: Retornar os dados do Firebase
                return buscarClientesDoFirebase()
            } else {
                Log.d(TAG, "✅ Retornando ${clientesLocais.size} clientes do cache local:")
                clientesLocais.forEach {
                    Log.d(TAG, "   - ${it.nome} (ID: ${it.id}, Pendente: ${it.pendenteSincronizacao})")
                }
                clientesLocais.map { it.toModel() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao buscar clientes", e)
            // Retorna cache local mesmo em caso de erro
            clienteDao.buscarTodosClientes().map { it.toModel() }
        }
    }

    /**
     * Busca clientes do Firebase e atualiza cache local
     */
    private suspend fun buscarClientesDoFirebase(): List<Cliente> {
        return try {
            val snapshot = firestore.collection(COLLECTION_CLIENTES)
                .get()
                .await()

            val clientes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Cliente::class.java)?.copy(id = doc.id)
            }

            // Atualiza cache local
            if (clientes.isNotEmpty()) {
                val entities = clientes.map { it.toEntity(pendenteSincronizacao = false) }
                clienteDao.inserirClientes(entities)
                Log.d(TAG, "✅ Cache local atualizado com ${clientes.size} clientes do Firebase")
            }

            clientes
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao buscar clientes do Firebase", e)
            // Retorna cache local se Firebase falhar
            clienteDao.buscarTodosClientes().map { it.toModel() }
        }
    }

    /**
     * Observa mudanças nos clientes em tempo real (Flow)
     */
    fun observarClientes(): Flow<List<Cliente>> {
        return clienteDao.observarTodosClientes()
            .map { entities -> entities.map { it.toModel() } }
    }

    /**
     * Salva cliente com estratégia offline-first
     */
    suspend fun salvarCliente(cliente: Cliente): String {
        return try {
            // 1. Gera ID se necessário
            val clienteId = cliente.id.ifEmpty { UUID.randomUUID().toString() }
            val clienteComId = cliente.copy(id = clienteId)

            // 2. SEMPRE salva localmente primeiro
            val entity = clienteComId.toEntity(pendenteSincronizacao = true)
            clienteDao.inserirCliente(entity)
            Log.d(TAG, "✅ Cliente '${cliente.nome}' salvo localmente")

            // 3. Tenta sincronizar com Firebase
            try {
                firestore.collection(COLLECTION_CLIENTES)
                    .document(clienteId)
                    .set(clienteComId)
                    .await()

                // Marca como sincronizado
                clienteDao.marcarComoSincronizado(clienteId)
                Log.d(TAG, "✅ Cliente '${cliente.nome}' sincronizado com Firebase")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Falha na sincronização com Firebase, mantido como pendente", e)
                // Cliente permanece marcado como pendente de sincronização
            }

            clienteId
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao salvar cliente", e)
            throw e
        }
    }

    /**
     * Exclui cliente com estratégia offline-first
     */
    suspend fun excluirCliente(clienteId: String) {
        try {
            // 1. Remove do cache local imediatamente
            clienteDao.deletarClientePorId(clienteId)
            Log.d(TAG, "✅ Cliente excluído do cache local")

            // 2. Tenta excluir do Firebase
            try {
                firestore.collection(COLLECTION_CLIENTES)
                    .document(clienteId)
                    .delete()
                    .await()
                Log.d(TAG, "✅ Cliente excluído do Firebase")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Falha ao excluir do Firebase, será removido na próxima sincronização", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao excluir cliente", e)
            throw e
        }
    }

    /**
     * Busca cliente por ID
     */
    suspend fun buscarClientePorId(clienteId: String): Cliente? {
        return try {
            // Busca primeiro no cache local
            val local = clienteDao.buscarClientePorId(clienteId)
            if (local != null) {
                return local.toModel()
            }

            // Se não encontrar, busca no Firebase
            val doc = firestore.collection(COLLECTION_CLIENTES)
                .document(clienteId)
                .get()
                .await()

            doc.toObject(Cliente::class.java)?.copy(id = doc.id)?.also { cliente ->
                // Salva no cache
                clienteDao.inserirCliente(cliente.toEntity(pendenteSincronizacao = false))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar cliente por ID", e)
            null
        }
    }

    /**
     * Sincroniza clientes pendentes com Firebase
     */
    suspend fun sincronizarClientesPendentes(): Int {
        return try {
            val pendentes = clienteDao.buscarClientesPendentesSincronizacao()
            Log.d(TAG, "Sincronizando ${pendentes.size} clientes pendentes...")

            var sincronizados = 0
            pendentes.forEach { entity ->
                try {
                    firestore.collection(COLLECTION_CLIENTES)
                        .document(entity.id)
                        .set(entity.toModel())
                        .await()

                    clienteDao.marcarComoSincronizado(entity.id)
                    sincronizados++
                    Log.d(TAG, "✅ Cliente '${entity.nome}' sincronizado")
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Falha ao sincronizar cliente '${entity.nome}'", e)
                }
            }

            Log.d(TAG, "✅ ${sincronizados}/${pendentes.size} clientes sincronizados")
            sincronizados
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao sincronizar clientes pendentes", e)
            0
        }
    }

    /**
     * Força sincronização completa com Firebase
     * Baixa todos os clientes do Firebase e atualiza cache local
     */
    suspend fun sincronizarComFirebase() {
        try {
            Log.d(TAG, "Iniciando sincronização completa com Firebase...")

            // 1. Sincroniza pendentes locais para Firebase
            sincronizarClientesPendentes()

            // 2. Baixa dados atualizados do Firebase
            buscarClientesDoFirebase()

            Log.d(TAG, "✅ Sincronização completa finalizada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro na sincronização completa", e)
        }
    }

    /**
     * Retorna quantidade de clientes pendentes de sincronização
     */
    suspend fun contarClientesPendentes(): Int {
        return try {
            clienteDao.contarClientesPendentes()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao contar clientes pendentes", e)
            0
        }
    }

    /**
     * Pesquisa clientes por nome ou CNPJ/CPF
     */
    suspend fun pesquisarClientes(query: String): List<Cliente> {
        return try {
            clienteDao.buscarClientesPorNome(query).map { it.toModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao pesquisar clientes", e)
            emptyList()
        }
    }

    /**
     * Limpa cache local (use com cuidado!)
     */
    suspend fun limparCacheLocal() {
        try {
            clienteDao.limparTodosClientes()
            Log.d(TAG, "Cache local limpo")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar cache", e)
        }
    }
}
