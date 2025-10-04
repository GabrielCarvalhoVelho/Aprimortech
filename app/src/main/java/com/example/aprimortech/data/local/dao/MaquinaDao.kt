package com.example.aprimortech.data.local.dao

import androidx.room.*
import com.example.aprimortech.data.local.entity.MaquinaEntity

@Dao
interface MaquinaDao {
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
