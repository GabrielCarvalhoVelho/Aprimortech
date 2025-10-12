package com.example.aprimortech.model

/**
 * Modelo de dados para Tinta
 * Armazena códigos de tinta usados em relatórios
 */
data class Tinta(
    val id: String = "",
    val codigo: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

