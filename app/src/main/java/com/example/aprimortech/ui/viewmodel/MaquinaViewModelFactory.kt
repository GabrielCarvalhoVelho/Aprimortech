package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aprimortech.domain.usecase.BuscarMaquinasUseCase
import com.example.aprimortech.domain.usecase.ExcluirMaquinaUseCase
import com.example.aprimortech.domain.usecase.SalvarMaquinaUseCase
import com.example.aprimortech.domain.usecase.SincronizarMaquinasUseCase
import com.example.aprimortech.domain.usecase.BuscarClientesUseCase

class MaquinaViewModelFactory(
    private val buscarMaquinasUseCase: BuscarMaquinasUseCase,
    private val salvarMaquinaUseCase: SalvarMaquinaUseCase,
    private val excluirMaquinaUseCase: ExcluirMaquinaUseCase,
    private val sincronizarMaquinasUseCase: SincronizarMaquinasUseCase,
    private val buscarClientesUseCase: BuscarClientesUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MaquinaViewModel::class.java)) {
            return MaquinaViewModel(
                buscarMaquinasUseCase,
                salvarMaquinaUseCase,
                excluirMaquinaUseCase,
                sincronizarMaquinasUseCase,
                buscarClientesUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
