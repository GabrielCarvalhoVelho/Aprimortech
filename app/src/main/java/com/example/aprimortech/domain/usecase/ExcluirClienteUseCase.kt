package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.ClienteRepository

/**
 * Use Case para excluir cliente
 * Remove localmente e sincroniza quando possível
 */
class ExcluirClienteUseCase(
    private val repository: ClienteRepository
) {
    suspend operator fun invoke(clienteId: String) {
        repository.excluirCliente(clienteId)
    }
}

