package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.ClienteRepository

class SincronizarClientesUseCase(private val repository: ClienteRepository) {
    suspend operator fun invoke(): Boolean = repository.sincronizarTudo()
}
