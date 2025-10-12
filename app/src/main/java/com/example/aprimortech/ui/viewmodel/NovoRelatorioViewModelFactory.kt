package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aprimortech.domain.usecase.BuscarClientesUseCase
import com.example.aprimortech.domain.usecase.SincronizarClientesUseCase
import com.example.aprimortech.data.repository.ContatoRepository
import com.example.aprimortech.data.repository.SetorRepository

class NovoRelatorioViewModelFactory(
    private val buscarClientesUseCase: BuscarClientesUseCase,
    private val sincronizarClientesUseCase: SincronizarClientesUseCase,
    private val contatoRepository: ContatoRepository,
    private val setorRepository: SetorRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NovoRelatorioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NovoRelatorioViewModel(
                buscarClientesUseCase,
                sincronizarClientesUseCase,
                contatoRepository,
                setorRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
