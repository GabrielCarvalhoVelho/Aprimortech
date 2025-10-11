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

    private val _itensPendentesSincronizacao = MutableStateFlow(0)
    val itensPendentesSincronizacao: StateFlow<Int> = _itensPendentesSincronizacao.asStateFlow()

    private val _sincronizacaoInicial = MutableStateFlow(false)
    val sincronizacaoInicial: StateFlow<Boolean> = _sincronizacaoInicial.asStateFlow()

    init {
        sincronizarDadosIniciais()
    }

    fun carregarClientes() {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true
                _clientes.value = buscarClientesUseCase()
                Log.d(TAG, "✅ ${_clientes.value.size} clientes carregados (cache local)")
                verificarItensPendentes()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao carregar clientes", e)
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
                Log.d(TAG, "💾 Salvando cliente: ${cliente.nome}")

                val clienteId = salvarClienteUseCase(cliente)
                val sucesso = clienteId.isNotEmpty()

                if (sucesso) {
                    _mensagemOperacao.value = "✅ Cliente '${cliente.nome}' salvo!"
                    Log.d(TAG, "✅ Cliente salvo com sucesso (ID: $clienteId)")
                    callback(true)
                } else {
                    _mensagemOperacao.value = "❌ Erro ao salvar cliente"
                    Log.w(TAG, "⚠️ Problema ao salvar cliente")
                    callback(false)
                }
                carregarClientes()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao salvar cliente", e)
                _mensagemOperacao.value = "Erro: ${e.message}"
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
                _mensagemOperacao.value = "✅ Cliente excluído"
                Log.d(TAG, "✅ Cliente excluído com sucesso")
                carregarClientes()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao excluir cliente", e)
                _mensagemOperacao.value = "Erro ao excluir: ${e.message}"
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    fun sincronizarDados() {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true
                _mensagemOperacao.value = "🔄 Sincronizando..."

                sincronizarClientesUseCase()

                _mensagemOperacao.value = "✅ Sincronização concluída!"
                Log.d(TAG, "✅ Sincronização concluída")

                carregarClientes()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro na sincronização", e)
                _mensagemOperacao.value = "⚠️ Erro na sincronização: ${e.message}"
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    fun verificarItensPendentes() {
        viewModelScope.launch {
            try {
                _itensPendentesSincronizacao.value = sincronizarClientesUseCase.contarPendentes()
                if (_itensPendentesSincronizacao.value > 0) {
                    Log.d(TAG, "⚠️ ${_itensPendentesSincronizacao.value} clientes pendentes de sincronização")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao verificar itens pendentes", e)
            }
        }
    }

    fun limparMensagem() {
        _mensagemOperacao.value = null
    }

    private fun sincronizarDadosIniciais() {
        viewModelScope.launch {
            try {
                _sincronizacaoInicial.value = true
                Log.d(TAG, "🔄 Iniciando sincronização inicial com Firebase...")

                // Força sincronização completa (baixa todos os dados do Firebase)
                sincronizarClientesUseCase()

                Log.d(TAG, "✅ Sincronização inicial concluída")

                // Recarrega os dados do cache atualizado
                carregarClientes()
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Erro na sincronização inicial (modo offline?)", e)
                // Mesmo com erro, tenta carregar o que tem no cache
                carregarClientes()
            } finally {
                _sincronizacaoInicial.value = false
            }
        }
    }
}
