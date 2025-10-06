package com.example.aprimortech.data.repository

import com.example.aprimortech.data.local.entity.MaquinaEntity
import com.example.aprimortech.model.Maquina
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaquinaRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("maquinas")

    // Métodos para compatibilidade com Use Cases existentes
    suspend fun getAllLocal(): List<MaquinaEntity> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Maquina::class.java)?.copy(id = document.id)?.toEntity()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun salvarMaquina(maquina: MaquinaEntity): Boolean {
        return try {
            val maquinaDomain = maquina.toDomain()
            if (maquina.id.isEmpty()) {
                collection.add(maquinaDomain).await()
            } else {
                collection.document(maquina.id).set(maquinaDomain).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun excluirMaquina(maquina: MaquinaEntity): Boolean {
        return try {
            if (maquina.id.isNotEmpty()) {
                collection.document(maquina.id).delete().await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun buscarMaquinasLocal(): List<Maquina> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Maquina::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Métodos originais do Firebase
    suspend fun buscarMaquinas(): List<Maquina> = buscarMaquinasLocal()

    suspend fun buscarMaquinaPorId(id: String): Maquina? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Maquina::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun buscarMaquinasPorCliente(clienteId: String): List<Maquina> {
        return try {
            val snapshot = collection
                .whereEqualTo("clienteId", clienteId)
                .get()
                .await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Maquina::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Métodos de conversão
    private fun Maquina.toEntity(): MaquinaEntity {
        return MaquinaEntity(
            id = id,
            clienteId = clienteId,
            nomeMaquina = nomeMaquina,
            fabricante = fabricante,
            numeroSerie = numeroSerie,
            modelo = modelo,
            identificacao = identificacao,
            anoFabricacao = anoFabricacao,
            codigoTinta = codigoTinta,
            codigoSolvente = codigoSolvente,
            dataProximaPreventiva = dataProximaPreventiva,
            codigoConfiguracao = codigoConfiguracao
        )
    }

    private fun MaquinaEntity.toDomain(): Maquina {
        return Maquina(
            id = id,
            clienteId = clienteId,
            nomeMaquina = nomeMaquina,
            fabricante = fabricante,
            numeroSerie = numeroSerie,
            modelo = modelo,
            identificacao = identificacao,
            anoFabricacao = anoFabricacao,
            codigoTinta = codigoTinta,
            codigoSolvente = codigoSolvente,
            dataProximaPreventiva = dataProximaPreventiva,
            codigoConfiguracao = codigoConfiguracao
        )
    }
}
