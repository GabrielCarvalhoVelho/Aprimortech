package com.example.aprimortech.model

data class ContatoCliente(
    val nome: String = "",  // ✅ Adicionado valor padrão para Firebase
    val setor: String? = null,
    val celular: String? = null
)

data class Cliente(
    val id: String = "",
    val nome: String = "",
    val cnpjCpf: String = "",
    val contatos: List<ContatoCliente> = emptyList(), // Agora lista de objetos
    val endereco: String = "",
    val cidade: String = "",
    val estado: String = "",
    val telefone: String = "",
    val celular: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)
