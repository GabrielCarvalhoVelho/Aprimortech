package com.example.aprimortech.model

data class Maquina(
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
