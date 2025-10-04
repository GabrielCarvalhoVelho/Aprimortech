package com.example.aprimortech.data.remote

import android.util.Log
import com.example.aprimortech.model.Cliente
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ClienteRemoteDataSource {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("clientes")

    companion object {
        private const val TAG = "ClienteRemoteDataSource"
    }

    suspend fun getAll(): List<Cliente> {
        return try {
            Log.d(TAG, "Buscando todos os clientes do Firestore...")
            val snapshot = collection.get().await()
            val clientes = snapshot.documents.mapNotNull {
                it.toObject(Cliente::class.java)?.copy(id = it.id)
            }
            Log.d(TAG, "Encontrados ${clientes.size} clientes no Firestore")
            clientes
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar clientes do Firestore", e)
            emptyList()
        }
    }

    suspend fun getById(id: String): Cliente? {
        return try {
            Log.d(TAG, "Buscando cliente com ID: $id")
            val doc = collection.document(id).get().await()
            val cliente = doc.toObject(Cliente::class.java)?.copy(id = doc.id)
            Log.d(TAG, "Cliente encontrado: ${cliente?.nome ?: "não encontrado"}")
            cliente
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar cliente com ID $id", e)
            null
        }
    }

    suspend fun insert(cliente: Cliente): Boolean {
        return try {
            Log.d(TAG, "Salvando cliente no Firestore: ${cliente.nome} (ID: ${cliente.id})")

            // Validar se o ID não está vazio
            if (cliente.id.isBlank()) {
                Log.e(TAG, "Erro: ID do cliente está vazio")
                return false
            }

            // Verificar se o usuário está autenticado
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.e(TAG, "Usuário não está autenticado - não é possível salvar no Firestore")
                return false
            }

            Log.d(TAG, "Usuário autenticado: ${currentUser.email}")

            // Usar o ID do cliente se fornecido, ou gerar um novo
            val docRef = if (cliente.id.isNotBlank()) {
                collection.document(cliente.id)
            } else {
                collection.document()
            }

            // Criar um mapa com os dados para garantir que tudo seja salvo
            val clienteData = mapOf(
                "nome" to cliente.nome,
                "cnpjCpf" to cliente.cnpjCpf,
                "contatos" to cliente.contatos, // Agora é uma lista
                "endereco" to cliente.endereco,
                "cidade" to cliente.cidade,
                "estado" to cliente.estado,
                "telefone" to cliente.telefone,
                "celular" to cliente.celular,
                "latitude" to cliente.latitude,
                "longitude" to cliente.longitude,
                "criadoPor" to currentUser.uid,
                "criadoEm" to com.google.firebase.Timestamp.now()
            )

            docRef.set(clienteData).await()
            Log.d(TAG, "Cliente salvo com sucesso no Firestore: ${cliente.nome}")
            true
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            when (e.code) {
                com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Log.e(TAG, "ERRO DE PERMISSÃO: O usuário não tem permissão para escrever no Firestore. Verifique as regras de segurança.", e)
                }
                com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                    Log.e(TAG, "ERRO DE AUTENTICAÇÃO: Usuário não está autenticado.", e)
                }
                else -> {
                    Log.e(TAG, "Erro do Firestore: ${e.code} - ${e.message}", e)
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Erro genérico ao salvar cliente no Firestore: ${cliente.nome}", e)
            false
        }
    }

    suspend fun update(cliente: Cliente): Boolean {
        return try {
            Log.d(TAG, "Atualizando cliente no Firestore: ${cliente.nome}")

            val clienteData = mapOf(
                "nome" to cliente.nome,
                "cnpjCpf" to cliente.cnpjCpf,
                "contatos" to cliente.contatos, // Agora é uma lista
                "endereco" to cliente.endereco,
                "cidade" to cliente.cidade,
                "estado" to cliente.estado,
                "telefone" to cliente.telefone,
                "celular" to cliente.celular,
                "latitude" to cliente.latitude,
                "longitude" to cliente.longitude
            )

            collection.document(cliente.id).set(clienteData).await()
            Log.d(TAG, "Cliente atualizado com sucesso no Firestore: ${cliente.nome}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar cliente no Firestore: ${cliente.nome}", e)
            false
        }
    }

    suspend fun delete(id: String): Boolean {
        return try {
            Log.d(TAG, "Excluindo cliente do Firestore com ID: $id")
            collection.document(id).delete().await()
            Log.d(TAG, "Cliente excluído com sucesso do Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao excluir cliente do Firestore com ID $id", e)
            false
        }
    }
}
