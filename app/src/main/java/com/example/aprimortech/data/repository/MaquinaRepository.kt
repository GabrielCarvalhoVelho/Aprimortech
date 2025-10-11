package com.example.aprimortech.data.repository

import android.util.Log
import com.example.aprimortech.data.local.dao.MaquinaDao
import com.example.aprimortech.data.local.entity.MaquinaEntity
import com.example.aprimortech.data.local.entity.toEntity
import com.example.aprimortech.data.local.entity.toModel
import com.example.aprimortech.model.Maquina
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositório de Máquinas com suporte completo para operação offline-first
 *
 * Estratégia:
 * 1. SEMPRE salva localmente primeiro (garantia de persistência offline)
 * 2. Tenta sincronizar com Firebase quando há conexão
 * 3. Marca itens como pendentes se sincronização falhar
 * 4. Lê dados locais como fonte primária
 */
@Singleton
class MaquinaRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val maquinaDao: MaquinaDao
) {
    companion object {
        private const val TAG = "MaquinaRepository"
        private const val COLLECTION_MAQUINAS = "maquinas"
    }

    /**
     * Busca todas as máquinas - Prioriza cache local
     */
    suspend fun buscarMaquinas(): List<Maquina> {
        return try {
            Log.d(TAG, "📂 Buscando máquinas do cache local...")
            val maquinasLocais = maquinaDao.buscarTodasMaquinas()

            Log.d(TAG, "📊 ${maquinasLocais.size} máquinas no cache local")

            if (maquinasLocais.isEmpty()) {
                Log.d(TAG, "⚠️ Cache vazio, buscando do Firebase...")
                return buscarMaquinasDoFirebase()
            } else {
                Log.d(TAG, "✅ Retornando ${maquinasLocais.size} máquinas do cache local")
                maquinasLocais.map { it.toModel() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao buscar máquinas", e)
            // Retorna cache local mesmo em caso de erro
            maquinaDao.buscarTodasMaquinas().map { it.toModel() }
        }
    }

    /**
     * Busca máquinas do Firebase e atualiza cache local
     */
    private suspend fun buscarMaquinasDoFirebase(): List<Maquina> {
        return try {
            val snapshot = firestore.collection(COLLECTION_MAQUINAS)
                .get()
                .await()

            val maquinas = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Maquina::class.java)?.copy(id = doc.id)
            }

            // Atualiza cache local
            if (maquinas.isNotEmpty()) {
                val entities = maquinas.map { it.toEntity(pendenteSincronizacao = false) }
                maquinaDao.inserirMaquinas(entities)
                Log.d(TAG, "✅ Cache local atualizado com ${maquinas.size} máquinas do Firebase")
            }

            maquinas
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao buscar máquinas do Firebase", e)
            // Retorna cache local se Firebase falhar
            maquinaDao.buscarTodasMaquinas().map { it.toModel() }
        }
    }

    /**
     * Observa mudanças nas máquinas em tempo real (Flow)
     */
    fun observarMaquinas(): Flow<List<Maquina>> {
        return maquinaDao.observarTodasMaquinas()
            .map { entities -> entities.map { it.toModel() } }
    }

    /**
     * Busca máquinas por cliente ID - Offline-first
     */
    suspend fun buscarMaquinasPorCliente(clienteId: String): List<Maquina> {
        return try {
            Log.d(TAG, "📂 Buscando máquinas do cliente $clienteId do cache local...")
            val maquinasLocais = maquinaDao.buscarMaquinasPorCliente(clienteId)

            if (maquinasLocais.isEmpty()) {
                Log.d(TAG, "⚠️ Cache vazio, buscando do Firebase...")
                return buscarMaquinasPorClienteDoFirebase(clienteId)
            } else {
                Log.d(TAG, "✅ ${maquinasLocais.size} máquinas encontradas no cache")
                maquinasLocais.map { it.toModel() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao buscar máquinas do cliente", e)
            maquinaDao.buscarMaquinasPorCliente(clienteId).map { it.toModel() }
        }
    }

    /**
     * Busca máquinas de um cliente do Firebase
     */
    private suspend fun buscarMaquinasPorClienteDoFirebase(clienteId: String): List<Maquina> {
        return try {
            val snapshot = firestore.collection(COLLECTION_MAQUINAS)
                .whereEqualTo("clienteId", clienteId)
                .get()
                .await()

            val maquinas = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Maquina::class.java)?.copy(id = doc.id)
            }

            // Atualiza cache local
            if (maquinas.isNotEmpty()) {
                val entities = maquinas.map { it.toEntity(pendenteSincronizacao = false) }
                maquinaDao.inserirMaquinas(entities)
                Log.d(TAG, "✅ Cache atualizado com ${maquinas.size} máquinas do cliente")
            }

            maquinas
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao buscar máquinas do Firebase", e)
            maquinaDao.buscarMaquinasPorCliente(clienteId).map { it.toModel() }
        }
    }

    /**
     * Observa máquinas de um cliente específico em tempo real
     */
    fun observarMaquinasPorCliente(clienteId: String): Flow<List<Maquina>> {
        return maquinaDao.observarMaquinasPorCliente(clienteId)
            .map { entities -> entities.map { it.toModel() } }
    }

    /**
     * Salva máquina com estratégia offline-first
     */
    suspend fun salvarMaquina(maquina: Maquina): String {
        return try {
            // 1. Gera ID se necessário
            val maquinaId = maquina.id.ifEmpty { UUID.randomUUID().toString() }
            val maquinaComId = maquina.copy(id = maquinaId)

            // 2. SEMPRE salva localmente primeiro
            val entity = maquinaComId.toEntity(pendenteSincronizacao = true)
            maquinaDao.inserirMaquina(entity)
            Log.d(TAG, "✅ Máquina '${maquina.identificacao}' salva localmente")

            // 3. Tenta sincronizar com Firebase
            try {
                firestore.collection(COLLECTION_MAQUINAS)
                    .document(maquinaId)
                    .set(maquinaComId)
                    .await()

                // Marca como sincronizado
                maquinaDao.marcarComoSincronizado(maquinaId)
                Log.d(TAG, "✅ Máquina '${maquina.identificacao}' sincronizada com Firebase")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Falha na sincronização com Firebase, mantido como pendente", e)
                // Máquina permanece marcada como pendente de sincronização
            }

            maquinaId
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao salvar máquina", e)
            throw e
        }
    }

    /**
     * Exclui máquina com estratégia offline-first
     */
    suspend fun excluirMaquina(maquinaId: String) {
        try {
            // 1. Remove do cache local imediatamente
            maquinaDao.deletarMaquinaPorId(maquinaId)
            Log.d(TAG, "✅ Máquina excluída do cache local")

            // 2. Tenta excluir do Firebase
            try {
                firestore.collection(COLLECTION_MAQUINAS)
                    .document(maquinaId)
                    .delete()
                    .await()
                Log.d(TAG, "✅ Máquina excluída do Firebase")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Falha ao excluir do Firebase, será removido na próxima sincronização", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao excluir máquina", e)
            throw e
        }
    }

    /**
     * Busca máquina por ID
     */
    suspend fun buscarMaquinaPorId(maquinaId: String): Maquina? {
        return try {
            // Busca primeiro no cache local
            val local = maquinaDao.buscarMaquinaPorId(maquinaId)
            if (local != null) {
                return local.toModel()
            }

            // Se não encontrar, busca no Firebase
            val doc = firestore.collection(COLLECTION_MAQUINAS)
                .document(maquinaId)
                .get()
                .await()

            doc.toObject(Maquina::class.java)?.copy(id = doc.id)?.also { maquina ->
                // Salva no cache
                maquinaDao.inserirMaquina(maquina.toEntity(pendenteSincronizacao = false))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar máquina por ID", e)
            null
        }
    }

    /**
     * Sincroniza máquinas pendentes com Firebase
     */
    suspend fun sincronizarMaquinasPendentes(): Int {
        return try {
            val pendentes = maquinaDao.buscarMaquinasPendentesSincronizacao()
            Log.d(TAG, "Sincronizando ${pendentes.size} máquinas pendentes...")

            var sincronizados = 0
            pendentes.forEach { entity ->
                try {
                    firestore.collection(COLLECTION_MAQUINAS)
                        .document(entity.id)
                        .set(entity.toModel())
                        .await()

                    maquinaDao.marcarComoSincronizado(entity.id)
                    sincronizados++
                    Log.d(TAG, "✅ Máquina '${entity.identificacao}' sincronizada")
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Falha ao sincronizar máquina '${entity.identificacao}'", e)
                }
            }

            Log.d(TAG, "✅ ${sincronizados}/${pendentes.size} máquinas sincronizadas")
            sincronizados
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao sincronizar máquinas pendentes", e)
            0
        }
    }

    /**
     * Força sincronização completa com Firebase
     * Baixa todas as máquinas do Firebase e atualiza cache local
     */
    suspend fun sincronizarComFirebase() {
        try {
            Log.d(TAG, "Iniciando sincronização completa com Firebase...")

            // 1. Sincroniza pendentes locais para Firebase
            sincronizarMaquinasPendentes()

            // 2. Baixa dados atualizados do Firebase
            buscarMaquinasDoFirebase()

            Log.d(TAG, "✅ Sincronização completa finalizada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro na sincronização completa", e)
        }
    }

    /**
     * Retorna quantidade de máquinas pendentes de sincronização
     */
    suspend fun contarMaquinasPendentes(): Int {
        return try {
            maquinaDao.contarMaquinasPendentes()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao contar máquinas pendentes", e)
            0
        }
    }

    /**
     * Pesquisa máquinas por número de série ou identificação
     */
    suspend fun pesquisarMaquinas(query: String): List<Maquina> {
        return try {
            maquinaDao.buscarMaquinasPorPesquisa(query).map { it.toModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao pesquisar máquinas", e)
            emptyList()
        }
    }

    /**
     * Limpa cache local (use com cuidado!)
     */
    suspend fun limparCacheLocal() {
        try {
            maquinaDao.limparTodasMaquinas()
            Log.d(TAG, "Cache local limpo")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar cache", e)
        }
    }

    // ========== MÉTODOS LEGADOS PARA COMPATIBILIDADE ==========

    suspend fun getAllLocal(): List<MaquinaEntity> {
        // ✅ Busca do cache local
        val localMaquinas = maquinaDao.buscarTodasMaquinas()

        // ✅ Tenta sincronizar em background (não bloqueia a resposta)
        try {
            if (localMaquinas.isEmpty()) {
                // Se cache vazio, busca do Firebase
                val maquinasFirebase = buscarMaquinasDoFirebase()
                return maquinasFirebase.map { it.toEntity(pendenteSincronizacao = false) }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Cache local será usado (Firebase indisponível)")
        }

        return localMaquinas
    }

    suspend fun salvarMaquina(maquina: MaquinaEntity): Boolean {
        return try {
            // Converte para domain model
            val maquinaDomain = maquina.toModel()

            // 1. Gera ID se necessário
            val maquinaId = if (maquinaDomain.id.isEmpty()) {
                UUID.randomUUID().toString()
            } else {
                maquinaDomain.id
            }
            val maquinaComId = maquinaDomain.copy(id = maquinaId)

            // 2. SEMPRE salva localmente primeiro
            val entity = maquinaComId.toEntity(pendenteSincronizacao = true)
            maquinaDao.inserirMaquina(entity)
            Log.d(TAG, "✅ Máquina '${maquinaComId.identificacao}' salva localmente")

            // 3. Tenta sincronizar com Firebase
            try {
                firestore.collection(COLLECTION_MAQUINAS)
                    .document(maquinaId)
                    .set(maquinaComId)
                    .await()

                // Marca como sincronizado
                maquinaDao.marcarComoSincronizado(maquinaId)
                Log.d(TAG, "✅ Máquina '${maquinaComId.identificacao}' sincronizada com Firebase")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Falha na sincronização com Firebase, mantido como pendente", e)
                // Máquina permanece marcada como pendente de sincronização
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao salvar máquina", e)
            false
        }
    }

    suspend fun excluirMaquina(maquina: MaquinaEntity): Boolean {
        return try {
            excluirMaquina(maquina.id)
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao excluir máquina", e)
            false
        }
    }

    suspend fun buscarMaquinasLocal(): List<Maquina> {
        return buscarMaquinas()
    }
}
