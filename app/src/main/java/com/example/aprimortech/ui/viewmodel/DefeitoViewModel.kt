package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprimortech.data.repository.DefeitoRepository
import com.example.aprimortech.model.Defeito
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefeitoViewModel @Inject constructor(
    private val defeitoRepository: DefeitoRepository
) : ViewModel() {

    private val _defeitos = MutableStateFlow<List<Defeito>>(emptyList())
    val defeitos: StateFlow<List<Defeito>> = _defeitos.asStateFlow()

    private val _top5Defeitos = MutableStateFlow<List<Defeito>>(emptyList())
    val top5Defeitos: StateFlow<List<Defeito>> = _top5Defeitos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        carregarDefeitos()
        carregarTop5Defeitos()
    }

    fun carregarDefeitos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val listaDefeitos = defeitoRepository.buscarDefeitos()
                _defeitos.value = listaDefeitos
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar defeitos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun carregarTop5Defeitos() {
        viewModelScope.launch {
            try {
                val top5 = defeitoRepository.buscarTop5Defeitos()
                _top5Defeitos.value = top5
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar top 5 defeitos: ${e.message}"
            }
        }
    }

    fun salvarDefeito(nomeDefeito: String, onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val defeitoId = defeitoRepository.salvarOuAtualizarDefeito(nomeDefeito)
                carregarDefeitos()
                carregarTop5Defeitos()
                onSuccess(defeitoId)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao salvar defeito: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limparErro() {
        _errorMessage.value = null
    }
}
