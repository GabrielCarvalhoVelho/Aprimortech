package com.example.aprimortech.data.repository

import com.example.aprimortech.model.Relatorio
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RelatorioRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("relatorios")

    suspend fun buscarTodosRelatorios(): List<Relatorio> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Relatorio::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscarRelatorioPorId(id: String): Relatorio? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Relatorio::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun buscarRelatoriosPorCliente(clienteId: String): List<Relatorio> {
        return try {
            val snapshot = collection
                .whereEqualTo("clienteId", clienteId)
                .get()
                .await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Relatorio::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscarRelatoriosPorMaquina(maquinaId: String): List<Relatorio> {
        return try {
            val snapshot = collection
                .whereEqualTo("maquinaId", maquinaId)
                .get()
                .await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Relatorio::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun salvarRelatorio(relatorio: Relatorio): String {
        return try {
            if (relatorio.id.isEmpty()) {
                // Novo relatório
                val documentRef = collection.add(relatorio).await()
                documentRef.id
            } else {
                // Atualizar relatório existente
                collection.document(relatorio.id).set(relatorio).await()
                relatorio.id
            }
        } catch (e: Exception) {
            throw Exception("Erro ao salvar relatório: ${e.message}")
        }
    }

    suspend fun excluirRelatorio(id: String) {
        try {
            collection.document(id).delete().await()
        } catch (e: Exception) {
            throw Exception("Erro ao excluir relatório: ${e.message}")
        }
    }
}
