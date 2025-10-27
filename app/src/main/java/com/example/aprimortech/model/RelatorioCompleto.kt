package com.example.aprimortech.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Modelo completo do relatório finalizado com todos os dados necessários
 * para exibição e geração de PDF
 */
@Parcelize
data class RelatorioCompleto(
    // Identificação
    val id: String = "",
    val dataRelatorio: String = "",

    // Dados do Cliente
    val clienteNome: String = "",
    val clienteEndereco: String = "",
    val clienteCidade: String = "",
    val clienteEstado: String = "",
    val clienteTelefone: String = "",
    val clienteCelular: String = "",
    val clienteContatos: List<ContatoInfo> = emptyList(),

    // Dados do Equipamento
    val equipamentoFabricante: String = "",
    val equipamentoNumeroSerie: String = "",
    val equipamentoCodigoConfiguracao: String = "",
    val equipamentoModelo: String = "",
    val equipamentoIdentificacao: String = "",
    val equipamentoAnoFabricacao: String = "",
    val equipamentoCodigoTinta: String = "",
    val equipamentoCodigoSolvente: String = "",
    val equipamentoDataProximaPreventiva: String = "",
    val equipamentoHoraProximaPreventiva: String = "",
    // NOVO: lista de fotos do equipamento (Base64 ou URIs serializados) - até 4 itens
    val equipamentoFotos: List<String> = emptyList(),

    // Defeitos
    val defeitos: List<String> = emptyList(),

    // Serviços Realizados
    val servicos: List<String> = emptyList(),

    // Peças Utilizadas
    val pecas: List<PecaInfo> = emptyList(),

    // Horas Técnicas
    val horarioEntrada: String = "",
    val horarioSaida: String = "",
    val valorHoraTecnica: Double = 0.0,
    val totalHorasTecnicas: Double = 0.0,

    // Deslocamento
    val quantidadeKm: Double = 0.0,
    val valorPorKm: Double = 0.0,
    val valorPedagios: Double = 0.0,
    val valorTotalDeslocamento: Double = 0.0,

    // Assinaturas (Base64)
    val assinaturaTecnico1: String? = null,
    val assinaturaTecnico2: String? = null,
    val assinaturaCliente1: String? = null,
    val assinaturaCliente2: String? = null,

    // Observações adicionais
    val observacoes: String = "",
    val nomeTecnico: String = ""
) : Parcelable

@Parcelize
data class ContatoInfo(
    val nome: String = "",
    val setor: String = "",
    val celular: String = ""
) : Parcelable

@Parcelize
data class PecaInfo(
    val codigo: String = "",
    val descricao: String = "",
    val quantidade: Int = 0
) : Parcelable
