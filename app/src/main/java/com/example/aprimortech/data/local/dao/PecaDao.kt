package com.example.aprimortech.data.local.dao

import androidx.room.*
import com.example.aprimortech.data.local.entity.PecaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PecaDao {
    @Query("SELECT * FROM pecas ORDER BY nome ASC")
    fun buscarTodasPecas(): Flow<List<PecaEntity>>

    @Query("SELECT * FROM pecas WHERE id = :id")
    suspend fun buscarPecaPorId(id: String): PecaEntity?

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

    @Query("DELETE FROM pecas")
    suspend fun excluirTodasPecas()
}
