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

    val pecas: StateFlow<List<Peca>> = buscarPecasUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val fabricantesDisponiveis: StateFlow<List<String>> = pecaRepository.buscarFabricantesDisponiveis()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categoriasDisponiveis: StateFlow<List<String>> = pecaRepository.buscarCategoriasDisponiveis()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun salvarPeca(peca: Peca) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PecaViewModel", "=== USUÁRIO SOLICITOU SALVAMENTO ===")
                android.util.Log.d("PecaViewModel", "Peça: ${peca.nome}")
                android.util.Log.d("PecaViewModel", "Dados da peça: $peca")

                salvarPecaUseCase(peca)
                android.util.Log.d("PecaViewModel", "Use case executado com sucesso")
                _mensagemOperacao.value = "Peça salva com sucesso!"
                android.util.Log.d("PecaViewModel", "=== SALVAMENTO CONCLUÍDO COM SUCESSO ===")
            } catch (e: Exception) {
                android.util.Log.e("PecaViewModel", "=== ERRO NO SALVAMENTO ===", e)
                _mensagemOperacao.value = "Erro ao salvar peça: ${e.message}"
            }
        }
    }

    fun excluirPeca(peca: Peca) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PecaViewModel", "=== USUÁRIO SOLICITOU EXCLUSÃO ===")
                android.util.Log.d("PecaViewModel", "Peça: ${peca.nome}")

                excluirPecaUseCase(peca)
                android.util.Log.d("PecaViewModel", "Use case de exclusão executado com sucesso")
                _mensagemOperacao.value = "Peça excluída com sucesso!"
                android.util.Log.d("PecaViewModel", "=== EXCLUSÃO CONCLUÍDA COM SUCESSO ===")
            } catch (e: Exception) {
                android.util.Log.e("PecaViewModel", "=== ERRO NA EXCLUSÃO ===", e)
                _mensagemOperacao.value = "Erro ao excluir peça: ${e.message}"
            }
        }
    }

    fun sincronizarDadosExistentes() {
        viewModelScope.launch {
            try {
                android.util.Log.w("PECA_FIREBASE_TEST", "=== USUÁRIO CLICOU EM SINCRONIZAR ===")
                android.util.Log.w("PECA_FIREBASE_TEST", "ViewModel: Iniciando sincronização...")

                // Busca dados do servidor primeiro
                android.util.Log.d("PecaViewModel", "Buscando dados do servidor...")
                val pecasDoServidor = pecaRepository.buscarPecasDoServidor()
                android.util.Log.d("PecaViewModel", "Dados encontrados no servidor: ${pecasDoServidor.size} peças")

                if (pecasDoServidor.isNotEmpty()) {
                    android.util.Log.d("PecaViewModel", "Usando dados do servidor")
                    _mensagemOperacao.value = "Peças sincronizadas com sucesso! ${pecasDoServidor.size} peças encontradas."
                } else {
                    android.util.Log.d("PecaViewModel", "Servidor vazio, criando dados de exemplo")
                    // Se não há dados no servidor, cria dados de exemplo
                    val pecasSimuladas = listOf(
                        Peca(
                            id = "peca-1",
                            nome = "Filtro de Ar",
                            codigo = "FLT001",
                            descricao = "Filtro de ar para impressoras industriais",
                            fabricante = "Hitachi",
                            categoria = "Filtros",
                            preco = 45.90
                        ),
                        Peca(
                            id = "peca-2",
                            nome = "Cabeça de Impressão",
                            codigo = "CBI002",
                            descricao = "Cabeça de impressão para modelos UX",
                            fabricante = "Videojet",
                            categoria = "Cabeças",
                            preco = 350.00
                        ),
                        Peca(
                            id = "peca-3",
                            nome = "Tinta Preta",
                            codigo = "TNT001",
                            descricao = "Tinta preta para codificação",
                            fabricante = "Domino",
                            categoria = "Tintas",
                            preco = 89.90
                        )
                    )
                    android.util.Log.d("PecaViewModel", "Sincronizando ${pecasSimuladas.size} peças de exemplo...")
                    sincronizarPecasUseCase(pecasSimuladas)
                    android.util.Log.d("PecaViewModel", "Dados de exemplo sincronizados")
                    _mensagemOperacao.value = "Dados de exemplo criados e sincronizados com sucesso!"
                }
                android.util.Log.w("PECA_FIREBASE_TEST", "=== SINCRONIZAÇÃO CONCLUÍDA COM SUCESSO ===")
            } catch (e: Exception) {
                android.util.Log.e("PECA_FIREBASE_TEST", "=== ERRO NA SINCRONIZAÇÃO DO VIEWMODEL ===", e)
                _mensagemOperacao.value = "Erro ao sincronizar peças: ${e.message}"
            }
        }
    }

    fun limparMensagem() {
        _mensagemOperacao.value = null
    }

    fun carregarTodosDados() {
        // Os dados são carregados automaticamente através dos StateFlows
        // Esta função pode ser expandida se necessário carregar dados adicionais
    }
}
