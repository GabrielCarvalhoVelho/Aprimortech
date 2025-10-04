package com.example.aprimortech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pecas")
data class PecaEntity(
    @PrimaryKey val id: String,
    val nome: String,
    val codigo: String,
    val descricao: String,
    val fabricante: String,
    val categoria: String,
    val preco: Double
)
