package com.example.aprimortech.data.repository

import android.util.Log
import com.example.aprimortech.model.Tinta
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Locale

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
     * Gera um document ID seguro a partir do código normalizado
     */
    private fun toDocId(normalized: String): String {
        // manter apenas caracteres alfanuméricos, underscore e hífen
        return normalized.replace("[^a-z0-9_-]".toRegex(), "_")
    }

    /**
     * Salva uma nova tinta — agora verifica duplicatas no banco e usa ID normalizado em transação.
     * Retorna o ID do documento salvo ou do documento existente caso já exista.
     */
    suspend fun salvarTinta(codigo: String): String? {
        val novo = codigo.trim()
        if (novo.isBlank()) return null

        val normalized = novo.lowercase(Locale.getDefault())

        return try {
            // 1) Se já houver um documento com campo codigo_normalized, retorne-o (compatibilidade)
            val byNormalized = collection.whereEqualTo("codigo_normalized", normalized).get().await()
            if (!byNormalized.isEmpty) {
                val existingId = byNormalized.documents.first().id
                Log.d(TAG, "Tinta já existe (match normalized) com ID: $existingId")
                return existingId
            }

            // 2) Usar docId derivado do código normalizado e transação para criar apenas se não existir
            val docId = toDocId(normalized)
            val docRef = collection.document(docId)

            val resultId = db.runTransaction { transaction ->
                val snap = transaction.get(docRef)
                if (snap.exists()) {
                    snap.id
                } else {
                    val data = mapOf(
                        "codigo" to novo,
                        "codigo_normalized" to normalized,
                        "timestamp" to System.currentTimeMillis()
                    )
                    transaction.set(docRef, data)
                    docRef.id
                }
            }.await()

            Log.d(TAG, "Tinta salva/obtida com ID: $resultId")
            resultId
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar tinta", e)
            null
        }
    }

    /**
     * Migra documentos existentes adicionando o campo `codigo_normalized` quando ausente.
     * Retorna o número de documentos atualizados.
     * Atenção: operação que lê e escreve na coleção inteira — usar uma vez (administrativo).
     */
    suspend fun migrarAdicionarCodigoNormalized(): Int {
        return try {
            val snapshot = collection.get().await()
            var updated = 0
            var batch = db.batch()
            var ops = 0

            for (doc in snapshot.documents) {
                val hasNormalized = doc.getString("codigo_normalized")
                if (hasNormalized == null) {
                    val codigo = doc.getString("codigo") ?: continue
                    val normalized = codigo.trim().lowercase(Locale.getDefault())
                    batch.update(doc.reference, mapOf("codigo_normalized" to normalized))
                    ops++
                    updated++
                    if (ops >= 500) {
                        batch.commit().await()
                        batch = db.batch()
                        ops = 0
                    }
                }
            }

            if (ops > 0) {
                batch.commit().await()
            }

            Log.d(TAG, "Migração concluída. Documentos atualizados: $updated")
            updated
        } catch (e: Exception) {
            Log.e(TAG, "Erro durante migração de tintas", e)
            0
        }
    }
}
