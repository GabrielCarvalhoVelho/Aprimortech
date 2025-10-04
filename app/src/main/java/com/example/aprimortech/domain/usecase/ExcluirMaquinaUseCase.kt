package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.local.entity.MaquinaEntity
import com.example.aprimortech.data.repository.MaquinaRepository

class ExcluirMaquinaUseCase(private val repository: MaquinaRepository) {
    suspend operator fun invoke(maquina: MaquinaEntity) {
        repository.deleteLocal(maquina)
        repository.deleteRemote(maquina.id)
    }
}
