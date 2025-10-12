package com.example.aprimortech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.aprimortech.model.Maquina

@Entity(tableName = "maquinas")
data class MaquinaEntity(
    @PrimaryKey
    val id: String = "",
    val clienteId: String = "",
    val fabricante: String = "",
    val numeroSerie: String = "",
    val modelo: String = "",
    val identificacao: String = "",
    val anoFabricacao: String = "",
    val codigoConfiguracao: String = "",
    val pendenteSincronizacao: Boolean = false, // Flag para indicar sincronização pendente
    val timestampAtualizacao: Long = System.currentTimeMillis() // Timestamp da última atualização
)

/**
 * Extensão para converter Entity -> Model
 */
fun MaquinaEntity.toModel(): Maquina {
    return Maquina(
        id = id,
        clienteId = clienteId,
        fabricante = fabricante,
        numeroSerie = numeroSerie,
        modelo = modelo,
        identificacao = identificacao,
        anoFabricacao = anoFabricacao,
        codigoConfiguracao = codigoConfiguracao
    )
}

/**
 * Extensão para converter Model -> Entity
 */
fun Maquina.toEntity(pendenteSincronizacao: Boolean = false): MaquinaEntity {
    return MaquinaEntity(
        id = id,
        clienteId = clienteId,
        fabricante = fabricante,
        numeroSerie = numeroSerie,
        modelo = modelo,
        identificacao = identificacao,
        anoFabricacao = anoFabricacao,
        codigoConfiguracao = codigoConfiguracao,
        pendenteSincronizacao = pendenteSincronizacao,
        timestampAtualizacao = System.currentTimeMillis()
    )
}
