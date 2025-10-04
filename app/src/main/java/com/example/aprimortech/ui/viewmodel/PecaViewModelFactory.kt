package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aprimortech.domain.usecase.BuscarPecasUseCase
import com.example.aprimortech.domain.usecase.ExcluirPecaUseCase
import com.example.aprimortech.domain.usecase.SalvarPecaUseCase
import com.example.aprimortech.domain.usecase.SincronizarPecasUseCase
import com.example.aprimortech.data.repository.PecaRepository

class PecaViewModelFactory(
    private val buscarPecasUseCase: BuscarPecasUseCase,
    private val salvarPecaUseCase: SalvarPecaUseCase,
    private val excluirPecaUseCase: ExcluirPecaUseCase,
    private val sincronizarPecasUseCase: SincronizarPecasUseCase,
    private val pecaRepository: PecaRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PecaViewModel::class.java)) {
            return PecaViewModel(
                buscarPecasUseCase,
                salvarPecaUseCase,
                excluirPecaUseCase,
                sincronizarPecasUseCase,
                pecaRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
