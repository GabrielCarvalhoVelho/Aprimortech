package com.example.aprimortech.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprimortech.domain.usecase.BuscarPecasUseCase
import com.example.aprimortech.domain.usecase.ExcluirPecaUseCase
import com.example.aprimortech.domain.usecase.SalvarPecaUseCase
import com.example.aprimortech.domain.usecase.SincronizarPecasUseCase
import com.example.aprimortech.data.repository.PecaRepository
import com.example.aprimortech.model.Peca
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class PecaViewModel @Inject constructor(
    private val buscarPecasUseCase: BuscarPecasUseCase,
    private val salvarPecaUseCase: SalvarPecaUseCase,
    private val excluirPecaUseCase: ExcluirPecaUseCase,
    private val sincronizarPecasUseCase: SincronizarPecasUseCase,
    private val pecaRepository: PecaRepository
) : ViewModel() {

    companion object { private const val TAG = "PecaViewModel" }

    private val _mensagemOperacao = MutableStateFlow<String?>(null)
    val mensagemOperacao: StateFlow<String?> = _mensagemOperacao.asStateFlow()

    private val _pecas = MutableStateFlow<List<Peca>>(emptyList())
    val pecas: StateFlow<List<Peca>> = _pecas.asStateFlow()

    private val _operacaoEmAndamento = MutableStateFlow(false)
    val operacaoEmAndamento: StateFlow<Boolean> = _operacaoEmAndamento.asStateFlow()

    private val _itensPendentesSincronizacao = MutableStateFlow(0)
    val itensPendentesSincronizacao: StateFlow<Int> = _itensPendentesSincronizacao.asStateFlow()

    private val _sincronizacaoInicial = MutableStateFlow(false)
    val sincronizacaoInicial: StateFlow<Boolean> = _sincronizacaoInicial.asStateFlow()

    init {
        sincronizarDadosIniciais()
    }

    private fun carregarPecas() {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true
                val pecasList = buscarPecasUseCase()
                _pecas.value = pecasList
                Log.d(TAG, "✅ ${pecasList.size} peças carregadas (cache local)")
                verificarItensPendentes()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao carregar peças", e)
                _mensagemOperacao.value = "Erro ao carregar peças: ${e.message}"
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    fun salvarPeca(peca: Peca, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true
                Log.d(TAG, "💾 Salvando peça: ${peca.codigo}")

                salvarPecaUseCase(peca)

                _mensagemOperacao.value = "✅ Peça '${peca.codigo}' salva!"
                Log.d(TAG, "✅ Peça salva com sucesso")
                callback(true)

                carregarPecas()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao salvar peça", e)
                _mensagemOperacao.value = "Erro ao salvar peça: ${e.message}"
                callback(false)
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    fun excluirPeca(id: String) {
        viewModelScope.launch {
            try {
                _operacaoEmAndamento.value = true
                excluirPecaUseCase(id)
                _mensagemOperacao.value = "✅ Peça excluída"
                Log.d(TAG, "✅ Peça excluída com sucesso")
                carregarPecas()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao excluir peça", e)
                _mensagemOperacao.value = "Erro ao excluir: ${e.message}"
            } finally {
                _operacaoEmAndamento.value = false
            }
        }
    }

    private fun verificarItensPendentes() {
        viewModelScope.launch {
            try {
                _itensPendentesSincronizacao.value = pecaRepository.contarPecasPendentes()
                if (_itensPendentesSincronizacao.value > 0) {
                    Log.d(TAG, "⚠️ ${_itensPendentesSincronizacao.value} peças pendentes de sincronização")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao verificar itens pendentes", e)
            }
        }
    }

    private fun sincronizarDadosIniciais() {
        viewModelScope.launch {
            try {
                _sincronizacaoInicial.value = true
                Log.d(TAG, "🔄 Iniciando sincronização inicial com Firebase...")

                // Força sincronização completa (baixa todos os dados do Firebase)
                sincronizarPecasUseCase()

                Log.d(TAG, "✅ Sincronização inicial concluída")

                // Carrega os dados do cache atualizado
                carregarPecas()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro na sincronização inicial", e)
                // Mesmo com erro, tenta carregar do cache local
                carregarPecas()
            } finally {
                _sincronizacaoInicial.value = false
            }
        }
    }

    fun limparMensagem() {
        _mensagemOperacao.value = null
    }
}
