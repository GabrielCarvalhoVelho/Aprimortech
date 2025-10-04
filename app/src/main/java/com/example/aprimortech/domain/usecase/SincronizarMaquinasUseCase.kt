package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.MaquinaRepository

class SincronizarMaquinasUseCase(private val repository: MaquinaRepository) {
    suspend operator fun invoke(): Int = repository.sincronizarDadosExistentes()
}
