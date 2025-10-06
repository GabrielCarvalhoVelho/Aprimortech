package com.example.aprimortech.data.repository

import com.example.aprimortech.model.Setor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SetorRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("setores")

    suspend fun buscarSetores(): List<Setor> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Setor::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscarSetoresPorCliente(clienteId: String): List<Setor> {
        return try {
            val snapshot = collection
                .whereEqualTo("clienteId", clienteId)
                .get()
                .await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Setor::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun salvarSetor(setor: Setor): String {
        return try {
            if (setor.id.isEmpty()) {
                val documentRef = collection.add(setor).await()
                documentRef.id
            } else {
                collection.document(setor.id).set(setor).await()
                setor.id
            }
        } catch (e: Exception) {
            throw Exception("Erro ao salvar setor: ${e.message}")
        }
    }

    suspend fun excluirSetor(id: String) {
        try {
            collection.document(id).delete().await()
        } catch (e: Exception) {
            throw Exception("Erro ao excluir setor: ${e.message}")
        }
    }
}
