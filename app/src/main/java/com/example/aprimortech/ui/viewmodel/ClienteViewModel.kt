package com.example.aprimortech.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprimortech.model.Cliente
import com.example.aprimortech.domain.usecase.BuscarClientesUseCase
import com.example.aprimortech.domain.usecase.ExcluirClienteUseCase
import com.example.aprimortech.domain.usecase.SalvarClienteUseCase
import com.example.aprimortech.domain.usecase.SincronizarClientesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClienteViewModel(
    private val buscarClientesUseCase: BuscarClientesUseCase,
    private val salvarClienteUseCase: SalvarClienteUseCase,
    private val excluirClienteUseCase: ExcluirClienteUseCase,
    private val sincronizarClientesUseCase: SincronizarClientesUseCase
) : ViewModel() {

    companion object { private const val TAG = "ClienteViewModel" }

    private val _clientes = MutableStateFlow<List<Cliente>>(emptyList())
    val clientes: StateFlow<List<Cliente>> = _clientes.asStateFlow()

    private val _mensagemOperacao = MutableStateFlow<String?>(null)
    val mensagemOperacao: StateFlow<String?> = _mensagemOperacao.asStateFlow()

    private val _operacaoEmAndamento = MutableStateFlow(false)
    val operacaoEmAndamento: StateFlow<Boolean> = _operacaoEmAndamento.asStateFlow()

    init {
        carregarClientes()
    }

    fun carregarClientes() {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true
                _clientes.value = buscarClientesUseCase()
                Log.d(TAG, "Clientes carregados: ${_clientes.value.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar clientes", e)
                _mensagemOperacao.value = "Erro ao carregar clientes: ${e.message}"
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    fun salvarCliente(cliente: Cliente, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true
                Log.d(TAG, "Salvando cliente: ${cliente.nome}")

                val clienteId = salvarClienteUseCase(cliente)
                val sucesso = clienteId.isNotEmpty()

                if (sucesso) {
                    _mensagemOperacao.value = "✅ Cliente '${cliente.nome}' salvo com sucesso!"
                    Log.d(TAG, "Cliente salvo com sucesso")
                    callback(true)
                } else {
                    _mensagemOperacao.value = "❌ Erro ao salvar cliente"
                    Log.w(TAG, "Problema ao salvar cliente")
                    callback(false)
                }
                carregarClientes()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao salvar cliente", e)
                _mensagemOperacao.value = "Erro ao salvar cliente: ${e.message}"
                callback(false)
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    fun excluirCliente(clienteId: String) {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true
                excluirClienteUseCase(clienteId)
                _mensagemOperacao.value = "Cliente excluído com sucesso"
                carregarClientes()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao excluir cliente", e)
                _mensagemOperacao.value = "Erro ao excluir cliente: ${e.message}"
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    fun sincronizarDadosExistentes() {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true
                val sucesso = sincronizarClientesUseCase()
                _mensagemOperacao.value = if (sucesso) {
                    "Sincronização concluída com sucesso!"
                } else {
                    "Erro na sincronização dos clientes."
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro durante sincronização", e)
                _mensagemOperacao.value = "Erro na sincronização: ${e.message}"
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    fun limparMensagem() {
        _mensagemOperacao.value = null
    }
}
