package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.PecaRepository
import com.example.aprimortech.model.Peca

class SalvarPecaUseCase(private val repository: PecaRepository) {
    suspend operator fun invoke(peca: Peca): String = repository.salvarPeca(peca)
}
