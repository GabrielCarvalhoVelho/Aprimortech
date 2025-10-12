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
            android.util.Log.d("RelatorioRepository", "=== SALVANDO RELATÓRIO ===")
            android.util.Log.d("RelatorioRepository", "ID: ${relatorio.id}")
            android.util.Log.d("RelatorioRepository", "⭐ Valor Hora Técnica: ${relatorio.valorHoraTecnica}")

            if (relatorio.id.isEmpty()) {
                // Novo relatório
                android.util.Log.d("RelatorioRepository", "Criando NOVO relatório no Firestore...")

                // Criar mapa explícito para garantir que todos os campos sejam salvos
                val relatorioMap = hashMapOf<String, Any?>(
                    "clienteId" to relatorio.clienteId,
                    "maquinaId" to relatorio.maquinaId,
                    "pecaIds" to relatorio.pecaIds,
                    "descricaoServico" to relatorio.descricaoServico,
                    "recomendacoes" to relatorio.recomendacoes,
                    "numeroNotaFiscal" to relatorio.numeroNotaFiscal,
                    "dataRelatorio" to relatorio.dataRelatorio,
                    "horarioEntrada" to relatorio.horarioEntrada,
                    "horarioSaida" to relatorio.horarioSaida,
                    "valorHoraTecnica" to relatorio.valorHoraTecnica,
                    "distanciaKm" to relatorio.distanciaKm,
                    "valorDeslocamentoPorKm" to relatorio.valorDeslocamentoPorKm,
                    "valorDeslocamentoTotal" to relatorio.valorDeslocamentoTotal,
                    "valorPedagios" to relatorio.valorPedagios,
                    "custoPecas" to relatorio.custoPecas,
                    "observacoes" to relatorio.observacoes,
                    "assinaturaCliente" to relatorio.assinaturaCliente,
                    "assinaturaTecnico" to relatorio.assinaturaTecnico,
                    "tintaId" to relatorio.tintaId,
                    "solventeId" to relatorio.solventeId,
                    "codigoTinta" to relatorio.codigoTinta,
                    "codigoSolvente" to relatorio.codigoSolvente,
                    "dataProximaPreventiva" to relatorio.dataProximaPreventiva,
                    "horasProximaPreventiva" to relatorio.horasProximaPreventiva,
                    "defeitosIdentificados" to relatorio.defeitosIdentificados,
                    "servicosRealizados" to relatorio.servicosRealizados,
                    "observacoesDefeitosServicos" to relatorio.observacoesDefeitosServicos,
                    "pecasUtilizadas" to relatorio.pecasUtilizadas, // ⭐ NOVO CAMPO
                    "syncPending" to relatorio.syncPending
                )

                android.util.Log.d("RelatorioRepository", "valorHoraTecnica no mapa: ${relatorioMap["valorHoraTecnica"]}")
                android.util.Log.d("RelatorioRepository", "⭐ pecasUtilizadas no mapa: ${relatorioMap["pecasUtilizadas"]}")

                val documentRef = collection.add(relatorioMap).await()
                android.util.Log.d("RelatorioRepository", "✅ Relatório criado com ID: ${documentRef.id}")
                documentRef.id
            } else {
                // Atualizar relatório existente
                android.util.Log.d("RelatorioRepository", "Atualizando relatório existente: ${relatorio.id}")

                val relatorioMap = hashMapOf<String, Any?>(
                    "clienteId" to relatorio.clienteId,
                    "maquinaId" to relatorio.maquinaId,
                    "pecaIds" to relatorio.pecaIds,
                    "descricaoServico" to relatorio.descricaoServico,
                    "recomendacoes" to relatorio.recomendacoes,
                    "numeroNotaFiscal" to relatorio.numeroNotaFiscal,
                    "dataRelatorio" to relatorio.dataRelatorio,
                    "horarioEntrada" to relatorio.horarioEntrada,
                    "horarioSaida" to relatorio.horarioSaida,
                    "valorHoraTecnica" to relatorio.valorHoraTecnica,
                    "distanciaKm" to relatorio.distanciaKm,
                    "valorDeslocamentoPorKm" to relatorio.valorDeslocamentoPorKm,
                    "valorDeslocamentoTotal" to relatorio.valorDeslocamentoTotal,
                    "valorPedagios" to relatorio.valorPedagios,
                    "custoPecas" to relatorio.custoPecas,
                    "observacoes" to relatorio.observacoes,
                    "assinaturaCliente" to relatorio.assinaturaCliente,
                    "assinaturaTecnico" to relatorio.assinaturaTecnico,
                    "tintaId" to relatorio.tintaId,
                    "solventeId" to relatorio.solventeId,
                    "codigoTinta" to relatorio.codigoTinta,
                    "codigoSolvente" to relatorio.codigoSolvente,
                    "dataProximaPreventiva" to relatorio.dataProximaPreventiva,
                    "horasProximaPreventiva" to relatorio.horasProximaPreventiva,
                    "defeitosIdentificados" to relatorio.defeitosIdentificados,
                    "servicosRealizados" to relatorio.servicosRealizados,
                    "observacoesDefeitosServicos" to relatorio.observacoesDefeitosServicos,
                    "pecasUtilizadas" to relatorio.pecasUtilizadas, // ⭐ NOVO CAMPO
                    "syncPending" to relatorio.syncPending
                )

                collection.document(relatorio.id).set(relatorioMap).await()
                android.util.Log.d("RelatorioRepository", "✅ Relatório atualizado com sucesso")
                relatorio.id
            }
        } catch (e: Exception) {
            android.util.Log.e("RelatorioRepository", "❌ Erro ao salvar relatório: ${e.message}", e)
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
