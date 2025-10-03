package com.example.aprimortech.data.local.dao

import androidx.room.*
import com.example.aprimortech.data.local.entity.ClienteEntity

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes")
    suspend fun getAll(): List<ClienteEntity>

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun getById(id: String): ClienteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cliente: ClienteEntity)

    @Update
    suspend fun update(cliente: ClienteEntity)

    @Delete
    suspend fun delete(cliente: ClienteEntity)

    @Query("DELETE FROM clientes")
    suspend fun deleteAll()
}
