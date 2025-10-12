package com.example.aprimortech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.aprimortech.model.Relatorio

@Entity(tableName = "relatorios")
data class RelatorioEntity(
    @PrimaryKey val id: String,
    val clienteId: String,
    val maquinaId: String,
    val pecaIds: String, // CSV format
    val descricaoServico: String,
    val recomendacoes: String,
    val numeroNotaFiscal: String?,
    val dataRelatorio: String,
    val horarioEntrada: String?,
    val horarioSaida: String?,
    val distanciaKm: Double?,
    val valorDeslocamentoPorKm: Double?,
    val valorDeslocamentoTotal: Double?,
    val valorPedagios: Double?,
    val custoPecas: Double?,
    val observacoes: String?,
    val assinaturaCliente: String?,
    val assinaturaTecnico: String?,
    val tintaId: String?,
    val solventeId: String?,
    val codigoTinta: String?,
    val codigoSolvente: String?,
    val dataProximaPreventiva: String?,
    val horasProximaPreventiva: String?,
    val syncPending: Boolean
)

// Extension functions para conversão
fun RelatorioEntity.toRelatorio(): Relatorio {
    return Relatorio(
        id = id,
        clienteId = clienteId,
        maquinaId = maquinaId,
        pecaIds = if (pecaIds.isBlank()) emptyList() else pecaIds.split(","),
        descricaoServico = descricaoServico,
        recomendacoes = recomendacoes,
        numeroNotaFiscal = numeroNotaFiscal,
        dataRelatorio = dataRelatorio,
        horarioEntrada = horarioEntrada,
        horarioSaida = horarioSaida,
        distanciaKm = distanciaKm,
        valorDeslocamentoPorKm = valorDeslocamentoPorKm,
        valorDeslocamentoTotal = valorDeslocamentoTotal,
        valorPedagios = valorPedagios,
        custoPecas = custoPecas,
        observacoes = observacoes,
        assinaturaCliente = assinaturaCliente,
        assinaturaTecnico = assinaturaTecnico,
        tintaId = tintaId,
        solventeId = solventeId,
        codigoTinta = codigoTinta,
        codigoSolvente = codigoSolvente,
        dataProximaPreventiva = dataProximaPreventiva,
        horasProximaPreventiva = horasProximaPreventiva,
        syncPending = syncPending
    )
}

fun Relatorio.toRelatorioEntity(): RelatorioEntity {
    return RelatorioEntity(
        id = id,
        clienteId = clienteId,
        maquinaId = maquinaId,
        pecaIds = pecaIds.joinToString(","),
        descricaoServico = descricaoServico,
        recomendacoes = recomendacoes,
        numeroNotaFiscal = numeroNotaFiscal,
        dataRelatorio = dataRelatorio,
        horarioEntrada = horarioEntrada,
        horarioSaida = horarioSaida,
        distanciaKm = distanciaKm,
        valorDeslocamentoPorKm = valorDeslocamentoPorKm,
        valorDeslocamentoTotal = valorDeslocamentoTotal,
        valorPedagios = valorPedagios,
        custoPecas = custoPecas,
        observacoes = observacoes,
        assinaturaCliente = assinaturaCliente,
        assinaturaTecnico = assinaturaTecnico,
        tintaId = tintaId,
        solventeId = solventeId,
        codigoTinta = codigoTinta,
        codigoSolvente = codigoSolvente,
        dataProximaPreventiva = dataProximaPreventiva,
        horasProximaPreventiva = horasProximaPreventiva,
        syncPending = syncPending
    )
}
