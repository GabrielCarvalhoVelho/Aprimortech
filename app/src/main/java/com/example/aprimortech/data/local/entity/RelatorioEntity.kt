package com.example.aprimortech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.aprimortech.model.Relatorio
import org.json.JSONArray

@Entity(tableName = "relatorios")
data class RelatorioEntity(
    @PrimaryKey val id: String,
    val numeroRelatorio: String = "",
    val clienteId: String,
    val maquinaId: String,
    val pecaIds: String, // CSV format
    val descricaoServico: String,
    val recomendacoes: String,
    val numeroNotaFiscal: String?,
    val dataRelatorio: String,
    val horarioEntrada: String?,
    val horarioSaida: String?,
    val valorHoraTecnica: Double?,
    val distanciaKm: Double?,
    val valorDeslocamentoPorKm: Double?,
    val valorDeslocamentoTotal: Double?,
    val valorPedagios: Double?,
    val custoPecas: Double?,
    val observacoes: String?,
    val assinaturaCliente1: String?,
    val assinaturaCliente2: String?,
    val assinaturaTecnico1: String?,
    val assinaturaTecnico2: String?,
    val tintaId: String?,
    val solventeId: String?,
    val codigoTinta: String?,
    val codigoSolvente: String?,
    val dataProximaPreventiva: String?,
    val horasProximaPreventiva: String?,
    val defeitosIdentificados: String = "", // CSV format
    val servicosRealizados: String = "", // CSV format
    val observacoesDefeitosServicos: String = "",
    val syncPending: Boolean,
    // NOVO: armazenamento das fotos do equipamento como JSON array string
    val equipamentoFotosJson: String = "[]"
)

// Extension functions para conversão
fun RelatorioEntity.toRelatorio(): Relatorio {
    // Desserializar equipamentoFotosJson (JSONArray -> List<String>)
    val fotosList = try {
        val arr = JSONArray(equipamentoFotosJson)
        List(arr.length()) { i -> arr.optString(i) }
    } catch (e: Exception) {
        emptyList()
    }

    return Relatorio(
        id = id,
        numeroRelatorio = numeroRelatorio,
        clienteId = clienteId,
        maquinaId = maquinaId,
        pecaIds = if (pecaIds.isBlank()) emptyList() else pecaIds.split(","),
        descricaoServico = descricaoServico,
        recomendacoes = recomendacoes,
        numeroNotaFiscal = numeroNotaFiscal,
        dataRelatorio = dataRelatorio,
        horarioEntrada = horarioEntrada,
        horarioSaida = horarioSaida,
        valorHoraTecnica = valorHoraTecnica,
        distanciaKm = distanciaKm,
        valorDeslocamentoPorKm = valorDeslocamentoPorKm,
        valorDeslocamentoTotal = valorDeslocamentoTotal,
        valorPedagios = valorPedagios,
        custoPecas = custoPecas,
        observacoes = observacoes,
        assinaturaCliente1 = assinaturaCliente1,
        assinaturaCliente2 = assinaturaCliente2,
        assinaturaTecnico1 = assinaturaTecnico1,
        assinaturaTecnico2 = assinaturaTecnico2,
        tintaId = tintaId,
        solventeId = solventeId,
        codigoTinta = codigoTinta,
        codigoSolvente = codigoSolvente,
        dataProximaPreventiva = dataProximaPreventiva,
        horasProximaPreventiva = horasProximaPreventiva,
        defeitosIdentificados = if (defeitosIdentificados.isBlank()) emptyList() else defeitosIdentificados.split(","),
        servicosRealizados = if (servicosRealizados.isBlank()) emptyList() else servicosRealizados.split(","),
        observacoesDefeitosServicos = observacoesDefeitosServicos,
        syncPending = syncPending,
        equipamentoFotos = fotosList
    )
}

fun Relatorio.toRelatorioEntity(): RelatorioEntity {
    // Serializar equipamentoFotos (List<String> -> JSONArray string)
    val fotosJson = try {
        val arr = JSONArray()
        equipamentoFotos.forEach { arr.put(it) }
        arr.toString()
    } catch (e: Exception) {
        "[]"
    }

    return RelatorioEntity(
        id = id,
        numeroRelatorio = numeroRelatorio,
        clienteId = clienteId,
        maquinaId = maquinaId,
        pecaIds = pecaIds.joinToString(","),
        descricaoServico = descricaoServico,
        recomendacoes = recomendacoes,
        numeroNotaFiscal = numeroNotaFiscal,
        dataRelatorio = dataRelatorio,
        horarioEntrada = horarioEntrada,
        horarioSaida = horarioSaida,
        valorHoraTecnica = valorHoraTecnica,
        distanciaKm = distanciaKm,
        valorDeslocamentoPorKm = valorDeslocamentoPorKm,
        valorDeslocamentoTotal = valorDeslocamentoTotal,
        valorPedagios = valorPedagios,
        custoPecas = custoPecas,
        observacoes = observacoes,
        assinaturaCliente1 = assinaturaCliente1,
        assinaturaCliente2 = assinaturaCliente2,
        assinaturaTecnico1 = assinaturaTecnico1,
        assinaturaTecnico2 = assinaturaTecnico2,
        tintaId = tintaId,
        solventeId = solventeId,
        codigoTinta = codigoTinta,
        codigoSolvente = codigoSolvente,
        dataProximaPreventiva = dataProximaPreventiva,
        horasProximaPreventiva = horasProximaPreventiva,
        defeitosIdentificados = defeitosIdentificados.joinToString(","),
        servicosRealizados = servicosRealizados.joinToString(","),
        observacoesDefeitosServicos = observacoesDefeitosServicos,
        syncPending = syncPending,
        equipamentoFotosJson = fotosJson
    )
}
