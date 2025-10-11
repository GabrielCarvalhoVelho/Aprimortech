package com.example.aprimortech.data.local.dao

import androidx.room.*
import com.example.aprimortech.data.local.entity.MaquinaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaquinaDao {

    /**
     * Observa todas as máquinas em tempo real
     * Retorna um Flow para observar mudanças
     */
    @Query("SELECT * FROM maquinas ORDER BY identificacao ASC")
    fun observarTodasMaquinas(): Flow<List<MaquinaEntity>>

    /**
     * Busca todas as máquinas (operação única)
     */
    @Query("SELECT * FROM maquinas ORDER BY identificacao ASC")
    suspend fun buscarTodasMaquinas(): List<MaquinaEntity>

    /**
     * Busca máquina por ID
     */
    @Query("SELECT * FROM maquinas WHERE id = :id")
    suspend fun buscarMaquinaPorId(id: String): MaquinaEntity?

    /**
     * Busca máquinas por cliente ID
     */
    @Query("SELECT * FROM maquinas WHERE clienteId = :clienteId ORDER BY identificacao ASC")
    suspend fun buscarMaquinasPorCliente(clienteId: String): List<MaquinaEntity>

    /**
     * Observa máquinas de um cliente específico
     */
    @Query("SELECT * FROM maquinas WHERE clienteId = :clienteId ORDER BY identificacao ASC")
    fun observarMaquinasPorCliente(clienteId: String): Flow<List<MaquinaEntity>>

    /**
     * Busca máquinas pendentes de sincronização
     */
    @Query("SELECT * FROM maquinas WHERE pendenteSincronizacao = 1")
    suspend fun buscarMaquinasPendentesSincronizacao(): List<MaquinaEntity>

    /**
     * Conta quantas máquinas estão pendentes de sincronização
     */
    @Query("SELECT COUNT(*) FROM maquinas WHERE pendenteSincronizacao = 1")
    suspend fun contarMaquinasPendentes(): Int

    /**
     * Busca máquinas por número de série ou identificação (pesquisa)
     */
    @Query("SELECT * FROM maquinas WHERE numeroSerie LIKE '%' || :query || '%' OR identificacao LIKE '%' || :query || '%' ORDER BY identificacao ASC")
    suspend fun buscarMaquinasPorPesquisa(query: String): List<MaquinaEntity>

    /**
     * Insere ou atualiza uma máquina
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirMaquina(maquina: MaquinaEntity)

    /**
     * Insere ou atualiza múltiplas máquinas (útil para sincronização)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirMaquinas(maquinas: List<MaquinaEntity>)

    /**
     * Atualiza uma máquina existente
     */
    @Update
    suspend fun atualizarMaquina(maquina: MaquinaEntity)

    /**
     * Deleta uma máquina
     */
    @Delete
    suspend fun deletarMaquina(maquina: MaquinaEntity)

    /**
     * Deleta máquina por ID
     */
    @Query("DELETE FROM maquinas WHERE id = :id")
    suspend fun deletarMaquinaPorId(id: String)

    /**
     * Marca máquina como sincronizada
     */
    @Query("UPDATE maquinas SET pendenteSincronizacao = 0 WHERE id = :id")
    suspend fun marcarComoSincronizado(id: String)

    /**
     * Marca máquina como pendente de sincronização
     */
    @Query("UPDATE maquinas SET pendenteSincronizacao = 1 WHERE id = :id")
    suspend fun marcarComoPendente(id: String)

    /**
     * Limpa todas as máquinas (útil para resetar cache)
     */
    @Query("DELETE FROM maquinas")
    suspend fun limparTodasMaquinas()

    /**
     * Deleta todas as máquinas de um cliente específico
     */
    @Query("DELETE FROM maquinas WHERE clienteId = :clienteId")
    suspend fun deletarMaquinasDoCliente(clienteId: String)

    // Métodos legados para compatibilidade
    @Query("SELECT * FROM maquinas")
    suspend fun getAll(): List<MaquinaEntity>

    @Query("SELECT * FROM maquinas WHERE id = :id")
    suspend fun getById(id: String): MaquinaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(maquina: MaquinaEntity)

    @Update
    suspend fun update(maquina: MaquinaEntity)

    @Delete
    suspend fun delete(maquina: MaquinaEntity)

    @Query("DELETE FROM maquinas")
    suspend fun deleteAll()
}
