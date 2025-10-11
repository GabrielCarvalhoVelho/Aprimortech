package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.PecaRepository

/**
 * Use Case para sincronizar peças
 * Sincroniza dados pendentes com Firebase e atualiza cache local
 */
class SincronizarPecasUseCase(
    private val repository: PecaRepository
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
        return repository.contarPecasPendentes()
    }
}
