package com.example.aprimortech.model

/**
 * Modelo de domínio para Peça
 * Campos: código, descrição, valor unitário
 * A quantidade é controlada apenas nos relatórios
 */
data class Peca(
    val id: String = "",
    val codigo: String = "",
    val descricao: String = "",
    val valorUnitario: Double = 0.0
)
