package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.ClienteRepository
import com.example.aprimortech.model.Cliente

/**
 * Use Case para buscar clientes
 * Prioriza cache local para operação offline
 */
class BuscarClientesUseCase(
    private val repository: ClienteRepository
) {
    suspend operator fun invoke(): List<Cliente> {
        return repository.buscarClientes()
    }
}

