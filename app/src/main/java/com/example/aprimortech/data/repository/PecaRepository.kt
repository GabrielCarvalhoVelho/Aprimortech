package com.example.aprimortech.data.repository

import android.util.Log
import com.example.aprimortech.data.local.dao.PecaDao
import com.example.aprimortech.data.local.entity.toEntity
import com.example.aprimortech.data.local.entity.toModel
import com.example.aprimortech.model.Peca
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositório de Peças com suporte completo para operação offline
 *
 * Estratégia:
 * 1. SEMPRE salva localmente primeiro (garantia de persistência offline)
 * 2. Tenta sincronizar com Firebase quando há conexão
 * 3. Marca itens como pendentes se sincronização falhar
 * 4. Lê dados locais como fonte primária
 */
@Singleton
class PecaRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val pecaDao: PecaDao
) {
    companion object {
        private const val TAG = "PecaRepository"
        private const val COLLECTION_PECAS = "pecas"
    }

    /**
     * Busca todas as peças - Prioriza cache local
     */
    suspend fun buscarPecas(): List<Peca> {
        return try {
            Log.d(TAG, "📂 Buscando peças do cache local...")
            val pecasLocais = pecaDao.buscarTodasPecas()

            Log.d(TAG, "📊 ${pecasLocais.size} peças no cache local")

            if (pecasLocais.isEmpty()) {
                Log.d(TAG, "⚠️ Cache vazio, buscando do Firebase...")
                return buscarPecasDoFirebase()
            } else {
                Log.d(TAG, "✅ Retornando ${pecasLocais.size} peças do cache local")
                pecasLocais.map { it.toModel() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao buscar peças", e)
            // Retorna cache local mesmo em caso de erro
            pecaDao.buscarTodasPecas().map { it.toModel() }
        }
    }

    /**
     * Busca peças do Firebase e atualiza cache local
     */
    private suspend fun buscarPecasDoFirebase(): List<Peca> {
        return try {
            val snapshot = firestore.collection(COLLECTION_PECAS)
                .get()
                .await()

            val pecas = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Peca::class.java)?.copy(id = doc.id)
            }

            // Atualiza cache local
            if (pecas.isNotEmpty()) {
                val entities = pecas.map { it.toEntity(pendenteSincronizacao = false) }
                pecaDao.inserirPecas(entities)
                Log.d(TAG, "✅ Cache local atualizado com ${pecas.size} peças do Firebase")
            }

            pecas
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao buscar peças do Firebase", e)
            // Retorna cache local se Firebase falhar
            pecaDao.buscarTodasPecas().map { it.toModel() }
        }
    }

    /**
     * Observa mudanças nas peças em tempo real (Flow)
     */
    fun observarPecas(): Flow<List<Peca>> {
        return pecaDao.observarTodasPecas()
            .map { entities -> entities.map { it.toModel() } }
    }

    /**
     * Busca peça por ID
     */
    suspend fun buscarPecaPorId(id: String): Peca? {
        return try {
            // Busca primeiro no cache local
            val local = pecaDao.buscarPecaPorId(id)
            if (local != null) {
                return local.toModel()
            }

            // Se não encontrar, busca no Firebase
            val doc = firestore.collection(COLLECTION_PECAS)
                .document(id)
                .get()
                .await()

            doc.toObject(Peca::class.java)?.copy(id = doc.id)?.also { peca ->
                // Salva no cache
                pecaDao.inserirPeca(peca.toEntity(pendenteSincronizacao = false))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar peça por ID", e)
            null
        }
    }

    /**
     * Salva peça com estratégia offline-first
     */
    suspend fun salvarPeca(peca: Peca): String {
        return try {
            // 1. Gera ID se necessário
            val pecaId = peca.id.ifEmpty { UUID.randomUUID().toString() }
            val pecaComId = peca.copy(id = pecaId)

            // 2. SEMPRE salva localmente primeiro
            val entity = pecaComId.toEntity(pendenteSincronizacao = true)
            pecaDao.inserirPeca(entity)
            Log.d(TAG, "✅ Peça '${peca.codigo}' salva localmente")

            // 3. Tenta sincronizar com Firebase
            try {
                firestore.collection(COLLECTION_PECAS)
                    .document(pecaId)
                    .set(pecaComId)
                    .await()

                // Marca como sincronizado
                pecaDao.marcarComoSincronizado(pecaId)
                Log.d(TAG, "✅ Peça '${peca.codigo}' sincronizada com Firebase")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Falha na sincronização com Firebase, mantido como pendente", e)
                // Peça permanece marcada como pendente de sincronização
            }

            pecaId
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao salvar peça", e)
            throw Exception("Erro ao salvar peça: ${e.message}")
        }
    }

    /**
     * Exclui peça com estratégia offline-first
     */
    suspend fun excluirPeca(id: String) {
        try {
            // 1. Remove do cache local imediatamente
            pecaDao.deletarPecaPorId(id)
            Log.d(TAG, "✅ Peça excluída do cache local")

            // 2. Tenta excluir do Firebase
            try {
                firestore.collection(COLLECTION_PECAS)
                    .document(id)
                    .delete()
                    .await()
                Log.d(TAG, "✅ Peça excluída do Firebase")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Falha ao excluir do Firebase, será removido na próxima sincronização", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao excluir peça", e)
            throw Exception("Erro ao excluir peça: ${e.message}")
        }
    }

    /**
     * Sincroniza peças pendentes com Firebase
     */
    suspend fun sincronizarPecasPendentes(): Int {
        return try {
            val pendentes = pecaDao.buscarPecasPendentesSincronizacao()
            Log.d(TAG, "Sincronizando ${pendentes.size} peças pendentes...")

            var sincronizados = 0
            pendentes.forEach { entity ->
                try {
                    firestore.collection(COLLECTION_PECAS)
                        .document(entity.id)
                        .set(entity.toModel())
                        .await()

                    pecaDao.marcarComoSincronizado(entity.id)
                    sincronizados++
                    Log.d(TAG, "✅ Peça '${entity.codigo}' sincronizada")
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Falha ao sincronizar peça '${entity.codigo}'", e)
                }
            }

            Log.d(TAG, "✅ ${sincronizados}/${pendentes.size} peças sincronizadas")
            sincronizados
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao sincronizar peças pendentes", e)
            0
        }
    }

    /**
     * Força sincronização completa com Firebase
     * Baixa todas as peças do Firebase e atualiza cache local
     */
    suspend fun sincronizarComFirebase() {
        try {
            Log.d(TAG, "Iniciando sincronização completa com Firebase...")

            // 1. Sincroniza pendentes locais para Firebase
            sincronizarPecasPendentes()

            // 2. Baixa dados atualizados do Firebase
            buscarPecasDoFirebase()

            Log.d(TAG, "✅ Sincronização completa finalizada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro na sincronização completa", e)
        }
    }

    /**
     * Retorna quantidade de peças pendentes de sincronização
     */
    suspend fun contarPecasPendentes(): Int {
        return try {
            pecaDao.contarPecasPendentes()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao contar peças pendentes", e)
            0
        }
    }
}
