package com.example.aprimortech.data.repository

import com.example.aprimortech.model.Cliente
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClienteRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("clientes")

    suspend fun buscarClientes(): List<Cliente> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Cliente::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscarClientePorId(id: String): Cliente? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Cliente::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun salvarCliente(cliente: Cliente): String {
        return try {
            if (cliente.id.isEmpty()) {
                val documentRef = collection.add(cliente).await()
                documentRef.id
            } else {
                collection.document(cliente.id).set(cliente).await()
                cliente.id
            }
        } catch (e: Exception) {
            throw Exception("Erro ao salvar cliente: ${e.message}")
        }
    }

    suspend fun excluirCliente(id: String) {
        try {
            collection.document(id).delete().await()
        } catch (e: Exception) {
            throw Exception("Erro ao excluir cliente: ${e.message}")
        }
    }
}
