package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.local.entity.MaquinaEntity
import com.example.aprimortech.data.repository.MaquinaRepository

class SalvarMaquinaUseCase(private val repository: MaquinaRepository) {
    suspend operator fun invoke(maquina: MaquinaEntity): Boolean = repository.salvarMaquina(maquina)
}
