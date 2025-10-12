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

    // ✅ NOVO: Controle de itens pendentes de sincronização
    private val _itensPendentesSincronizacao = MutableStateFlow(0)
    val itensPendentesSincronizacao: StateFlow<Int> = _itensPendentesSincronizacao.asStateFlow()

    private val _sincronizacaoInicial = MutableStateFlow(false)
    val sincronizacaoInicial: StateFlow<Boolean> = _sincronizacaoInicial.asStateFlow()

    init {
        sincronizarDadosIniciais()
    }

    fun carregarTodosDados() {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true
                Log.d(TAG, "Iniciando carregamento de dados...")

                _maquinas.value = buscarMaquinasUseCase()
                Log.d(TAG, "Máquinas carregadas: ${_maquinas.value.size}")

                _clientes.value = buscarClientesUseCase()
                Log.d(TAG, "Clientes carregados: ${_clientes.value.size}")

                carregarAutocompleteData()

                // ✅ Verifica itens pendentes
                verificarItensPendentes()

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
        val maquinasAtuais = _maquinas.value
        _fabricantesDisponiveis.value = maquinasAtuais.map { it.fabricante }.filter { it.isNotBlank() }.distinct().sorted()
        _modelosDisponiveis.value = maquinasAtuais.map { it.modelo }.filter { it.isNotBlank() }.distinct().sorted()
        // ⚠️ REMOVIDOS: codigoTinta e codigoSolvente não fazem mais parte da máquina
        _codigosTintaDisponiveis.value = emptyList()
        _codigosSolventeDisponiveis.value = emptyList()
    }

    // ✅ NOVO: Verifica quantidade de itens pendentes
    private suspend fun verificarItensPendentes() {
        try {
            val pendentes = _maquinas.value.count { it.pendenteSincronizacao }
            _itensPendentesSincronizacao.value = pendentes
            if (pendentes > 0) {
                Log.d(TAG, "⚠️ $pendentes máquina(s) pendente(s) de sincronização")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar itens pendentes", e)
        }
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

                // ✅ RECARREGA IMEDIATAMENTE para mostrar a máquina na lista
                carregarTodosDados()

                if (sucesso) {
                    // Verifica se foi realmente sincronizado com Firebase
                    val maquinaSalva = _maquinas.value.find { it.id == maquina.id || it.numeroSerie == maquina.numeroSerie }
                    if (maquinaSalva?.pendenteSincronizacao == true) {
                        _mensagemOperacao.value = "💾 Máquina salva localmente. Sincronizará quando houver conexão."
                    } else {
                        _mensagemOperacao.value = "✅ Máquina '${maquina.identificacao}' salva e sincronizada!"
                    }
                } else {
                    _mensagemOperacao.value = "💾 Máquina salva localmente. Sincronizará quando houver conexão."
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao salvar máquina", e)
                _mensagemOperacao.value = "💾 Máquina salva localmente. Sincronizará quando houver conexão."
                // ✅ Recarrega mesmo com erro para mostrar dados locais
                carregarTodosDados()
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
                _mensagemOperacao.value = "✅ Máquina '${maquina.identificacao}' excluída"
                carregarTodosDados()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao excluir máquina", e)
                _mensagemOperacao.value = "✅ Máquina excluída localmente. Sincronização pendente."
                carregarTodosDados()
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
                    "✅ Sincronização concluída com sucesso!"
                } else {
                    "⚠️ Erro na sincronização. Dados salvos localmente."
                }
                carregarTodosDados()
            } catch (e: Exception) {
                Log.e(TAG, "Erro durante sincronização", e)
                _mensagemOperacao.value = "⚠️ Erro na sincronização: ${e.message}"
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    fun limparMensagem() { _mensagemOperacao.value = null }

    private fun sincronizarDadosIniciais() {
        viewModelScope.launch {
            try {
                _sincronizacaoInicial.value = true
                Log.d(TAG, "🔄 Iniciando sincronização inicial com Firebase...")

                // Força sincronização completa (baixa todos os dados do Firebase)
                sincronizarMaquinasUseCase()

                Log.d(TAG, "✅ Sincronização inicial concluída")

                // Carrega os dados do cache atualizado
                carregarTodosDados()
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Erro na sincronização inicial (modo offline?)", e)
                // Mesmo com erro, tenta carregar o que tem no cache
                carregarTodosDados()
            } finally {
                _sincronizacaoInicial.value = false
            }
        }
    }
}
