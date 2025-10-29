package com.example.aprimortech.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.aprimortech.model.Relatorio
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RelatorioRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("relatorios")
    private val storage = FirebaseStorage.getInstance()

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
            val result = snapshot.documents.mapNotNull { document ->
                document.toObject(Relatorio::class.java)?.copy(id = document.id)
            }
            android.util.Log.d("RelatorioRepository", "buscarRelatoriosPorCliente($clienteId) retornou ${result.size} relatórios")
            result
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
            val result = snapshot.documents.mapNotNull { document ->
                document.toObject(Relatorio::class.java)?.copy(id = document.id)
            }
            android.util.Log.d("RelatorioRepository", "buscarRelatoriosPorMaquina($maquinaId) retornou ${result.size} relatórios")
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun salvarRelatorio(relatorio: Relatorio): String {
        return try {
            android.util.Log.d("RelatorioRepository", "=== SALVANDO RELATÓRIO ===")
            android.util.Log.d("RelatorioRepository", "ID: ${relatorio.id}")
            android.util.Log.d("RelatorioRepository", "⭐ Valor Hora Técnica: ${relatorio.valorHoraTecnica}")

            // Helper: upload imagens Base64 para Storage e retornar lista de URLs (ou manter as que já são URL/gs://)
            suspend fun uploadFotosIfNeeded(targetDocId: String, fotos: List<String>): List<String> {
                if (fotos.isEmpty()) return fotos
                val uploaded = mutableListOf<String>()
                fotos.forEachIndexed { idx, item ->
                    try {
                        val cleaned = item.substringAfter("base64,\"").replace("\n", "").replace("\r", "").trim()
                        // If item already looks like URL (http/https) or gs://, keep it
                        if (item.startsWith("http://") || item.startsWith("https://")) {
                            uploaded.add(item)
                            return@forEachIndexed
                        }

                        if (item.startsWith("gs://")) {
                            // try to resolve gs:// to a public download URL via Firebase Storage
                            try {
                                val refFromGs = storage.getReferenceFromUrl(item)
                                val resolved = refFromGs.downloadUrl.await().toString()
                                uploaded.add(resolved)
                                return@forEachIndexed
                            } catch (ex: Exception) {
                                // if resolution fails, keep original gs:// reference
                                uploaded.add(item)
                                return@forEachIndexed
                            }
                        }

                        // Decode base64 (try common flags)
                        var decodedBytes: ByteArray? = null
                        val flags = listOf(Base64.DEFAULT, Base64.NO_WRAP, Base64.URL_SAFE, Base64.NO_PADDING or Base64.NO_WRAP)
                        for (f in flags) {
                            try {
                                val db = Base64.decode(cleaned, f)
                                if (db.isNotEmpty()) { decodedBytes = db; break }
                            } catch (_: Exception) { /* try next */ }
                        }

                        if (decodedBytes == null || decodedBytes.isEmpty()) {
                            // Try raw string decode without header
                            try {
                                val db = Base64.decode(item.replace("\\s".toRegex(), ""), Base64.DEFAULT)
                                if (db.isNotEmpty()) decodedBytes = db
                            } catch (_: Exception) { }
                        }

                        if (decodedBytes == null) {
                            // cannot decode, skip
                            uploaded.add(item)
                            return@forEachIndexed
                        }

                        // Optionally compress image to JPEG to reduce size
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        val outStream = ByteArrayOutputStream()
                        val quality = 80
                        if (bitmap != null) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream)
                        } else {
                            // if decode to bitmap fails, upload raw bytes
                            outStream.write(decodedBytes)
                        }
                        val finalBytes = outStream.toByteArray()

                        // Build storage path: relatorios/<docId>/fotos/<uuid>.jpg
                        val fileName = "foto_${idx}_${UUID.randomUUID()}.jpg"
                        val path = "relatorios/$targetDocId/fotos/$fileName"
                        val ref = storage.reference.child(path)

                        val metadata = StorageMetadata.Builder()
                            .setContentType("image/jpeg")
                            .build()

                        // upload
                        ref.putBytes(finalBytes, metadata).await()
                        val downloadUrl = ref.downloadUrl.await().toString()
                        uploaded.add(downloadUrl)
                    } catch (ex: Exception) {
                        android.util.Log.w("RelatorioRepository", "Falha ao fazer upload da foto index=$idx: ${ex.message}", ex)
                        // fallback: keep original item
                        uploaded.add(item)
                    }
                }
                return uploaded
            }

            if (relatorio.id.isEmpty()) {
                // Novo relatório: criar documento com ID conhecido para organizar Storage
                val newDocRef = collection.document()
                val newId = newDocRef.id

                // First, upload fotos if any (the function is suspend)
                val fotosUploaded = uploadFotosIfNeeded(newId, relatorio.equipamentoFotos)

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
                    "valorDeslocamentoTotal" to relatorio.valorDeslocamentoPorKm,
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
                    "pecasUtilizadas" to relatorio.pecasUtilizadas, // ⭐ NOVO CAMPO
                    "equipamentoFotos" to fotosUploaded, // fotos já uploaded (HTTP URLs quando possível)
                    "syncPending" to relatorio.syncPending
                )

                newDocRef.set(relatorioMap).await()
                android.util.Log.d("RelatorioRepository", "✅ Relatório criado com ID: $newId")
                newId
            } else {
                // Atualizar relatório existente
                android.util.Log.d("RelatorioRepository", "Atualizando relatório existente: ${relatorio.id}")

                // Upload fotos (keeping existing URLs)
                val fotosUploaded = uploadFotosIfNeeded(relatorio.id, relatorio.equipamentoFotos)

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
                    "pecasUtilizadas" to relatorio.pecasUtilizadas, // ⭐ NOVO CAMPO
                    "equipamentoFotos" to fotosUploaded, // fotos já uploaded (HTTP URLs quando possível)
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
