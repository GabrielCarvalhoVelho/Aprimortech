package com.example.aprimortech.data.repository

import com.example.aprimortech.model.Peca
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PecaRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("pecas")

    suspend fun buscarPecas(): List<Peca> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Peca::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscarPecaPorId(id: String): Peca? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Peca::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun salvarPeca(peca: Peca): String {
        return try {
            if (peca.id.isEmpty()) {
                val documentRef = collection.add(peca).await()
                documentRef.id
            } else {
                collection.document(peca.id).set(peca).await()
                peca.id
            }
        } catch (e: Exception) {
            throw Exception("Erro ao salvar peça: ${e.message}")
        }
    }

    suspend fun excluirPeca(id: String) {
        try {
            collection.document(id).delete().await()
        } catch (e: Exception) {
            throw Exception("Erro ao excluir peça: ${e.message}")
        }
    }
}
