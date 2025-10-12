package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprimortech.domain.usecase.BuscarClientesUseCase
import com.example.aprimortech.domain.usecase.SincronizarClientesUseCase
import com.example.aprimortech.data.repository.ContatoRepository
import com.example.aprimortech.data.repository.SetorRepository
import com.example.aprimortech.model.Cliente
import com.example.aprimortech.model.Contato
import com.example.aprimortech.model.Setor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NovoRelatorioViewModel(
    private val buscarClientesUseCase: BuscarClientesUseCase,
    private val sincronizarClientesUseCase: SincronizarClientesUseCase,
    private val contatoRepository: ContatoRepository,
    private val setorRepository: SetorRepository
) : ViewModel() {

    private val _clientes = MutableStateFlow<List<Cliente>>(emptyList())
    val clientes: StateFlow<List<Cliente>> = _clientes.asStateFlow()

    private val _contatos = MutableStateFlow<List<Contato>>(emptyList())
    val contatos: StateFlow<List<Contato>> = _contatos.asStateFlow()

    private val _setores = MutableStateFlow<List<Setor>>(emptyList())
    val setores: StateFlow<List<Setor>> = _setores.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _mensagemOperacao = MutableStateFlow<String?>(null)
    val mensagemOperacao: StateFlow<String?> = _mensagemOperacao.asStateFlow()

    private val _sincronizacaoInicial = MutableStateFlow(false)
    val sincronizacaoInicial: StateFlow<Boolean> = _sincronizacaoInicial.asStateFlow()

    init {
        sincronizarDadosIniciais()
    }

    private fun sincronizarDadosIniciais() {
        viewModelScope.launch {
            try {
                _sincronizacaoInicial.value = true
                android.util.Log.d("NovoRelatorioViewModel", "🔄 Sincronizando dados iniciais...")

                // Força sincronização com Firebase
                sincronizarClientesUseCase()

                // Carrega dados do cache atualizado
                carregarClientes()

                android.util.Log.d("NovoRelatorioViewModel", "✅ Sincronização inicial concluída")
            } catch (e: Exception) {
                android.util.Log.e("NovoRelatorioViewModel", "⚠️ Erro na sincronização inicial, carregando cache", e)
                // Mesmo com erro, tenta carregar do cache
                carregarClientes()
            } finally {
                _sincronizacaoInicial.value = false
            }
        }
    }

    fun carregarClientes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                android.util.Log.d("NovoRelatorioViewModel", "📂 Carregando clientes (cache local)...")
                val clientesLocal = buscarClientesUseCase()
                _clientes.value = clientesLocal
                android.util.Log.d("NovoRelatorioViewModel", "✅ ${clientesLocal.size} clientes carregados")
                clientesLocal.forEach { cliente ->
                    android.util.Log.d("NovoRelatorioViewModel", "   - ${cliente.nome} (${cliente.cnpjCpf})")
                }
            } catch (e: Exception) {
                android.util.Log.e("NovoRelatorioViewModel", "❌ Erro ao carregar clientes", e)
                _mensagemOperacao.value = "Erro ao carregar clientes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun carregarContatosPorCliente(clienteId: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("NovoRelatorioViewModel", "Carregando contatos para cliente: $clienteId")
                val contatosCliente = contatoRepository.buscarContatosPorCliente(clienteId)
                _contatos.value = contatosCliente
                android.util.Log.d("NovoRelatorioViewModel", "Contatos carregados: ${contatosCliente.size}")
            } catch (e: Exception) {
                android.util.Log.e("NovoRelatorioViewModel", "Erro ao carregar contatos", e)
                _mensagemOperacao.value = "Erro ao carregar contatos: ${e.message}"
            }
        }
    }

    fun carregarSetoresPorCliente(clienteId: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("NovoRelatorioViewModel", "Carregando setores para cliente: $clienteId")
                val setoresCliente = setorRepository.buscarSetoresPorCliente(clienteId)
                _setores.value = setoresCliente
                android.util.Log.d("NovoRelatorioViewModel", "Setores carregados: ${setoresCliente.size}")
            } catch (e: Exception) {
                android.util.Log.e("NovoRelatorioViewModel", "Erro ao carregar setores", e)
                _mensagemOperacao.value = "Erro ao carregar setores: ${e.message}"
            }
        }
    }

    fun salvarContato(contato: Contato) {
        viewModelScope.launch {
            try {
                android.util.Log.d("NovoRelatorioViewModel", "Salvando novo contato: ${contato.nome}")
                val novoId = contatoRepository.salvarContato(contato)
                android.util.Log.d("NovoRelatorioViewModel", "Contato salvo com ID: $novoId")

                // Recarregar contatos para atualizar a lista
                carregarContatosPorCliente(contato.clienteId)
                _mensagemOperacao.value = "Contato criado com sucesso!"
            } catch (e: Exception) {
                android.util.Log.e("NovoRelatorioViewModel", "Erro ao salvar contato", e)
                _mensagemOperacao.value = "Erro ao salvar contato: ${e.message}"
            }
        }
    }

    fun salvarSetor(setor: Setor) {
        viewModelScope.launch {
            try {
                android.util.Log.d("NovoRelatorioViewModel", "Salvando novo setor: ${setor.nome}")
                val novoId = setorRepository.salvarSetor(setor)
                android.util.Log.d("NovoRelatorioViewModel", "Setor salvo com ID: $novoId")

                // Recarregar setores para atualizar a lista
                carregarSetoresPorCliente(setor.clienteId)
                _mensagemOperacao.value = "Setor criado com sucesso!"
            } catch (e: Exception) {
                android.util.Log.e("NovoRelatorioViewModel", "Erro ao salvar setor", e)
                _mensagemOperacao.value = "Erro ao salvar setor: ${e.message}"
            }
        }
    }

    fun limparMensagem() {
        _mensagemOperacao.value = null
    }
}
