package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.PecaRepository
import com.example.aprimortech.model.Peca
import javax.inject.Inject

class SalvarPecaUseCase @Inject constructor(
    private val repository: PecaRepository
) {
    suspend operator fun invoke(peca: Peca) {
        repository.salvarPeca(peca)
    }
}
