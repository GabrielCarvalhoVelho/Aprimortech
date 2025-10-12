package com.example.aprimortech.model

/**
 * Modelo de dados para Solvente
 * Armazena códigos de solvente usados em relatórios
 */
data class Solvente(
    val id: String = "",
    val codigo: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

