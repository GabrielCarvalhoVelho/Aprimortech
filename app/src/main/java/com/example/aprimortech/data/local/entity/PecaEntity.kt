package com.example.aprimortech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.aprimortech.model.Peca

/**
 * Entidade Room para Peça com suporte offline-first
 * Campos: código, descrição, valor unitário
 * A quantidade é controlada apenas nos relatórios
 */
@Entity(tableName = "pecas")
data class PecaEntity(
    @PrimaryKey val id: String,
    val codigo: String,
    val descricao: String,
    val valorUnitario: Double,
    val pendenteSincronizacao: Boolean = false,
    val timestampAtualizacao: Long = System.currentTimeMillis()
)

/**
 * Extensão para converter Entity -> Model
 */
fun PecaEntity.toModel(): Peca {
    return Peca(
        id = id,
        codigo = codigo,
        descricao = descricao,
        valorUnitario = valorUnitario
    )
}

/**
 * Extensão para converter Model -> Entity
 */
fun Peca.toEntity(pendenteSincronizacao: Boolean = false): PecaEntity {
    return PecaEntity(
        id = id,
        codigo = codigo,
        descricao = descricao,
        valorUnitario = valorUnitario,
        pendenteSincronizacao = pendenteSincronizacao,
        timestampAtualizacao = System.currentTimeMillis()
    )
}
