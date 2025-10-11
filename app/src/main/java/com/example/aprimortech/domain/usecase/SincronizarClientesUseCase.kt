package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.ClienteRepository

/**
 * Use Case para sincronizar clientes
 * Sincroniza dados pendentes com Firebase e atualiza cache local
 */
class SincronizarClientesUseCase(
    private val repository: ClienteRepository
) {
    /**
     * Sincroniza todos os dados
     */
    suspend operator fun invoke() {
        repository.sincronizarComFirebase()
    }

    /**
     * Retorna quantidade de itens pendentes de sincronização
     */
    suspend fun contarPendentes(): Int {
        return repository.contarClientesPendentes()
    }
}

