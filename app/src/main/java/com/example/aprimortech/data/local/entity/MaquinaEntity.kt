package com.example.aprimortech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maquinas")
data class MaquinaEntity(
    @PrimaryKey
    val id: String = "",
    val clienteId: String = "",
    val fabricante: String = "",
    val numeroSerie: String = "",
    val modelo: String = "",
    val identificacao: String = "",
    val anoFabricacao: String = "",
    val codigoTinta: String = "",
    val codigoSolvente: String = "",
    val dataProximaPreventiva: String = "",
    val codigoConfiguracao: String = "",
    val horasProximaPreventiva: String = ""
)
