package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.RelatorioRepository
import javax.inject.Inject

class ExcluirRelatorioUseCase @Inject constructor(
    private val relatorioRepository: RelatorioRepository
) {
    suspend operator fun invoke(id: String) = relatorioRepository.excluirRelatorio(id)
}
