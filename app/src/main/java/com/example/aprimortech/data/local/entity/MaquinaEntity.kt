package com.example.aprimortech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maquinas")
data class MaquinaEntity(
    @PrimaryKey val id: String,
    val clienteId: String, // Relacionamento com cliente
    val nomeMaquina: String, // Nome da máquina
    val fabricante: String,
    val numeroSerie: String, // ID único da máquina
    val modelo: String,
    val identificacao: String,
    val anoFabricacao: String, // Formato YYYY
    val codigoTinta: String,
    val codigoSolvente: String,
    val dataProximaPreventiva: String, // Formato yyyy-MM-dd
    val codigoConfiguracao: String = "" // Manter compatibilidade, mas deprecated
)
