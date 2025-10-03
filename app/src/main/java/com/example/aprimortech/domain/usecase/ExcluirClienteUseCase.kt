package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.ClienteRepository
import com.example.aprimortech.data.local.entity.ClienteEntity

class ExcluirClienteUseCase(private val repository: ClienteRepository) {
    suspend operator fun invoke(cliente: ClienteEntity) {
        repository.deleteLocal(cliente)
        repository.deleteRemote(cliente.id)
    }
}
