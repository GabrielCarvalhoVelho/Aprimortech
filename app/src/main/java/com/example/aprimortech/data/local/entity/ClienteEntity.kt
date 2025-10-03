package com.example.aprimortech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey val id: String,
    val nome: String,
    val cnpjCpf: String,
    val contato: String,
    val endereco: String,
    val cidade: String,
    val estado: String,
    val telefone: String,
    val celular: String,
    val latitude: Double?,
    val longitude: Double?
)
