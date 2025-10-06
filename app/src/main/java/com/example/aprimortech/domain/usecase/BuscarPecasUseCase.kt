package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.PecaRepository
import com.example.aprimortech.model.Peca

class BuscarPecasUseCase(private val repository: PecaRepository) {
    suspend operator fun invoke(): List<Peca> = repository.buscarPecas()
}
