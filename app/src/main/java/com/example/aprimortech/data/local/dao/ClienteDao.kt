package com.example.aprimortech.data.local.dao

import androidx.room.*
import com.example.aprimortech.data.local.entity.ClienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {

    /**
     * Busca todos os clientes armazenados localmente
     * Retorna um Flow para observar mudanças em tempo real
     */
    @Query("SELECT * FROM clientes ORDER BY nome ASC")
    fun observarTodosClientes(): Flow<List<ClienteEntity>>

    /**
     * Busca todos os clientes (operação única)
     */
    @Query("SELECT * FROM clientes ORDER BY nome ASC")
    suspend fun buscarTodosClientes(): List<ClienteEntity>

    /**
     * Busca cliente por ID
     */
    @Query("SELECT * FROM clientes WHERE id = :clienteId")
    suspend fun buscarClientePorId(clienteId: String): ClienteEntity?

    /**
     * Busca clientes pendentes de sincronização
     */
    @Query("SELECT * FROM clientes WHERE pendenteSincronizacao = 1")
    suspend fun buscarClientesPendentesSincronizacao(): List<ClienteEntity>

    /**
     * Conta quantos clientes estão pendentes de sincronização
     */
    @Query("SELECT COUNT(*) FROM clientes WHERE pendenteSincronizacao = 1")
    suspend fun contarClientesPendentes(): Int

    /**
     * Busca clientes por nome (pesquisa)
     */
    @Query("SELECT * FROM clientes WHERE nome LIKE '%' || :query || '%' OR cnpjCpf LIKE '%' || :query || '%' ORDER BY nome ASC")
    suspend fun buscarClientesPorNome(query: String): List<ClienteEntity>

    /**
     * Insere ou atualiza um cliente
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirCliente(cliente: ClienteEntity)

    /**
     * Insere ou atualiza múltiplos clientes (útil para sincronização)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirClientes(clientes: List<ClienteEntity>)

    /**
     * Atualiza um cliente existente
     */
    @Update
    suspend fun atualizarCliente(cliente: ClienteEntity)

    /**
     * Deleta um cliente
     */
    @Delete
    suspend fun deletarCliente(cliente: ClienteEntity)

    /**
     * Deleta cliente por ID
     */
    @Query("DELETE FROM clientes WHERE id = :clienteId")
    suspend fun deletarClientePorId(clienteId: String)

    /**
     * Marca cliente como sincronizado
     */
    @Query("UPDATE clientes SET pendenteSincronizacao = 0 WHERE id = :clienteId")
    suspend fun marcarComoSincronizado(clienteId: String)

    /**
     * Marca cliente como pendente de sincronização
     */
    @Query("UPDATE clientes SET pendenteSincronizacao = 1 WHERE id = :clienteId")
    suspend fun marcarComoPendente(clienteId: String)

    /**
     * Limpa todos os clientes (útil para resetar cache)
     */
    @Query("DELETE FROM clientes")
    suspend fun limparTodosClientes()

    /**
     * Busca clientes por cidade
     */
    @Query("SELECT * FROM clientes WHERE cidade = :cidade ORDER BY nome ASC")
    suspend fun buscarClientesPorCidade(cidade: String): List<ClienteEntity>

    /**
     * Busca clientes por estado
     */
    @Query("SELECT * FROM clientes WHERE estado = :estado ORDER BY cidade, nome ASC")
    suspend fun buscarClientesPorEstado(estado: String): List<ClienteEntity>
}

