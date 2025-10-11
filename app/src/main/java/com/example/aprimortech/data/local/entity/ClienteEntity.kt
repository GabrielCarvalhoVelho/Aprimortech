package com.example.aprimortech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.aprimortech.data.local.converters.ContatoClienteListConverter
import com.example.aprimortech.model.Cliente
import com.example.aprimortech.model.ContatoCliente

@Entity(tableName = "clientes")
@TypeConverters(ContatoClienteListConverter::class)
data class ClienteEntity(
    @PrimaryKey
    val id: String,
    val nome: String,
    val cnpjCpf: String,
    val contatos: List<ContatoCliente>,
    val endereco: String,
    val cidade: String,
    val estado: String,
    val telefone: String,
    val celular: String,
    val latitude: Double?,
    val longitude: Double?,
    val pendenteSincronizacao: Boolean = false, // Flag para indicar sincronização pendente
    val timestampAtualizacao: Long = System.currentTimeMillis() // Timestamp da última atualização
)

/**
 * Extensão para converter Entity -> Model
 */
fun ClienteEntity.toModel(): Cliente {
    return Cliente(
        id = id,
        nome = nome,
        cnpjCpf = cnpjCpf,
        contatos = contatos,
        endereco = endereco,
        cidade = cidade,
        estado = estado,
        telefone = telefone,
        celular = celular,
        latitude = latitude,
        longitude = longitude
    )
}

/**
 * Extensão para converter Model -> Entity
 */
fun Cliente.toEntity(pendenteSincronizacao: Boolean = false): ClienteEntity {
    return ClienteEntity(
        id = id,
        nome = nome,
        cnpjCpf = cnpjCpf,
        contatos = contatos,
        endereco = endereco,
        cidade = cidade,
        estado = estado,
        telefone = telefone,
        celular = celular,
        latitude = latitude,
        longitude = longitude,
        pendenteSincronizacao = pendenteSincronizacao,
        timestampAtualizacao = System.currentTimeMillis()
    )
}

