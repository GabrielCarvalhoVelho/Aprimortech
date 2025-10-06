package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aprimortech.data.repository.ClienteRepository
import com.example.aprimortech.data.repository.ContatoRepository
import com.example.aprimortech.data.repository.SetorRepository

class NovoRelatorioViewModelFactory(
    private val clienteRepository: ClienteRepository,
    private val contatoRepository: ContatoRepository,
    private val setorRepository: SetorRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NovoRelatorioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NovoRelatorioViewModel(clienteRepository, contatoRepository, setorRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
