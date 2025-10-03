package com.example.aprimortech.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.aprimortech.data.local.dao.ClienteDao
import com.example.aprimortech.data.local.entity.ClienteEntity

@Database(entities = [ClienteEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clienteDao(): ClienteDao
}
