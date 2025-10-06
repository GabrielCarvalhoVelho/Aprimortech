package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.RelatorioRepository
import com.example.aprimortech.model.Relatorio

class BuscarRelatoriosUseCase(private val repository: RelatorioRepository) {
    suspend operator fun invoke(): List<Relatorio> = repository.buscarTodosRelatorios()
}
