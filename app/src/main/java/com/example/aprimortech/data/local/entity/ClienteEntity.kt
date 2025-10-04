package com.example.aprimortech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.aprimortech.data.local.converters.StringListConverter

@Entity(tableName = "clientes")
@TypeConverters(StringListConverter::class)
data class ClienteEntity(
    @PrimaryKey val id: String,
    val nome: String,
    val cnpjCpf: String,
    val contatos: List<String>, // Mudando para lista de contatos
    val endereco: String,
    val cidade: String,
    val estado: String,
    val telefone: String,
    val celular: String,
    val latitude: Double?,
    val longitude: Double?
)
