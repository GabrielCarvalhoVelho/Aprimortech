package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.ClienteRepository
import com.example.aprimortech.model.Cliente

class SalvarClienteUseCase(private val repository: ClienteRepository) {
    suspend operator fun invoke(cliente: Cliente): String = repository.salvarCliente(cliente)
}
