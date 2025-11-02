package com.example.aprimortech.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.aprimortech.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel compartilhado para gerenciar o estado do relatório
 * durante todo o fluxo de criação
 */
class RelatorioSharedViewModel : ViewModel() {

    private val _relatorioCompleto = MutableStateFlow<RelatorioCompleto?>(null)
    val relatorioCompleto: StateFlow<RelatorioCompleto?> = _relatorioCompleto.asStateFlow()

    // Dados temporários durante a criação
    private var clienteData: ClienteData? = null
    private var equipamentoData: EquipamentoData? = null
    private var defeitosData: List<String> = emptyList()
    private var servicosData: List<String> = emptyList()
    private var pecasData: List<PecaData> = emptyList()
    private var horasData: HorasData? = null
    private var deslocamentoData: DeslocamentoData? = null
    private var assinaturasData: AssinaturasData? = null
    private var observacoesData: String = ""

    fun setClienteData(
        nome: String,
        endereco: String,
        cidade: String,
        estado: String,
        telefone: String,
        celular: String,
        contatos: List<ContatoInfo>
    ) {
        clienteData = ClienteData(nome, endereco, cidade, estado, telefone, celular, contatos)
    }

    fun setEquipamentoData(
        fabricante: String,
        numeroSerie: String,
        codigoConfiguracao: String,
        modelo: String,
        identificacao: String,
        anoFabricacao: String,
        maquinaId: String = "",
        codigoTinta: String,
        codigoSolvente: String,
        dataProximaPreventiva: String,
        horaProximaPreventiva: String,
        equipamentoFotos: List<String> = emptyList()
    ) {
        equipamentoData = EquipamentoData(
            fabricante,
            numeroSerie,
            codigoConfiguracao,
            modelo,
            identificacao,
            anoFabricacao,
            maquinaId,
            codigoTinta,
            codigoSolvente,
            dataProximaPreventiva,
            horaProximaPreventiva,
            equipamentoFotos
        )
        android.util.Log.d("RelatorioSharedViewModel", "[DEBUG] setEquipamentoData chamado")
        android.util.Log.d("RelatorioSharedViewModel", "[DEBUG] codigoTinta: $codigoTinta")
        android.util.Log.d("RelatorioSharedViewModel", "[DEBUG] codigoSolvente: $codigoSolvente")
        android.util.Log.d("RelatorioSharedViewModel", "[DEBUG] dataProximaPreventiva: $dataProximaPreventiva")
        android.util.Log.d("RelatorioSharedViewModel", "[DEBUG] horaProximaPreventiva: $horaProximaPreventiva")
        android.util.Log.d("RelatorioSharedViewModel", "[DEBUG] fotos count: ${equipamentoFotos.size}")
    }

    fun setDefeitosServicos(defeitos: List<String>, servicos: List<String>, observacoes: String) {
        defeitosData = defeitos
        servicosData = servicos
        observacoesData = observacoes
    }

    fun setPecas(pecas: List<PecaData>) {
        pecasData = pecas
    }

    fun setHorasDeslocamento(
        horarioEntrada: String,
        horarioSaida: String,
        valorHoraTecnica: Double,
        totalHoras: Double,
        quantidadeKm: Double,
        valorPorKm: Double,
        valorPedagios: Double,
        valorTotalDeslocamento: Double
    ) {
        horasData = HorasData(horarioEntrada, horarioSaida, valorHoraTecnica, totalHoras)
        deslocamentoData = DeslocamentoData(quantidadeKm, valorPorKm, valorPedagios, valorTotalDeslocamento)
    }

    fun setAssinaturas(
        assinaturaTecnico1: String?,
        assinaturaTecnico2: String?,
        assinaturaCliente1: String?,
        assinaturaCliente2: String?,
        nomeTecnico: String
    ) {
        assinaturasData = AssinaturasData(
            assinaturaTecnico1,
            assinaturaTecnico2,
            assinaturaCliente1,
            assinaturaCliente2,
            nomeTecnico
        )
    }

