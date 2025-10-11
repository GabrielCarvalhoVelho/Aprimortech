package com.example.aprimortech.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.aprimortech.data.local.dao.ClienteDao
import com.example.aprimortech.data.local.dao.MaquinaDao
import com.example.aprimortech.data.local.dao.PecaDao
import com.example.aprimortech.data.local.dao.RelatorioDao
import com.example.aprimortech.data.local.entity.ClienteEntity
import com.example.aprimortech.data.local.entity.MaquinaEntity
import com.example.aprimortech.data.local.entity.PecaEntity
import com.example.aprimortech.data.local.entity.RelatorioEntity
import com.example.aprimortech.data.local.converters.ContatoClienteListConverter
import com.example.aprimortech.data.local.converters.StringListConverter

@Database(
    entities = [ClienteEntity::class, MaquinaEntity::class, PecaEntity::class, RelatorioEntity::class],
    version = 9,
    exportSchema = false
)
@TypeConverters(StringListConverter::class, ContatoClienteListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clienteDao(): ClienteDao
    abstract fun maquinaDao(): MaquinaDao
    abstract fun pecaDao(): PecaDao
    abstract fun relatorioDao(): RelatorioDao
}
