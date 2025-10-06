package com.example.aprimortech.ui.viewmodel

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

    private val _mensagemOperacao = MutableStateFlow<String?>(null)
    val mensagemOperacao: StateFlow<String?> = _mensagemOperacao.asStateFlow()

    private val _pecas = MutableStateFlow<List<Peca>>(emptyList())
    val pecas: StateFlow<List<Peca>> = _pecas.asStateFlow()

    private val _fabricantesDisponiveis = MutableStateFlow<List<String>>(emptyList())
    val fabricantesDisponiveis: StateFlow<List<String>> = _fabricantesDisponiveis.asStateFlow()

    private val _categoriasDisponiveis = MutableStateFlow<List<String>>(emptyList())
    val categoriasDisponiveis: StateFlow<List<String>> = _categoriasDisponiveis.asStateFlow()

    init {
        carregarPecas()
        carregarDadosAuxiliares()
    }

    private fun carregarPecas() {
        viewModelScope.launch {
            try {
                val pecasList = buscarPecasUseCase()
                _pecas.value = pecasList
            } catch (e: Exception) {
                android.util.Log.e("PecaViewModel", "Erro ao carregar peças", e)
                _mensagemOperacao.value = "Erro ao carregar peças: ${e.message}"
            }
        }
    }

    private fun carregarDadosAuxiliares() {
        viewModelScope.launch {
            try {
                // Extrair fabricantes e categorias das peças existentes
                val pecasAtuais = _pecas.value
                _fabricantesDisponiveis.value = pecasAtuais.map { it.fabricante }.filter { it.isNotBlank() }.distinct().sorted()
                _categoriasDisponiveis.value = pecasAtuais.map { it.categoria }.filter { it.isNotBlank() }.distinct().sorted()
            } catch (e: Exception) {
                android.util.Log.e("PecaViewModel", "Erro ao carregar dados auxiliares", e)
            }
        }
    }

    fun salvarPeca(peca: Peca) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PecaViewModel", "=== USUÁRIO SOLICITOU SALVAMENTO ===")
                android.util.Log.d("PecaViewModel", "Peça: ${peca.nome}")

                salvarPecaUseCase(peca)
                android.util.Log.d("PecaViewModel", "Use case executado com sucesso")
                _mensagemOperacao.value = "Peça salva com sucesso!"
                carregarPecas() // Recarrega a lista
                android.util.Log.d("PecaViewModel", "=== SALVAMENTO CONCLUÍDO COM SUCESSO ===")
            } catch (e: Exception) {
                android.util.Log.e("PecaViewModel", "=== ERRO NO SALVAMENTO ===", e)
                _mensagemOperacao.value = "Erro ao salvar peça: ${e.message}"
            }
        }
    }

    fun excluirPeca(id: String) {
        viewModelScope.launch {
            try {
                excluirPecaUseCase(id)
                _mensagemOperacao.value = "Peça excluída com sucesso!"
                carregarPecas() // Recarrega a lista
            } catch (e: Exception) {
                android.util.Log.e("PecaViewModel", "Erro ao excluir peça", e)
                _mensagemOperacao.value = "Erro ao excluir peça: ${e.message}"
            }
        }
    }

    fun sincronizarPecas() {
        viewModelScope.launch {
            try {
                val sucesso = sincronizarPecasUseCase()
                if (sucesso) {
                    _mensagemOperacao.value = "Peças sincronizadas com sucesso!"
                    carregarPecas()
                } else {
                    _mensagemOperacao.value = "Falha na sincronização das peças"
                }
            } catch (e: Exception) {
                android.util.Log.e("PecaViewModel", "Erro ao sincronizar peças", e)
                _mensagemOperacao.value = "Erro na sincronização: ${e.message}"
            }
        }
    }

    fun limparMensagem() {
        _mensagemOperacao.value = null
    }
}
