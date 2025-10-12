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
            android.util.Log.d("RelatorioRemoteDataSource", "Cliente ID: ${relatorio.clienteId}")
            android.util.Log.d("RelatorioRemoteDataSource", "Máquina ID: ${relatorio.maquinaId}")
            android.util.Log.d("RelatorioRemoteDataSource", "Horário Entrada: ${relatorio.horarioEntrada}")
            android.util.Log.d("RelatorioRemoteDataSource", "Horário Saída: ${relatorio.horarioSaida}")
            android.util.Log.d("RelatorioRemoteDataSource", "⭐ Valor Hora Técnica: ${relatorio.valorHoraTecnica}")
            android.util.Log.d("RelatorioRemoteDataSource", "Distância KM: ${relatorio.distanciaKm}")
            android.util.Log.d("RelatorioRemoteDataSource", "Valor por KM: ${relatorio.valorDeslocamentoPorKm}")
            android.util.Log.d("RelatorioRemoteDataSource", "Código Tinta: ${relatorio.codigoTinta}")
            android.util.Log.d("RelatorioRemoteDataSource", "Código Solvente: ${relatorio.codigoSolvente}")

            // Criar um mapa explícito para garantir que todos os campos sejam salvos
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
                "assinaturaCliente1" to relatorio.assinaturaCliente1,
                "assinaturaCliente2" to relatorio.assinaturaCliente2,
                "assinaturaTecnico1" to relatorio.assinaturaTecnico1,
                "assinaturaTecnico2" to relatorio.assinaturaTecnico2,
                "tintaId" to relatorio.tintaId,
                "solventeId" to relatorio.solventeId,
                "codigoTinta" to relatorio.codigoTinta,
                "codigoSolvente" to relatorio.codigoSolvente,
                "dataProximaPreventiva" to relatorio.dataProximaPreventiva,
                "horasProximaPreventiva" to relatorio.horasProximaPreventiva,
                "defeitosIdentificados" to relatorio.defeitosIdentificados,
                "servicosRealizados" to relatorio.servicosRealizados,
                "observacoesDefeitosServicos" to relatorio.observacoesDefeitosServicos,
                "syncPending" to relatorio.syncPending
            )

            android.util.Log.d("RelatorioRemoteDataSource", "Mapa completo criado com ${relatorioMap.size} campos")
            android.util.Log.d("RelatorioRemoteDataSource", "valorHoraTecnica no mapa: ${relatorioMap["valorHoraTecnica"]}")

            collection.document(relatorio.id).set(relatorioMap).await()
            android.util.Log.d("RelatorioRemoteDataSource", "✅ Relatório salvo no Firebase com sucesso")
        } catch (e: Exception) {
            android.util.Log.e("RelatorioRemoteDataSource", "❌ Erro ao salvar relatório no Firebase", e)
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
