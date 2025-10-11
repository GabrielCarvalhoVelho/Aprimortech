package com.example.aprimortech.data.remote

import com.example.aprimortech.model.Peca
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PecaRemoteDataSource @Inject constructor() {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("pecas")

    suspend fun buscarTodasPecas(): List<Peca> {
        return try {
            android.util.Log.d("PecaRemoteDataSource", "Iniciando busca de todas as peças no Firebase")
            val snapshot = collection.get().await()
            android.util.Log.d("PecaRemoteDataSource", "Snapshot obtido: ${snapshot.documents.size} documentos encontrados")

            val pecas = snapshot.documents.mapNotNull { document ->
                android.util.Log.d("PecaRemoteDataSource", "Processando documento: ${document.id}")
                document.toObject(Peca::class.java)?.copy(id = document.id)
            }

            android.util.Log.d("PecaRemoteDataSource", "Busca concluída: ${pecas.size} peças convertidas com sucesso")
            pecas
        } catch (e: Exception) {
            android.util.Log.e("PecaRemoteDataSource", "Erro ao buscar peças do Firebase", e)
            emptyList()
        }
    }

    suspend fun buscarPecaPorId(id: String): Peca? {
        return try {
            android.util.Log.d("PecaRemoteDataSource", "Buscando peça por ID: $id")
            val document = collection.document(id).get().await()
            android.util.Log.d("PecaRemoteDataSource", "Documento encontrado: ${document.exists()}")

            val peca = document.toObject(Peca::class.java)?.copy(id = document.id)
            android.util.Log.d("PecaRemoteDataSource", "Peça convertida: ${peca?.codigo ?: "null"}")
            peca
        } catch (e: Exception) {
            android.util.Log.e("PecaRemoteDataSource", "Erro ao buscar peça por ID: $id", e)
            null
        }
    }

    suspend fun salvarPeca(peca: Peca) {
        try {
            android.util.Log.w("PECA_FIREBASE_TEST", "=== TESTE CRÍTICO - SALVANDO PEÇA ===")
            android.util.Log.w("PECA_FIREBASE_TEST", "Código da peça: ${peca.codigo}")
            android.util.Log.w("PECA_FIREBASE_TEST", "Firebase configurado: ${firestore != null}")
            android.util.Log.w("PECA_FIREBASE_TEST", "Collection configurada: ${collection != null}")

            android.util.Log.d("PecaRemoteDataSource", "Iniciando salvamento da peça: ${peca.codigo} (ID: ${peca.id})")

            if (peca.id.isNotEmpty()) {
                android.util.Log.d("PecaRemoteDataSource", "Atualizando peça existente com ID: ${peca.id}")
                collection.document(peca.id).set(peca).await()
                android.util.Log.d("PecaRemoteDataSource", "Peça atualizada com sucesso no Firebase")
            } else {
                android.util.Log.d("PecaRemoteDataSource", "Criando nova peça sem ID")
                val docRef = collection.add(peca).await()
                android.util.Log.d("PecaRemoteDataSource", "Nova peça criada com ID: ${docRef.id}")
            }
        } catch (e: Exception) {
            android.util.Log.e("PECA_FIREBASE_TEST", "=== ERRO CRÍTICO NO SALVAMENTO ===", e)
            throw e
        }
    }

    suspend fun excluirPeca(id: String) {
        try {
            android.util.Log.d("PecaRemoteDataSource", "Iniciando exclusão da peça com ID: $id")
            collection.document(id).delete().await()
            android.util.Log.d("PecaRemoteDataSource", "Peça excluída com sucesso do Firebase")
        } catch (e: Exception) {
            android.util.Log.e("PecaRemoteDataSource", "Erro ao excluir peça com ID: $id", e)
            throw e
        }
    }

    suspend fun sincronizarPecas(pecas: List<Peca>) {
        try {
            android.util.Log.d("PecaRemoteDataSource", "Iniciando sincronização de ${pecas.size} peças")
            val batch = firestore.batch()

            // Primeiro, limpa a coleção existente
            android.util.Log.d("PecaRemoteDataSource", "Buscando documentos existentes para exclusão")
            val existingDocs = collection.get().await()
            android.util.Log.d("PecaRemoteDataSource", "Encontrados ${existingDocs.documents.size} documentos existentes")

            existingDocs.documents.forEach { doc ->
                android.util.Log.d("PecaRemoteDataSource", "Marcando para exclusão: ${doc.id}")
                batch.delete(doc.reference)
            }

            // Depois, adiciona as novas peças
            android.util.Log.d("PecaRemoteDataSource", "Adicionando ${pecas.size} novas peças ao batch")
            pecas.forEach { peca ->
                val docRef = if (peca.id.isNotEmpty()) {
                    android.util.Log.d("PecaRemoteDataSource", "Usando ID existente: ${peca.id}")
                    collection.document(peca.id)
                } else {
                    android.util.Log.d("PecaRemoteDataSource", "Gerando novo ID para: ${peca.codigo}")
                    collection.document()
                }
                batch.set(docRef, peca)
            }

            android.util.Log.d("PecaRemoteDataSource", "Executando batch commit")
            batch.commit().await()
            android.util.Log.d("PecaRemoteDataSource", "Sincronização concluída com sucesso")
        } catch (e: Exception) {
            android.util.Log.e("PecaRemoteDataSource", "Erro ao sincronizar peças", e)
            throw e
        }
    }
}
