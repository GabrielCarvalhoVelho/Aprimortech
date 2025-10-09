package com.example.aprimortech.data.repository

import com.example.aprimortech.data.local.dao.ClienteDao
import com.example.aprimortech.data.local.entity.ClienteEntity
import com.example.aprimortech.model.Cliente
import com.example.aprimortech.model.ContatoCliente
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClienteRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val clienteDao: ClienteDao
) {
    private val collection = firestore.collection("clientes")

    private fun parseContatos(raw: Any?): List<ContatoCliente> {
        if (raw !is List<*>) return emptyList()
        return raw.mapNotNull { item ->
            when (item) {
                is String -> ContatoCliente(nome = item)
                is Map<*, *> -> {
                    val nome = item["nome"] as? String ?: return@mapNotNull null
                    val setor = item["setor"] as? String
                    val celular = item["celular"] as? String
                    ContatoCliente(nome = nome, setor = setor, celular = celular)
                }
                else -> null
            }
        }
    }

    private fun documentToCliente(document: com.google.firebase.firestore.DocumentSnapshot): Cliente? {
        val data = document.data ?: return null
        return try {
            val id = document.id
            val nome = data["nome"] as? String ?: ""
            val cnpjCpf = data["cnpjCpf"] as? String ?: ""
            val contatos = parseContatos(data["contatos"]) // retrocompatível
            val endereco = data["endereco"] as? String ?: ""
            val cidade = data["cidade"] as? String ?: ""
            val estado = data["estado"] as? String ?: ""
            val telefone = data["telefone"] as? String ?: ""
            val celular = data["celular"] as? String ?: ""
            val latitude = (data["latitude"] as? Number)?.toDouble()
            val longitude = (data["longitude"] as? Number)?.toDouble()
            Cliente(
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
        } catch (_: Exception) {
            null
        }
    }

    private fun Cliente.toEntity(): ClienteEntity = ClienteEntity(
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

    private fun ClienteEntity.toDomain(): Cliente = Cliente(
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

    suspend fun buscarClientes(): List<Cliente> = withContext(Dispatchers.IO) {
        // 1. Carrega do cache primeiro
        val locais = try { clienteDao.getAll().map { it.toDomain() } } catch (_: Exception) { emptyList() }

        // 2. Tenta atualizar do remoto
        return@withContext try {
            val snapshot = collection.get().await()
            val remotos = snapshot.documents.mapNotNull { documentToCliente(it) }
            // Substitui cache (estratégia simples)
            clienteDao.deleteAll()
            remotos.forEach { clienteDao.insert(it.toEntity()) }
            remotos
        } catch (_: Exception) {
            // Falhou remoto: retorna cache
            locais
        }
    }

    suspend fun buscarClientePorId(id: String): Cliente? = withContext(Dispatchers.IO) {
        // Cache primeiro
        val local = try { clienteDao.getById(id)?.toDomain() } catch (_: Exception) { null }
        // Tenta remoto
        return@withContext try {
            val document = collection.document(id).get().await()
            val remoto = documentToCliente(document)
            if (remoto != null) clienteDao.insert(remoto.toEntity())
            remoto ?: local
        } catch (_: Exception) { local }
    }

    suspend fun salvarCliente(cliente: Cliente): String = withContext(Dispatchers.IO) {
        // Salva remoto primeiro
        val savedId = try {
            val contatosMap = cliente.contatos.map {
                mapOf(
                    "nome" to it.nome,
                    "setor" to it.setor,
                    "celular" to it.celular
                )
            }
            val payload = mapOf(
                "nome" to cliente.nome,
                "cnpjCpf" to cliente.cnpjCpf,
                "contatos" to contatosMap,
                "endereco" to cliente.endereco,
                "cidade" to cliente.cidade,
                "estado" to cliente.estado,
                "telefone" to cliente.telefone,
                "celular" to cliente.celular,
                "latitude" to cliente.latitude,
                "longitude" to cliente.longitude
            )
            if (cliente.id.isEmpty()) {
                val documentRef = collection.add(payload).await()
                documentRef.id
            } else {
                collection.document(cliente.id).set(payload).await()
                cliente.id
            }
        } catch (e: Exception) {
            throw Exception("Erro ao salvar cliente remoto: ${e.message}")
        }
        // Atualiza cache local
        val finalId = if (cliente.id.isEmpty()) savedId else cliente.id
        clienteDao.insert(cliente.copy(id = finalId).toEntity())
        finalId
    }

    suspend fun excluirCliente(id: String) = withContext(Dispatchers.IO) {
        try { collection.document(id).delete().await() } catch (_: Exception) {}
        try { clienteDao.getById(id)?.let { clienteDao.delete(it) } } catch (_: Exception) {}
    }

    suspend fun sincronizarTudo(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = collection.get().await()
            val remotos = snapshot.documents.mapNotNull { documentToCliente(it) }
            clienteDao.deleteAll()
            remotos.forEach { clienteDao.insert(it.toEntity()) }
            true
        } catch (_: Exception) { false }
    }
}
