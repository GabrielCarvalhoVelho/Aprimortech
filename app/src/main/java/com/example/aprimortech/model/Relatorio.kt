package com.example.aprimortech.model

data class Relatorio(
    val id: String = "",
    val clienteId: String = "",
    val maquinaId: String = "",
    val pecaIds: List<String> = emptyList(),
    val descricaoServico: String = "",
    val recomendacoes: String = "",
    val numeroNotaFiscal: String? = null,                 // Opcional
    val dataRelatorio: String = getCurrentDate(),         // Auto preenchida (editável)
    val horarioEntrada: String? = null,                   // HH:mm
    val horarioSaida: String? = null,                     // HH:mm
    val valorHoraTecnica: Double? = null,                 // Valor da hora técnica
    val distanciaKm: Double? = null,                      // Calculada (rota)
    val valorDeslocamentoPorKm: Double? = null,           // Inserido manual
    val valorDeslocamentoTotal: Double? = null,           // distanciaKm * valorDeslocamentoPorKm
    val valorPedagios: Double? = null,                    // Manual
    val custoPecas: Double? = null,                       // Manual ou calculado externamente
    val observacoes: String? = null,
    val assinaturaCliente: String? = null,                // Base64 da imagem da assinatura
    val assinaturaTecnico: String? = null,                // Base64 da imagem da assinatura
    val tintaId: String? = null,                          // ID da tinta na collection tintas
    val solventeId: String? = null,                       // ID do solvente na collection solventes
    val codigoTinta: String? = null,                      // Código da tinta (cache para exibição)
    val codigoSolvente: String? = null,                   // Código do solvente (cache para exibição)
    val dataProximaPreventiva: String? = null,            // Data da próxima manutenção preventiva
    val horasProximaPreventiva: String? = null,           // Horas da próxima manutenção preventiva
    val defeitosIdentificados: List<String> = emptyList(), // Lista de defeitos selecionados
    val servicosRealizados: List<String> = emptyList(),    // Lista de serviços realizados
    val observacoesDefeitosServicos: String = "",          // Observações da tela DefeitoServicosScreen
    val pecasUtilizadas: List<Map<String, Any>> = emptyList(), // [{"codigo": "ABC", "descricao": "Peça X", "quantidade": 2}]
    val syncPending: Boolean = true
)

private fun getCurrentDate(): String {
    val calendar = java.util.Calendar.getInstance()
    val year = calendar.get(java.util.Calendar.YEAR)
    val month = calendar.get(java.util.Calendar.MONTH) + 1
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    return String.format("%04d-%02d-%02d", year, month, day)
}
