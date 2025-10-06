package com.example.aprimortech.data.repository

import com.example.aprimortech.model.Contato
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContatoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("contatos")

    suspend fun buscarContatos(): List<Contato> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Contato::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscarContatosPorCliente(clienteId: String): List<Contato> {
        return try {
            val snapshot = collection
                .whereEqualTo("clienteId", clienteId)
                .get()
                .await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Contato::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun salvarContato(contato: Contato): String {
        return try {
            if (contato.id.isEmpty()) {
                val documentRef = collection.add(contato).await()
                documentRef.id
            } else {
                collection.document(contato.id).set(contato).await()
                contato.id
            }
        } catch (e: Exception) {
            throw Exception("Erro ao salvar contato: ${e.message}")
        }
    }

    suspend fun excluirContato(id: String) {
        try {
            collection.document(id).delete().await()
        } catch (e: Exception) {
            throw Exception("Erro ao excluir contato: ${e.message}")
        }
    }
}
