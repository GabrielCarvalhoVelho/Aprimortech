package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.local.entity.MaquinaEntity
import com.example.aprimortech.data.repository.MaquinaRepository

class BuscarMaquinasUseCase(private val repository: MaquinaRepository) {
    suspend operator fun invoke(): List<MaquinaEntity> = repository.getAllLocal()
}
