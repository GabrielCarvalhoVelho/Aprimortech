package com.example.aprimortech.model

data class Cliente(
    val id: String = "",
    val nome: String = "",
    val cnpjCpf: String = "",
    val contatos: List<String> = emptyList(), // Mudando de contato para contatos (lista)
    val endereco: String = "",
    val cidade: String = "",
    val estado: String = "",
    val telefone: String = "",
    val celular: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)
