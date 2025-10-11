package com.example.aprimortech.data.local.dao

import androidx.room.*
import com.example.aprimortech.data.local.entity.PecaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PecaDao {
    /**
     * Observa todas as peças em tempo real
     */
    @Query("SELECT * FROM pecas ORDER BY nome ASC")
    fun observarTodasPecas(): Flow<List<PecaEntity>>

    /**
     * Busca todas as peças (operação única)
     */
    @Query("SELECT * FROM pecas ORDER BY nome ASC")
    suspend fun buscarTodasPecas(): List<PecaEntity>

    @Query("SELECT * FROM pecas WHERE id = :id")
    suspend fun buscarPecaPorId(id: String): PecaEntity?

    /**
     * Busca peças pendentes de sincronização
     */
    @Query("SELECT * FROM pecas WHERE pendenteSincronizacao = 1")
    suspend fun buscarPecasPendentesSincronizacao(): List<PecaEntity>

    /**
     * Conta quantas peças estão pendentes de sincronização
     */
    @Query("SELECT COUNT(*) FROM pecas WHERE pendenteSincronizacao = 1")
    suspend fun contarPecasPendentes(): Int

    @Query("SELECT DISTINCT fabricante FROM pecas WHERE fabricante != '' ORDER BY fabricante ASC")
    fun buscarFabricantesDisponiveis(): Flow<List<String>>

    @Query("SELECT DISTINCT categoria FROM pecas WHERE categoria != '' ORDER BY categoria ASC")
    fun buscarCategoriasDisponiveis(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirPeca(peca: PecaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirPecas(pecas: List<PecaEntity>)

    @Update
    suspend fun atualizarPeca(peca: PecaEntity)

    @Delete
    suspend fun excluirPeca(peca: PecaEntity)

    /**
     * Deleta peça por ID
     */
    @Query("DELETE FROM pecas WHERE id = :id")
    suspend fun deletarPecaPorId(id: String)

    /**
     * Marca peça como sincronizada
     */
    @Query("UPDATE pecas SET pendenteSincronizacao = 0 WHERE id = :id")
    suspend fun marcarComoSincronizado(id: String)

    @Query("DELETE FROM pecas")
    suspend fun excluirTodasPecas()
}
