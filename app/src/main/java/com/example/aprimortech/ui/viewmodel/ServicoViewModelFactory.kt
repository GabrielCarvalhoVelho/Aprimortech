package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aprimortech.data.repository.ServicoRepository
import javax.inject.Inject

class ServicoViewModelFactory @Inject constructor(
    private val servicoRepository: ServicoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServicoViewModel::class.java)) {
            return ServicoViewModel(servicoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
