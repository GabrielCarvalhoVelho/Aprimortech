package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.local.entity.MaquinaEntity
import com.example.aprimortech.data.repository.MaquinaRepository

class BuscarMaquinasUseCase(private val repository: MaquinaRepository) {
    suspend operator fun invoke(): List<MaquinaEntity> {
        // ✅ Busca direto as entities do cache local, preservando todos os campos incluindo pendenteSincronizacao
        return repository.getAllLocal()
    }
}
