package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.PecaRepository

class ExcluirPecaUseCase(private val repository: PecaRepository) {
    suspend operator fun invoke(id: String) = repository.excluirPeca(id)
}
