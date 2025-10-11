package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.ClienteRepository
import com.example.aprimortech.model.Cliente

/**
 * Use Case para salvar cliente
 * Garante salvamento local primeiro, sincronização depois
 */
class SalvarClienteUseCase(
    private val repository: ClienteRepository
) {
    suspend operator fun invoke(cliente: Cliente): String {
        return repository.salvarCliente(cliente)
    }
}

