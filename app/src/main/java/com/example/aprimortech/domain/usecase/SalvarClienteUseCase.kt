package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.ClienteRepository
import com.example.aprimortech.data.local.entity.ClienteEntity
import com.example.aprimortech.model.Cliente

class SalvarClienteUseCase(private val repository: ClienteRepository) {
    suspend operator fun invoke(cliente: ClienteEntity) {
        repository.insertLocal(cliente)
        // Opcional: também salva remoto
        val clienteDominio = Cliente(
            id = cliente.id,
            nome = cliente.nome,
            cnpjCpf = cliente.cnpjCpf,
            contato = cliente.contato,
            endereco = cliente.endereco,
            cidade = cliente.cidade,
            estado = cliente.estado,
            telefone = cliente.telefone,
            celular = cliente.celular,
            latitude = cliente.latitude,
            longitude = cliente.longitude
        )
        repository.insertRemote(clienteDominio)
    }
}
