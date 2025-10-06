package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aprimortech.domain.usecase.BuscarRelatoriosUseCase
import com.example.aprimortech.domain.usecase.SalvarRelatorioUseCase
import com.example.aprimortech.domain.usecase.ExcluirRelatorioUseCase
import com.example.aprimortech.domain.usecase.SincronizarRelatoriosUseCase
import com.example.aprimortech.domain.usecase.BuscarProximasManutencoesPreventivasUseCase
import com.example.aprimortech.data.repository.RelatorioRepository

class RelatorioViewModelFactory(
    private val buscarRelatoriosUseCase: BuscarRelatoriosUseCase,
    private val salvarRelatorioUseCase: SalvarRelatorioUseCase,
    private val excluirRelatorioUseCase: ExcluirRelatorioUseCase,
    private val sincronizarRelatoriosUseCase: SincronizarRelatoriosUseCase,
    private val buscarProximasManutencoesPreventivasUseCase: BuscarProximasManutencoesPreventivasUseCase,
    private val relatorioRepository: RelatorioRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RelatorioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RelatorioViewModel(
                buscarRelatoriosUseCase,
                salvarRelatorioUseCase,
                excluirRelatorioUseCase,
                sincronizarRelatoriosUseCase,
                buscarProximasManutencoesPreventivasUseCase,
                relatorioRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
