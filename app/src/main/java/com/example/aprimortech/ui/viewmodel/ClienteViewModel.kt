package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprimortech.data.local.entity.ClienteEntity
import com.example.aprimortech.domain.usecase.BuscarClientesUseCase
import com.example.aprimortech.domain.usecase.SalvarClienteUseCase
import com.example.aprimortech.domain.usecase.ExcluirClienteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClienteViewModel(
    private val buscarClientesUseCase: BuscarClientesUseCase,
    private val salvarClienteUseCase: SalvarClienteUseCase,
    private val excluirClienteUseCase: ExcluirClienteUseCase
) : ViewModel() {
    fun excluirCliente(cliente: ClienteEntity) {
        viewModelScope.launch {
            excluirClienteUseCase(cliente)
            carregarClientes()
        }
    }
    private val _clientes = MutableStateFlow<List<ClienteEntity>>(emptyList())
    val clientes: StateFlow<List<ClienteEntity>> = _clientes.asStateFlow()

    fun carregarClientes() {
        viewModelScope.launch {
            _clientes.value = buscarClientesUseCase()
        }
    }

    fun salvarCliente(cliente: ClienteEntity) {
        viewModelScope.launch {
            salvarClienteUseCase(cliente)
            carregarClientes()
        }
    }
}
