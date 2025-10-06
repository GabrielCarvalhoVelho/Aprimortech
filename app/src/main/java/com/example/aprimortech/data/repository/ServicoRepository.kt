package com.example.aprimortech.data.repository

import com.example.aprimortech.model.Servico
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import java.text.SimpleDateFormat
import java.util.*

@Singleton
class ServicoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("servicos")

    suspend fun buscarServicos(): List<Servico> {
        return try {
            val snapshot = collection
                .orderBy("vezesUsado", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Servico::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscarTop5Servicos(): List<Servico> {
        return try {
            val snapshot = collection
                .orderBy("vezesUsado", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Servico::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscarServicoPorNome(nome: String): Servico? {
        return try {
            val snapshot = collection
                .whereEqualTo("nome", nome)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.let { document ->
                document.toObject(Servico::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun salvarOuAtualizarServico(nomeServico: String): String {
        return try {
            val servicoExistente = buscarServicoPorNome(nomeServico)
            val dataAtual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            if (servicoExistente != null) {
                // Atualizar contador e último uso
                val servicoAtualizado = servicoExistente.copy(
                    vezesUsado = servicoExistente.vezesUsado + 1,
                    ultimoUso = dataAtual
                )
                collection.document(servicoExistente.id).set(servicoAtualizado).await()
                servicoExistente.id
            } else {
                // Criar novo serviço
                val novoServico = Servico(
                    nome = nomeServico,
                    vezesUsado = 1,
                    ultimoUso = dataAtual
                )
                val documentRef = collection.add(novoServico).await()
                documentRef.id
            }
        } catch (e: Exception) {
            throw Exception("Erro ao salvar serviço: ${e.message}")
        }
    }
}
