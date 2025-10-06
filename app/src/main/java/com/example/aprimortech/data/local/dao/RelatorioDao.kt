package com.example.aprimortech.data.local.dao

import androidx.room.*
import com.example.aprimortech.data.local.entity.RelatorioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RelatorioDao {
    @Query("SELECT * FROM relatorios ORDER BY date(dataRelatorio) DESC")
    fun buscarTodosRelatorios(): Flow<List<RelatorioEntity>>

    @Query("SELECT * FROM relatorios WHERE id = :id")
    suspend fun buscarRelatorioPorId(id: String): RelatorioEntity?

    @Query("SELECT * FROM relatorios WHERE clienteId = :clienteId ORDER BY date(dataRelatorio) DESC")
    fun buscarRelatoriosPorCliente(clienteId: String): Flow<List<RelatorioEntity>>

    @Query("SELECT * FROM relatorios WHERE maquinaId = :maquinaId ORDER BY date(dataRelatorio) DESC")
    fun buscarRelatoriosPorMaquina(maquinaId: String): Flow<List<RelatorioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirRelatorio(relatorio: RelatorioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirRelatorios(relatorios: List<RelatorioEntity>)

    @Update
    suspend fun atualizarRelatorio(relatorio: RelatorioEntity)

    @Delete
    suspend fun excluirRelatorio(relatorio: RelatorioEntity)

    @Query("DELETE FROM relatorios")
    suspend fun excluirTodosRelatorios()

    @Query("SELECT * FROM relatorios WHERE syncPending = 1")
    suspend fun buscarRelatoriosPendentesSync(): List<RelatorioEntity>
}
