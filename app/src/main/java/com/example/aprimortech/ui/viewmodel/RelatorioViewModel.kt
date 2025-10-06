package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprimortech.domain.usecase.BuscarRelatoriosUseCase
import com.example.aprimortech.domain.usecase.ExcluirRelatorioUseCase
import com.example.aprimortech.domain.usecase.SalvarRelatorioUseCase
import com.example.aprimortech.domain.usecase.SincronizarRelatoriosUseCase
import com.example.aprimortech.domain.usecase.BuscarProximasManutencoesPreventivasUseCase
import com.example.aprimortech.data.repository.RelatorioRepository
import com.example.aprimortech.model.Relatorio
import com.example.aprimortech.model.Maquina
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class RelatorioViewModel @Inject constructor(
    private val buscarRelatoriosUseCase: BuscarRelatoriosUseCase,
    private val salvarRelatorioUseCase: SalvarRelatorioUseCase,
    private val excluirRelatorioUseCase: ExcluirRelatorioUseCase,
    private val sincronizarRelatoriosUseCase: SincronizarRelatoriosUseCase,
    private val buscarProximasManutencoesPreventivasUseCase: BuscarProximasManutencoesPreventivasUseCase,
    private val relatorioRepository: RelatorioRepository
) : ViewModel() {

    private val _mensagemOperacao = MutableStateFlow<String?>(null)
    val mensagemOperacao: StateFlow<String?> = _mensagemOperacao.asStateFlow()

    private val _relatorios = MutableStateFlow<List<Relatorio>>(emptyList())
    val relatorios: StateFlow<List<Relatorio>> = _relatorios.asStateFlow()

    private val _proximasManutencoes = MutableStateFlow<List<Maquina>>(emptyList())
    val proximasManutencoes: StateFlow<List<Maquina>> = _proximasManutencoes.asStateFlow()

    init {
        carregarRelatorios()
        carregarProximasManutencoes()
    }

    private fun carregarRelatorios() {
        viewModelScope.launch {
            try {
                val relatoriosList = buscarRelatoriosUseCase()
                _relatorios.value = relatoriosList
            } catch (e: Exception) {
                android.util.Log.e("RelatorioViewModel", "Erro ao carregar relatórios", e)
                _mensagemOperacao.value = "Erro ao carregar relatórios: ${e.message}"
            }
        }
    }

    private fun carregarProximasManutencoes() {
        viewModelScope.launch {
            try {
                val manutencoes = buscarProximasManutencoesPreventivasUseCase()
                _proximasManutencoes.value = manutencoes
            } catch (e: Exception) {
                android.util.Log.e("RelatorioViewModel", "Erro ao carregar manutenções", e)
            }
        }
    }

    fun salvarRelatorio(relatorio: Relatorio) {
        viewModelScope.launch {
            try {
                android.util.Log.d("RelatorioViewModel", "=== USUÁRIO SOLICITOU SALVAMENTO DE RELATÓRIO ===")
                android.util.Log.d("RelatorioViewModel", "Cliente ID: ${relatorio.clienteId}")
                android.util.Log.d("RelatorioViewModel", "Máquina ID: ${relatorio.maquinaId}")

                salvarRelatorioUseCase(relatorio)
                android.util.Log.d("RelatorioViewModel", "Use case executado com sucesso")
                _mensagemOperacao.value = "Relatório salvo com sucesso!"
                carregarRelatorios() // Recarrega a lista
                android.util.Log.d("RelatorioViewModel", "=== SALVAMENTO CONCLUÍDO COM SUCESSO ===")
            } catch (e: Exception) {
                android.util.Log.e("RelatorioViewModel", "=== ERRO NO SALVAMENTO ===", e)
                _mensagemOperacao.value = "Erro ao salvar relatório: ${e.message}"
            }
        }
    }

    fun excluirRelatorio(id: String) {
        viewModelScope.launch {
            try {
                excluirRelatorioUseCase(id)
                _mensagemOperacao.value = "Relatório excluído com sucesso!"
                carregarRelatorios() // Recarrega a lista
            } catch (e: Exception) {
                android.util.Log.e("RelatorioViewModel", "Erro ao excluir relatório", e)
                _mensagemOperacao.value = "Erro ao excluir relatório: ${e.message}"
            }
        }
    }

    fun sincronizarRelatorios() {
        viewModelScope.launch {
            try {
                val sucesso = sincronizarRelatoriosUseCase()
                if (sucesso) {
                    _mensagemOperacao.value = "Relatórios sincronizados com sucesso!"
                    carregarRelatorios()
                } else {
                    _mensagemOperacao.value = "Falha na sincronização dos relatórios"
                }
            } catch (e: Exception) {
                android.util.Log.e("RelatorioViewModel", "Erro ao sincronizar relatórios", e)
                _mensagemOperacao.value = "Erro na sincronização: ${e.message}"
            }
        }
    }

    fun buscarRelatoriosPorCliente(clienteId: String): Flow<List<Relatorio>> {
        return flow {
            try {
                val relatorios = relatorioRepository.buscarRelatoriosPorCliente(clienteId)
                emit(relatorios)
            } catch (e: Exception) {
                android.util.Log.e("RelatorioViewModel", "Erro ao buscar relatórios por cliente", e)
                emit(emptyList())
            }
        }
    }

    fun buscarRelatoriosPorMaquina(maquinaId: String): Flow<List<Relatorio>> {
        return flow {
            try {
                val relatorios = relatorioRepository.buscarRelatoriosPorMaquina(maquinaId)
                emit(relatorios)
            } catch (e: Exception) {
                android.util.Log.e("RelatorioViewModel", "Erro ao buscar relatórios por máquina", e)
                emit(emptyList())
            }
        }
    }

    fun limparMensagem() {
        _mensagemOperacao.value = null
    }
}
