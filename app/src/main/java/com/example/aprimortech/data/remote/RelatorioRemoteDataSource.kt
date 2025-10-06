package com.example.aprimortech.data.remote

import com.example.aprimortech.model.Relatorio
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RelatorioRemoteDataSource @Inject constructor() {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("relatorios")

    suspend fun salvarRelatorio(relatorio: Relatorio) {
        try {
            android.util.Log.d("RelatorioRemoteDataSource", "=== SALVANDO RELATÓRIO NO FIREBASE ===")
            android.util.Log.d("RelatorioRemoteDataSource", "ID: ${relatorio.id}")

            collection.document(relatorio.id).set(relatorio).await()
            android.util.Log.d("RelatorioRemoteDataSource", "Relatório salvo no Firebase com sucesso")
        } catch (e: Exception) {
            android.util.Log.e("RelatorioRemoteDataSource", "Erro ao salvar relatório no Firebase", e)
            throw e
        }
    }

    suspend fun excluirRelatorio(id: String) {
        try {
            android.util.Log.d("RelatorioRemoteDataSource", "=== EXCLUINDO RELATÓRIO DO FIREBASE ===")
            android.util.Log.d("RelatorioRemoteDataSource", "ID: $id")

            collection.document(id).delete().await()
            android.util.Log.d("RelatorioRemoteDataSource", "Relatório excluído do Firebase com sucesso")
        } catch (e: Exception) {
            android.util.Log.e("RelatorioRemoteDataSource", "Erro ao excluir relatório do Firebase", e)
            throw e
        }
    }

    suspend fun buscarTodosRelatorios(): List<Relatorio> {
        return try {
            android.util.Log.d("RelatorioRemoteDataSource", "=== BUSCANDO RELATÓRIOS DO FIREBASE ===")

            val snapshot = collection.get().await()
            val relatorios = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Relatorio::class.java)
            }

            android.util.Log.d("RelatorioRemoteDataSource", "Encontrados ${relatorios.size} relatórios no Firebase")
            relatorios
        } catch (e: Exception) {
            android.util.Log.e("RelatorioRemoteDataSource", "Erro ao buscar relatórios do Firebase", e)
            emptyList()
        }
    }

    suspend fun sincronizarRelatorios(relatorios: List<Relatorio>) {
        try {
            android.util.Log.d("RelatorioRemoteDataSource", "=== SINCRONIZANDO RELATÓRIOS COM FIREBASE ===")
            android.util.Log.d("RelatorioRemoteDataSource", "Quantidade: ${relatorios.size}")

            relatorios.forEach { relatorio ->
                collection.document(relatorio.id).set(relatorio).await()
            }

            android.util.Log.d("RelatorioRemoteDataSource", "Sincronização concluída com sucesso")
        } catch (e: Exception) {
            android.util.Log.e("RelatorioRemoteDataSource", "Erro na sincronização com Firebase", e)
            throw e
        }
    }
}
