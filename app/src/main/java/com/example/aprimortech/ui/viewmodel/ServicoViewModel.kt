package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprimortech.data.repository.ServicoRepository
import com.example.aprimortech.model.Servico
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ServicoViewModel @Inject constructor(
    private val servicoRepository: ServicoRepository
) : ViewModel() {

    private val _servicos = MutableStateFlow<List<Servico>>(emptyList())
    val servicos: StateFlow<List<Servico>> = _servicos.asStateFlow()

    private val _top5Servicos = MutableStateFlow<List<Servico>>(emptyList())
    val top5Servicos: StateFlow<List<Servico>> = _top5Servicos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        carregarServicos()
        carregarTop5Servicos()
    }

    fun carregarServicos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val listaServicos = servicoRepository.buscarServicos()
                _servicos.value = listaServicos
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar serviços: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun carregarTop5Servicos() {
        viewModelScope.launch {
            try {
                val top5 = servicoRepository.buscarTop5Servicos()
                _top5Servicos.value = top5
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar top 5 serviços: ${e.message}"
            }
        }
    }

    fun salvarServico(nomeServico: String, onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val servicoId = servicoRepository.salvarOuAtualizarServico(nomeServico)
                carregarServicos()
                carregarTop5Servicos()
                onSuccess(servicoId)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao salvar serviço: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limparErro() {
        _errorMessage.value = null
    }
}
