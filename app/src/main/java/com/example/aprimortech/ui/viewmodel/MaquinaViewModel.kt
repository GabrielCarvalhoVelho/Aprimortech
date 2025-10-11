package com.example.aprimortech.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprimortech.data.local.entity.MaquinaEntity
import com.example.aprimortech.model.Cliente
import com.example.aprimortech.domain.usecase.BuscarMaquinasUseCase
import com.example.aprimortech.domain.usecase.ExcluirMaquinaUseCase
import com.example.aprimortech.domain.usecase.SalvarMaquinaUseCase
import com.example.aprimortech.domain.usecase.SincronizarMaquinasUseCase
import com.example.aprimortech.domain.usecase.BuscarClientesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MaquinaViewModel(
    private val buscarMaquinasUseCase: BuscarMaquinasUseCase,
    private val salvarMaquinaUseCase: SalvarMaquinaUseCase,
    private val excluirMaquinaUseCase: ExcluirMaquinaUseCase,
    private val sincronizarMaquinasUseCase: SincronizarMaquinasUseCase,
    private val buscarClientesUseCase: BuscarClientesUseCase
) : ViewModel() {

    companion object { private const val TAG = "MaquinaViewModel" }

    private val _maquinas = MutableStateFlow<List<MaquinaEntity>>(emptyList())
    val maquinas: StateFlow<List<MaquinaEntity>> = _maquinas.asStateFlow()

    private val _clientes = MutableStateFlow<List<Cliente>>(emptyList())
    val clientes: StateFlow<List<Cliente>> = _clientes.asStateFlow()

    private val _fabricantesDisponiveis = MutableStateFlow<List<String>>(emptyList())
    val fabricantesDisponiveis: StateFlow<List<String>> = _fabricantesDisponiveis.asStateFlow()

    private val _modelosDisponiveis = MutableStateFlow<List<String>>(emptyList())
    val modelosDisponiveis: StateFlow<List<String>> = _modelosDisponiveis.asStateFlow()

    private val _codigosTintaDisponiveis = MutableStateFlow<List<String>>(emptyList())
    val codigosTintaDisponiveis: StateFlow<List<String>> = _codigosTintaDisponiveis.asStateFlow()

    private val _codigosSolventeDisponiveis = MutableStateFlow<List<String>>(emptyList())
    val codigosSolventeDisponiveis: StateFlow<List<String>> = _codigosSolventeDisponiveis.asStateFlow()

    private val _mensagemOperacao = MutableStateFlow<String?>(null)
    val mensagemOperacao: StateFlow<String?> = _mensagemOperacao.asStateFlow()

    private val _operacaoEmAndamento = MutableStateFlow(false)
    val operacaoEmAndamento: StateFlow<Boolean> = _operacaoEmAndamento.asStateFlow()

    fun carregarTodosDados() {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true
                Log.d(TAG, "Iniciando carregamento de dados...")

                _maquinas.value = buscarMaquinasUseCase()
                Log.d(TAG, "Máquinas carregadas: ${_maquinas.value.size}")

                _clientes.value = buscarClientesUseCase()
                Log.d(TAG, "Clientes carregados: ${_clientes.value.size}")
                _clientes.value.forEach { cliente ->
                    Log.d(TAG, "Cliente: id=${cliente.id}, nome=${cliente.nome}")
                }

                carregarAutocompleteData()
                Log.d(TAG, "Dados carregados: ${_maquinas.value.size} máquinas, ${_clientes.value.size} clientes")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar dados", e)
                _mensagemOperacao.value = "Erro ao carregar dados: ${e.message}"
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    private suspend fun carregarAutocompleteData() {
        // Estes métodos precisarão ser adicionados aos use cases
        // Por agora, vou simular com dados das máquinas já carregadas
        val maquinasAtuais = _maquinas.value
        _fabricantesDisponiveis.value = maquinasAtuais.map { it.fabricante }.filter { it.isNotBlank() }.distinct().sorted()
        _modelosDisponiveis.value = maquinasAtuais.map { it.modelo }.filter { it.isNotBlank() }.distinct().sorted()
        _codigosTintaDisponiveis.value = maquinasAtuais.map { it.codigoTinta }.filter { it.isNotBlank() }.distinct().sorted()
        _codigosSolventeDisponiveis.value = maquinasAtuais.map { it.codigoSolvente }.filter { it.isNotBlank() }.distinct().sorted()
    }

    fun salvarMaquina(maquina: MaquinaEntity) {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true

                // Validar número de série único
                val numeroSerieExiste = _maquinas.value.any {
                    it.numeroSerie.equals(maquina.numeroSerie, ignoreCase = true) && it.id != maquina.id
                }

                if (numeroSerieExiste) {
                    _mensagemOperacao.value = "❌ Número de série '${maquina.numeroSerie}' já está em uso!"
                    return@launch
                }

                val sucesso = salvarMaquinaUseCase(maquina)
                if (sucesso) {
                    _mensagemOperacao.value = "✅ Máquina '${maquina.identificacao}' salva com sucesso!"
                } else {
                    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    _mensagemOperacao.value = if (currentUser == null) {
                        "❌ Usuário não autenticado. Faça login novamente."
                    } else {
                        "⚠️ Máquina salva localmente, mas não sincronizada com o Firestore."
                    }
                }
                carregarTodosDados()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao salvar máquina", e)
                _mensagemOperacao.value = "❌ Erro ao salvar máquina: ${e.message}"
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    fun excluirMaquina(maquina: MaquinaEntity) {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true
                excluirMaquinaUseCase(maquina)
                _mensagemOperacao.value = "Máquina '${maquina.identificacao}' excluída"
                carregarTodosDados()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao excluir máquina", e)
                _mensagemOperacao.value = "Erro ao excluir máquina: ${e.message}"
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    fun sincronizarDadosExistentes() {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true
                val sucesso = sincronizarMaquinasUseCase()
                _mensagemOperacao.value = if (sucesso) {
                    "Sincronização concluída com sucesso!"
                } else {
                    "Erro na sincronização das máquinas."
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro durante sincronização", e)
                _mensagemOperacao.value = "Erro na sincronização: ${e.message}"
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    fun limparMensagem() { _mensagemOperacao.value = null }
}
