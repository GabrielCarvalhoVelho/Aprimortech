package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aprimortech.data.repository.MaquinaRepository
import com.example.aprimortech.data.repository.RelatorioRepository

class DadosEquipamentoViewModelFactory(
    private val maquinaRepository: MaquinaRepository,
    private val relatorioRepository: RelatorioRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DadosEquipamentoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DadosEquipamentoViewModel(maquinaRepository, relatorioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
