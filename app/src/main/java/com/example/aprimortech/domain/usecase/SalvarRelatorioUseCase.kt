package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.RelatorioRepository
import com.example.aprimortech.model.Relatorio
import javax.inject.Inject

class SalvarRelatorioUseCase @Inject constructor(
    private val relatorioRepository: RelatorioRepository
) {
    suspend operator fun invoke(relatorio: Relatorio): String = relatorioRepository.salvarRelatorio(relatorio)
}
