package com.example.aprimortech.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.aprimortech.data.local.dao.ClienteDao
import com.example.aprimortech.data.local.entity.ClienteEntity
import com.example.aprimortech.data.local.converters.StringListConverter

@Database(entities = [ClienteEntity::class], version = 2, exportSchema = false) // Incrementando versão por causa da mudança
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clienteDao(): ClienteDao
}
