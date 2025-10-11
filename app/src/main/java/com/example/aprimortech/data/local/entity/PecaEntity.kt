package com.example.aprimortech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.aprimortech.model.Peca

@Entity(tableName = "pecas")
data class PecaEntity(
    @PrimaryKey val id: String,
    val nome: String,
    val codigo: String,
    val descricao: String,
    val fabricante: String,
    val categoria: String,
    val preco: Double,
    val pendenteSincronizacao: Boolean = false,
    val timestampAtualizacao: Long = System.currentTimeMillis()
)

/**
 * Extensão para converter Entity -> Model
 */
fun PecaEntity.toModel(): Peca {
    return Peca(
        id = id,
        nome = nome,
        codigo = codigo,
        descricao = descricao,
        fabricante = fabricante,
        categoria = categoria,
        preco = preco
    )
}

/**
 * Extensão para converter Model -> Entity
 */
fun Peca.toEntity(pendenteSincronizacao: Boolean = false): PecaEntity {
    return PecaEntity(
        id = id,
        nome = nome,
        codigo = codigo,
        descricao = descricao,
        fabricante = fabricante,
        categoria = categoria,
        preco = preco,
        pendenteSincronizacao = pendenteSincronizacao,
        timestampAtualizacao = System.currentTimeMillis()
    )
}
