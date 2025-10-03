package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.ClienteRepository
import com.example.aprimortech.data.local.entity.ClienteEntity

class BuscarClientesUseCase(private val repository: ClienteRepository) {
    suspend operator fun invoke(): List<ClienteEntity> {
        return repository.getAllLocal()
    }
}
