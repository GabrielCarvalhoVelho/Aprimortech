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

    private val _proximasManutencoes = MutableStateFlow<List<Relatorio>>(emptyList())
    val proximasManutencoes: StateFlow<List<Relatorio>> = _proximasManutencoes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _relatorioSalvoId = MutableStateFlow<String?>(null)
    val relatorioSalvoId: StateFlow<String?> = _relatorioSalvoId.asStateFlow()

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
                _relatorioSalvoId.value = relatorio.id // Armazena o ID do relatório salvo
                carregarRelatorios() // Recarrega a lista
                android.util.Log.d("RelatorioViewModel", "=== SALVAMENTO CONCLUÍDO COM SUCESSO ===")
            } catch (e: Exception) {
                android.util.Log.e("RelatorioViewModel", "=== ERRO NO SALVAMENTO ===", e)
                _mensagemOperacao.value = "Erro ao salvar relatório: ${e.message}"
            }
        }
    }

    fun salvarRelatorioComAssinaturas(
        relatorio: Relatorio,
        assinaturaCliente1: android.graphics.Bitmap?,
        assinaturaCliente2: android.graphics.Bitmap?,
        assinaturaTecnico1: android.graphics.Bitmap?,
        assinaturaTecnico2: android.graphics.Bitmap?
    ) {
        android.util.Log.d("RelatorioViewModel", "=== MÉTODO salvarRelatorioComAssinaturas CHAMADO ===")
        android.util.Log.d("RelatorioViewModel", "Iniciando viewModelScope.launch...")

        viewModelScope.launch {
            try {
                android.util.Log.d("RelatorioViewModel", "=== DENTRO DO VIEWMODEL SCOPE ===")
                android.util.Log.d("RelatorioViewModel", "Cliente ID: ${relatorio.clienteId}")
                android.util.Log.d("RelatorioViewModel", "Máquina ID: ${relatorio.maquinaId}")
                android.util.Log.d("RelatorioViewModel", "Relatório ID: ${relatorio.id}")
                android.util.Log.d("RelatorioViewModel", "Horário Entrada: ${relatorio.horarioEntrada}")
                android.util.Log.d("RelatorioViewModel", "Horário Saída: ${relatorio.horarioSaida}")
                android.util.Log.d("RelatorioViewModel", "⭐⭐⭐ VALOR HORA TÉCNICA: ${relatorio.valorHoraTecnica}")
                android.util.Log.d("RelatorioViewModel", "Tem assinatura cliente 1: ${assinaturaCliente1 != null}")
                android.util.Log.d("RelatorioViewModel", "Tem assinatura cliente 2: ${assinaturaCliente2 != null}")
                android.util.Log.d("RelatorioViewModel", "Tem assinatura técnico 1: ${assinaturaTecnico1 != null}")
                android.util.Log.d("RelatorioViewModel", "Tem assinatura técnico 2: ${assinaturaTecnico2 != null}")

                android.util.Log.d("RelatorioViewModel", "Iniciando conversão para Base64...")

                // Converte bitmaps para Base64
                val assinaturaCliente1Base64 = assinaturaCliente1?.let { bitmap ->
                    val byteArrayOutputStream = java.io.ByteArrayOutputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()
                    android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
                }
                val assinaturaCliente2Base64 = assinaturaCliente2?.let { bitmap ->
                    val byteArrayOutputStream = java.io.ByteArrayOutputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()
                    android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
                }
                val assinaturaTecnico1Base64 = assinaturaTecnico1?.let { bitmap ->
                    val byteArrayOutputStream = java.io.ByteArrayOutputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()
                    android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
                }
                val assinaturaTecnico2Base64 = assinaturaTecnico2?.let { bitmap ->
                    val byteArrayOutputStream = java.io.ByteArrayOutputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()
                    android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
                }

                android.util.Log.d("RelatorioViewModel", "Criando relatório completo...")
                // Atualiza o relatório com as assinaturas em Base64
                val relatorioCompleto = relatorio.copy(
                    assinaturaCliente1 = assinaturaCliente1Base64,
                    assinaturaCliente2 = assinaturaCliente2Base64,
                    assinaturaTecnico1 = assinaturaTecnico1Base64,
                    assinaturaTecnico2 = assinaturaTecnico2Base64
                )

                android.util.Log.d("RelatorioViewModel", "=== RELATÓRIO COMPLETO CRIADO ===")
                android.util.Log.d("RelatorioViewModel", "⭐⭐⭐ VALOR HORA TÉCNICA (após copy): ${relatorioCompleto.valorHoraTecnica}")
                android.util.Log.d("RelatorioViewModel", "Relatório completo criado, chamando salvarRelatorioUseCase...")
                android.util.Log.d("RelatorioViewModel", "Use case disponível: ${salvarRelatorioUseCase != null}")

                // Captura o ID retornado pelo salvarRelatorioUseCase
                val idRelatorioSalvo = salvarRelatorioUseCase(relatorioCompleto)
                android.util.Log.d("RelatorioViewModel", "Use case executado com sucesso!")
                android.util.Log.d("RelatorioViewModel", "ID retornado pelo repository: $idRelatorioSalvo")

                android.util.Log.d("RelatorioViewModel", "Definindo mensagem de sucesso...")
                _mensagemOperacao.value = "Relatório finalizado com sucesso!"
                _relatorioSalvoId.value = idRelatorioSalvo // Usa o ID retornado pelo repository
                android.util.Log.d("RelatorioViewModel", "Mensagem definida: ${_mensagemOperacao.value}")
                android.util.Log.d("RelatorioViewModel", "ID do relatório salvo: $idRelatorioSalvo")

                android.util.Log.d("RelatorioViewModel", "Recarregando lista de relatórios...")
                carregarRelatorios() // Recarrega a lista
                android.util.Log.d("RelatorioViewModel", "=== SALVAMENTO COM ASSINATURAS CONCLUÍDO ===")
            } catch (e: Exception) {
                android.util.Log.e("RelatorioViewModel", "=== ERRO NO SALVAMENTO COM ASSINATURAS ===", e)
                android.util.Log.e("RelatorioViewModel", "Stack trace completo:", e)
                _mensagemOperacao.value = "Erro ao finalizar relatório: ${e.message}"
                android.util.Log.d("RelatorioViewModel", "Mensagem de erro definida: ${_mensagemOperacao.value}")
            }
        }
        android.util.Log.d("RelatorioViewModel", "=== FIM DO MÉTODO salvarRelatorioComAssinaturas ===")
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