    fun buildRelatorioCompleto(relatorioId: String = "", dataRelatorio: String = getCurrentDate()): RelatorioCompleto {
        android.util.Log.d("RelatorioSharedViewModel", "[DEBUG] buildRelatorioCompleto chamado")
        android.util.Log.d("RelatorioSharedViewModel", "[DEBUG] equipamentoData: $equipamentoData")
        val relatorio = RelatorioCompleto(
            id = relatorioId,
            dataRelatorio = dataRelatorio,

            // Dados do Cliente
            clienteNome = clienteData?.nome ?: "",
            clienteEndereco = clienteData?.endereco ?: "",
            clienteCidade = clienteData?.cidade ?: "",
            clienteEstado = clienteData?.estado ?: "",
            clienteTelefone = clienteData?.telefone ?: "",
            clienteCelular = clienteData?.celular ?: "",
            clienteContatos = clienteData?.contatos ?: emptyList(),

            // Dados do Equipamento
            equipamentoFabricante = equipamentoData?.fabricante ?: "",
            equipamentoNumeroSerie = equipamentoData?.numeroSerie ?: "",
            equipamentoCodigoConfiguracao = equipamentoData?.codigoConfiguracao ?: "",
            equipamentoModelo = equipamentoData?.modelo ?: "",
            equipamentoIdentificacao = equipamentoData?.identificacao ?: "",
            equipamentoAnoFabricacao = equipamentoData?.anoFabricacao ?: "",
            equipamentoMaquinaId = equipamentoData?.maquinaId ?: "",
            equipamentoCodigoTinta = equipamentoData?.codigoTinta ?: "",
            equipamentoCodigoSolvente = equipamentoData?.codigoSolvente ?: "",
            equipamentoDataProximaPreventiva = equipamentoData?.dataProximaPreventiva ?: "",
            equipamentoHoraProximaPreventiva = equipamentoData?.horaProximaPreventiva ?: "",
            equipamentoFotos = equipamentoData?.fotos ?: emptyList(),

            // Defeitos e Serviços
            defeitos = defeitosData,
            servicos = servicosData,

            // Peças
            pecas = pecasData.map { PecaInfo(it.codigo, it.descricao, it.quantidade) },

            // Horas Técnicas
            horarioEntrada = horasData?.horarioEntrada ?: "",
            horarioSaida = horasData?.horarioSaida ?: "",
            valorHoraTecnica = horasData?.valorHoraTecnica ?: 0.0,
            totalHorasTecnicas = horasData?.totalHoras ?: 0.0,

            // Deslocamento
            quantidadeKm = deslocamentoData?.quantidadeKm ?: 0.0,
            valorPorKm = deslocamentoData?.valorPorKm ?: 0.0,
            valorPedagios = deslocamentoData?.valorPedagios ?: 0.0,
            valorTotalDeslocamento = deslocamentoData?.valorTotalDeslocamento ?: 0.0,

            // Assinaturas
            assinaturaTecnico1 = assinaturasData?.assinaturaTecnico1,
            assinaturaTecnico2 = assinaturasData?.assinaturaTecnico2,
            assinaturaCliente1 = assinaturasData?.assinaturaCliente1,
            assinaturaCliente2 = assinaturasData?.assinaturaCliente2,
            nomeTecnico = assinaturasData?.nomeTecnico ?: "",

            // Observações
            observacoes = observacoesData
        )

        android.util.Log.d("RelatorioSharedViewModel", "[DEBUG] RelatorioCompleto criado")
        android.util.Log.d("RelatorioSharedViewModel", "[DEBUG] equipamentoCodigoTinta: ${relatorio.equipamentoCodigoTinta}")
        android.util.Log.d("RelatorioSharedViewModel", "[DEBUG] equipamentoCodigoSolvente: ${relatorio.equipamentoCodigoSolvente}")
        android.util.Log.d("RelatorioSharedViewModel", "[DEBUG] equipamentoDataProximaPreventiva: ${relatorio.equipamentoDataProximaPreventiva}")
        android.util.Log.d("RelatorioSharedViewModel", "[DEBUG] equipamentoHoraProximaPreventiva: ${relatorio.equipamentoHoraProximaPreventiva}")

        _relatorioCompleto.value = relatorio
        return relatorio
    }

    fun limparDados() {
        clienteData = null
        equipamentoData = null
        defeitosData = emptyList()
        servicosData = emptyList()
        pecasData = emptyList()
        horasData = null
        deslocamentoData = null
        assinaturasData = null
        observacoesData = ""
        _relatorioCompleto.value = null
    }

    private fun getCurrentDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return String.format("%02d/%02d/%04d", day, month, year)
    }
}

// Data classes auxiliares
private data class ClienteData(
    val nome: String,
    val endereco: String,
    val cidade: String,
    val estado: String,
    val telefone: String,
    val celular: String,
    val contatos: List<ContatoInfo>
)

private data class EquipamentoData(
    val fabricante: String,
    val numeroSerie: String,
    val codigoConfiguracao: String,
    val modelo: String,
    val identificacao: String,
    val anoFabricacao: String,
    val maquinaId: String = "",
    val codigoTinta: String,
    val codigoSolvente: String,
    val dataProximaPreventiva: String,
    val horaProximaPreventiva: String,
    val fotos: List<String> = emptyList()
)

data class PecaData(
    val codigo: String,
    val descricao: String,
    val quantidade: Int,
    val valorUnitario: Double? = null
)

private data class HorasData(
    val horarioEntrada: String,
    val horarioSaida: String,
    val valorHoraTecnica: Double,
    val totalHoras: Double
)

private data class DeslocamentoData(
    val quantidadeKm: Double,
    val valorPorKm: Double,
    val valorPedagios: Double,
    val valorTotalDeslocamento: Double
)

private data class AssinaturasData(
    val assinaturaTecnico1: String?,
    val assinaturaTecnico2: String?,
    val assinaturaCliente1: String?,
    val assinaturaCliente2: String?,
    val nomeTecnico: String
)
