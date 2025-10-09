package com.example.aprimortech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.aprimortech.data.local.converters.ContatoClienteListConverter
import com.example.aprimortech.model.ContatoCliente

@Entity(tableName = "clientes")
@TypeConverters(ContatoClienteListConverter::class)
data class ClienteEntity(
    @PrimaryKey val id: String,
    val nome: String,
    val cnpjCpf: String,
    val contatos: List<ContatoCliente>, // Agora lista de objetos
    val endereco: String,
    val cidade: String,
    val estado: String,
    val telefone: String,
    val celular: String,
    val latitude: Double?,
    val longitude: Double?
)
