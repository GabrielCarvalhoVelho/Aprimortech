package com.example.aprimortech.data.repository

import android.util.Log
import com.example.aprimortech.model.Solvente
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SolventeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("solventes")

    companion object {
        private const val TAG = "SolventeRepository"
    }

    /**
     * Busca um solvente pelo ID
     */
    suspend fun buscarSolventePorId(solventeId: String): Solvente? {
        return try {
            Log.d(TAG, "Buscando solvente com ID: $solventeId")
            val document = collection.document(solventeId).get().await()
            document.toObject(Solvente::class.java)?.copy(id = document.id).also {
                Log.d(TAG, "Solvente encontrado: ${it?.codigo}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar solvente", e)
            null
        }
    }

    /**
     * Busca todos os solventes
     */
    suspend fun buscarSolventes(): List<Solvente> {
        return try {
            Log.d(TAG, "Buscando todos os solventes...")
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull {
                it.toObject(Solvente::class.java)?.copy(id = it.id)
            }.also {
                Log.d(TAG, "Encontrados ${it.size} solventes")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar solventes", e)
            emptyList()
        }
    }

    /**
     * Busca todos os solventes (alias para compatibilidade)
     */
    suspend fun buscarTodos(): List<Solvente> = buscarSolventes()

    /**
     * Salva um novo solvente
     */
    suspend fun salvarSolvente(codigo: String): String? {
        return try {
            val solvente = Solvente(codigo = codigo)
            val docRef = collection.add(solvente).await()
            Log.d(TAG, "Solvente salvo com ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar solvente", e)
            null
        }
    }
}
