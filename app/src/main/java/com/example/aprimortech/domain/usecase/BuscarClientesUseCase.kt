package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.ClienteRepository
import com.example.aprimortech.model.Cliente

class BuscarClientesUseCase(private val repository: ClienteRepository) {
    suspend operator fun invoke(): List<Cliente> = repository.buscarClientes()
}
