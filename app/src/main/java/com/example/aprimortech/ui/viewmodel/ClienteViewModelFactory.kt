package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aprimortech.domain.usecase.BuscarClientesUseCase
import com.example.aprimortech.domain.usecase.ExcluirClienteUseCase
import com.example.aprimortech.domain.usecase.SalvarClienteUseCase
import com.example.aprimortech.domain.usecase.SincronizarClientesUseCase

class ClienteViewModelFactory(
    private val buscarClientesUseCase: BuscarClientesUseCase,
    private val salvarClienteUseCase: SalvarClienteUseCase,
    private val excluirClienteUseCase: ExcluirClienteUseCase,
    private val sincronizarClientesUseCase: SincronizarClientesUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClienteViewModel::class.java)) {
            return ClienteViewModel(
                buscarClientesUseCase,
                salvarClienteUseCase,
                excluirClienteUseCase,
                sincronizarClientesUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
