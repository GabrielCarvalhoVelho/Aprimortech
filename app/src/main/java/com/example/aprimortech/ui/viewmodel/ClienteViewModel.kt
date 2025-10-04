package com.example.aprimortech.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprimortech.data.local.entity.ClienteEntity
import com.example.aprimortech.domain.usecase.BuscarClientesUseCase
import com.example.aprimortech.domain.usecase.SalvarClienteUseCase
import com.example.aprimortech.domain.usecase.ExcluirClienteUseCase
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

    companion object {
        private const val TAG = "ClienteViewModel"
    }

    private val _clientes = MutableStateFlow<List<ClienteEntity>>(emptyList())
    val clientes: StateFlow<List<ClienteEntity>> = _clientes.asStateFlow()

    // Estado para feedback de operações
    private val _mensagemOperacao = MutableStateFlow<String?>(null)
    val mensagemOperacao: StateFlow<String?> = _mensagemOperacao.asStateFlow()

    private val _operacaoEmAndamento = MutableStateFlow(false)
    val operacaoEmAndamento: StateFlow<Boolean> = _operacaoEmAndamento.asStateFlow()

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

    fun salvarCliente(cliente: ClienteEntity, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true
                Log.d(TAG, "Salvando cliente: ${cliente.nome}")

                val sucesso = salvarClienteUseCase(cliente)

                if (sucesso) {
                    _mensagemOperacao.value = "✅ Cliente '${cliente.nome}' salvo com sucesso no Firestore!"
                    Log.d(TAG, "Cliente salvo com sucesso")
                } else {
                    // Verificar se o usuário está autenticado para dar feedback específico
                    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    if (currentUser == null) {
                        _mensagemOperacao.value = "❌ Erro: Usuário não está autenticado. Faça login novamente."
                    } else {
                        _mensagemOperacao.value = "⚠️ Cliente '${cliente.nome}' salvo localmente, mas não foi possível sincronizar com o Firestore.\n\nPossíveis causas:\n• Regras de segurança do Firestore\n• Problemas de conectividade\n\nVerifique sua conexão e as configurações do Firebase."
                    }
                    Log.w(TAG, "Problema ao salvar no Firestore")
                }

                // Recarregar a lista
                carregarClientes()
                callback(sucesso)

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao salvar cliente", e)
                _mensagemOperacao.value = "❌ Erro ao salvar cliente: ${e.message}"
                callback(false)
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    fun excluirCliente(cliente: ClienteEntity) {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true
                excluirClienteUseCase(cliente)
                _mensagemOperacao.value = "Cliente '${cliente.nome}' excluído com sucesso"
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
                Log.d(TAG, "Iniciando sincronização de dados existentes...")

                val sincronizados = sincronizarClientesUseCase()

                if (sincronizados > 0) {
                    _mensagemOperacao.value = "Sincronização concluída! $sincronizados cliente(s) enviado(s) para o Firestore."
                } else {
                    _mensagemOperacao.value = "Todos os clientes já estão sincronizados com o Firestore."
                }

                Log.d(TAG, "Sincronização concluída: $sincronizados clientes")

            } catch (e: Exception) {
                Log.e(TAG, "Erro durante sincronização", e)
                _mensagemOperacao.value = "Erro durante sincronização: ${e.message}"
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    fun limparMensagem() {
        _mensagemOperacao.value = null
    }
}
