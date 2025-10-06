package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aprimortech.data.repository.DefeitoRepository
import javax.inject.Inject

class DefeitoViewModelFactory @Inject constructor(
    private val defeitoRepository: DefeitoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DefeitoViewModel::class.java)) {
            return DefeitoViewModel(defeitoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
