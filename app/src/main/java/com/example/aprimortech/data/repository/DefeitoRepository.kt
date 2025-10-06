package com.example.aprimortech.data.repository

import com.example.aprimortech.model.Defeito
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import java.text.SimpleDateFormat
import java.util.*

@Singleton
class DefeitoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("defeitos")

    suspend fun buscarDefeitos(): List<Defeito> {
        return try {
            val snapshot = collection
                .orderBy("vezesUsado", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Defeito::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscarTop5Defeitos(): List<Defeito> {
        return try {
            val snapshot = collection
                .orderBy("vezesUsado", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Defeito::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscarDefeitoPorNome(nome: String): Defeito? {
        return try {
            val snapshot = collection
                .whereEqualTo("nome", nome)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.let { document ->
                document.toObject(Defeito::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun salvarOuAtualizarDefeito(nomeDefeito: String): String {
        return try {
            val defeitoExistente = buscarDefeitoPorNome(nomeDefeito)
            val dataAtual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            if (defeitoExistente != null) {
                // Atualizar contador e último uso
                val defeitoAtualizado = defeitoExistente.copy(
                    vezesUsado = defeitoExistente.vezesUsado + 1,
                    ultimoUso = dataAtual
                )
                collection.document(defeitoExistente.id).set(defeitoAtualizado).await()
                defeitoExistente.id
            } else {
                // Criar novo defeito
                val novoDefeito = Defeito(
                    nome = nomeDefeito,
                    vezesUsado = 1,
                    ultimoUso = dataAtual
                )
                val documentRef = collection.add(novoDefeito).await()
                documentRef.id
            }
        } catch (e: Exception) {
            throw Exception("Erro ao salvar defeito: ${e.message}")
        }
    }
}
