package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.RelatorioRepository
import com.example.aprimortech.model.Relatorio
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Use case para buscar próximas manutenções preventivas
 * ATUALIZADO: Agora busca dos RELATÓRIOS ao invés das máquinas
 */
class BuscarProximasManutencoesPreventivasUseCase @Inject constructor(
    private val relatorioRepository: RelatorioRepository
) {
    suspend operator fun invoke(diasAntecedencia: Int = 30): List<Relatorio> {
        return try {
            // TODO: Implementar método buscarRelatoriosLocal() no RelatorioRepository
            // Por enquanto retorna lista vazia
            emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
