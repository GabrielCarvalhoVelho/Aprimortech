package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprimortech.data.repository.MaquinaRepository
import com.example.aprimortech.model.Maquina
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DadosEquipamentoViewModel(
    private val maquinaRepository: MaquinaRepository,
    private val relatorioRepository: com.example.aprimortech.data.repository.RelatorioRepository
) : ViewModel() {

    private val _fabricantes = MutableStateFlow<List<String>>(emptyList())
    val fabricantes: StateFlow<List<String>> = _fabricantes.asStateFlow()

    private val _maquinas = MutableStateFlow<List<Maquina>>(emptyList())
    val maquinas: StateFlow<List<Maquina>> = _maquinas.asStateFlow()

    private val _numerosSeriePorFabricante = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val numerosSeriePorFabricante: StateFlow<Map<String, List<String>>> = _numerosSeriePorFabricante.asStateFlow()

    private val _codigosTinta = MutableStateFlow<List<String>>(emptyList())
    val codigosTinta: StateFlow<List<String>> = _codigosTinta.asStateFlow()

    private val _codigosSolvente = MutableStateFlow<List<String>>(emptyList())
    val codigosSolvente: StateFlow<List<String>> = _codigosSolvente.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _mensagemOperacao = MutableStateFlow<String?>(null)
    val mensagemOperacao: StateFlow<String?> = _mensagemOperacao.asStateFlow()

    fun carregarDados() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                android.util.Log.d("DadosEquipamentoViewModel", "Carregando dados...")

                // Carregar máquinas e extrair fabricantes únicos
                val maquinasFirebase = maquinaRepository.buscarMaquinas()
                _maquinas.value = maquinasFirebase

                // Extrair fabricantes únicos
                val fabricantesList = mutableListOf<String>()
                maquinasFirebase.forEach { maquina ->
                    if (maquina.fabricante.isNotBlank() && !fabricantesList.contains(maquina.fabricante)) {
                        fabricantesList.add(maquina.fabricante)
                    }
                }
                fabricantesList.sort()
                _fabricantes.value = fabricantesList

                // Criar mapa de números de série por fabricante
                val numerosSeriePorFabricanteMap = mutableMapOf<String, MutableList<String>>()
                maquinasFirebase.forEach { maquina ->
                    if (maquina.fabricante.isNotBlank() && maquina.numeroSerie.isNotBlank()) {
                        val listaNumeros = numerosSeriePorFabricanteMap.getOrPut(maquina.fabricante) { mutableListOf() }
                        if (!listaNumeros.contains(maquina.numeroSerie)) {
                            listaNumeros.add(maquina.numeroSerie)
                        }
                    }
                }

                // Converter para Map<String, List<String>> e ordenar os números
                val numerosSerieFinal = mutableMapOf<String, List<String>>()
                numerosSeriePorFabricanteMap.forEach { (fabricante, numeros) ->
                    numeros.sort()
                    numerosSerieFinal[fabricante] = numeros.toList()
                }
                _numerosSeriePorFabricante.value = numerosSerieFinal

                android.util.Log.d("DadosEquipamentoViewModel", "Máquinas carregadas: ${maquinasFirebase.size}")
                android.util.Log.d("DadosEquipamentoViewModel", "Fabricantes únicos: ${fabricantesList.size}")
                android.util.Log.d("DadosEquipamentoViewModel", "Números de série por fabricante: ${numerosSerieFinal.size}")

                // Removendo códigos mockados - serão implementados quando necessário
                _codigosTinta.value = emptyList()
                _codigosSolvente.value = emptyList()

            } catch (e: Exception) {
                android.util.Log.e("DadosEquipamentoViewModel", "Erro ao carregar dados", e)
                _mensagemOperacao.value = "Erro ao carregar dados: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun obterNumerosSeriePorFabricante(fabricante: String): List<String> {
        return _numerosSeriePorFabricante.value[fabricante] ?: emptyList()
    }

    fun atualizarIdentificacaoMaquina(numeroSerie: String, novaIdentificacao: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("DadosEquipamentoViewModel", "Atualizando identificação da máquina: $numeroSerie -> $novaIdentificacao")

                // Buscar a máquina pelo número de série
                val maquina = _maquinas.value.find { it.numeroSerie.equals(numeroSerie, ignoreCase = true) }

                if (maquina != null) {
                    // Atualizar a máquina com a nova identificação
                    val maquinaAtualizada = maquina.copy(identificacao = novaIdentificacao)

                    // Converter para MaquinaEntity antes de salvar
                    val maquinaEntity = maquinaAtualizada.toEntity()
                    val sucesso = maquinaRepository.salvarMaquina(maquinaEntity)

                    if (sucesso) {
                        // Atualizar a lista local
                        val maquinasAtualizadas = _maquinas.value.map {
                            if (it.id == maquina.id) maquinaAtualizada else it
                        }
                        _maquinas.value = maquinasAtualizadas
                        android.util.Log.d("DadosEquipamentoViewModel", "Identificação atualizada com sucesso")
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e("DadosEquipamentoViewModel", "Erro ao atualizar identificação", e)
                _mensagemOperacao.value = "Erro ao atualizar identificação: ${e.message}"
            }
        }
    }

    // Método de conversão simplificado
    private fun Maquina.toEntity(): com.example.aprimortech.data.local.entity.MaquinaEntity {
        return com.example.aprimortech.data.local.entity.MaquinaEntity(
            id = this.id,
            clienteId = this.clienteId,
            fabricante = this.fabricante,
            numeroSerie = this.numeroSerie,
            modelo = this.modelo,
            identificacao = this.identificacao,
            anoFabricacao = this.anoFabricacao,
            dataProximaPreventiva = this.dataProximaPreventiva ?: "",
            codigoConfiguracao = this.codigoConfiguracao,
            horasProximaPreventiva = this.horasProximaPreventiva
        )
    }

    fun limparMensagem() {
        _mensagemOperacao.value = null
    }
}
