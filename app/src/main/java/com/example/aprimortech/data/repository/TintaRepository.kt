package com.example.aprimortech.data.repository

import android.util.Log
import com.example.aprimortech.model.Tinta
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TintaRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("tintas")

    companion object {
        private const val TAG = "TintaRepository"
    }

    /**
     * Busca uma tinta pelo ID
     */
    suspend fun buscarTintaPorId(tintaId: String): Tinta? {
        return try {
            Log.d(TAG, "Buscando tinta com ID: $tintaId")
            val document = collection.document(tintaId).get().await()
            document.toObject(Tinta::class.java)?.copy(id = document.id).also {
                Log.d(TAG, "Tinta encontrada: ${it?.codigo}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar tinta", e)
            null
        }
    }

    /**
     * Busca todas as tintas
     */
    suspend fun buscarTintas(): List<Tinta> {
        return try {
            Log.d(TAG, "Buscando todas as tintas...")
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull {
                it.toObject(Tinta::class.java)?.copy(id = it.id)
            }.also {
                Log.d(TAG, "Encontradas ${it.size} tintas")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar tintas", e)
            emptyList()
        }
    }

    /**
     * Busca todas as tintas (alias para compatibilidade)
     */
    suspend fun buscarTodas(): List<Tinta> = buscarTintas()

    /**
     * Salva uma nova tinta
     */
    suspend fun salvarTinta(codigo: String): String? {
        return try {
            val tinta = Tinta(codigo = codigo)
            val docRef = collection.add(tinta).await()
            Log.d(TAG, "Tinta salva com ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar tinta", e)
            null
        }
    }
}
