package com.example.aprimortech.data.remote

import android.util.Log
import com.example.aprimortech.model.Maquina
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

class MaquinaRemoteDataSource {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("maquinas")

    companion object { private const val TAG = "MaquinaRemoteDataSource" }

    suspend fun getAll(): List<Maquina> {
        return try {
            Log.d(TAG, "Buscando todas as máquinas do Firestore...")
            val snapshot = collection.get().await()
            val maquinas = snapshot.documents.mapNotNull { it.toObject(Maquina::class.java)?.copy(id = it.id) }
            Log.d(TAG, "Encontradas ${maquinas.size} máquinas no Firestore")
            maquinas
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "FirestoreException ao buscar máquinas: code=${e.code}", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Erro genérico ao buscar máquinas", e)
            emptyList()
        }
    }

    suspend fun insert(maquina: Maquina): Boolean {
        return try {
            if (maquina.id.isBlank()) {
                Log.e(TAG, "ID da máquina vazio")
                false
            } else {
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Log.e(TAG, "AUTH_REQUIRED: Usuário não autenticado - não pode salvar máquina")
                    false
                } else {
                    val data = mapOf(
                        "clienteId" to maquina.clienteId,
                        "fabricante" to maquina.fabricante,
                        "numeroSerie" to maquina.numeroSerie,
                        "modelo" to maquina.modelo,
                        "identificacao" to maquina.identificacao,
                        "anoFabricacao" to maquina.anoFabricacao,
                        "codigoTinta" to maquina.codigoTinta,
                        "codigoSolvente" to maquina.codigoSolvente,
                        "dataProximaPreventiva" to maquina.dataProximaPreventiva,
                        "criadoPor" to currentUser.uid,
                        "criadoEm" to com.google.firebase.Timestamp.now()
                    )
                    Log.d(TAG, "Tentando salvar máquina (id=${maquina.id}) no Firestore...")
                    collection.document(maquina.id).set(data).await()
                    Log.d(TAG, "Máquina salva com sucesso (id=${maquina.id})")
                    true
                }
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e(
                TAG,
                "Falha Firestore ao salvar máquina (code=${e.code}, cause=${e.cause?.javaClass?.simpleName}): ${e.message}",
                e
            )
            false
        } catch (e: Exception) {
            Log.e(TAG, "Erro genérico ao salvar máquina", e)
            false
        }
    }

    suspend fun update(maquina: Maquina): Boolean {
        return try {
            val data = mapOf(
                "clienteId" to maquina.clienteId,
                "fabricante" to maquina.fabricante,
                "numeroSerie" to maquina.numeroSerie,
                "modelo" to maquina.modelo,
                "identificacao" to maquina.identificacao,
                "anoFabricacao" to maquina.anoFabricacao,
                "codigoTinta" to maquina.codigoTinta,
                "codigoSolvente" to maquina.codigoSolvente,
                "dataProximaPreventiva" to maquina.dataProximaPreventiva
            )
            Log.d(TAG, "Tentando atualizar máquina (id=${maquina.id}) no Firestore...")
            collection.document(maquina.id).set(data).await()
            Log.d(TAG, "Máquina atualizada (id=${maquina.id})")
            true
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Falha Firestore ao atualizar máquina (code=${e.code}): ${e.message}", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Erro genérico ao atualizar máquina", e)
            false
        }
    }

    suspend fun delete(id: String): Boolean {
        return try {
            Log.d(TAG, "Tentando excluir máquina (id=$id) do Firestore...")
            collection.document(id).delete().await()
            Log.d(TAG, "Máquina excluída (id=$id)")
            true
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Falha Firestore ao excluir máquina (code=${e.code}): ${e.message}", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Erro genérico ao excluir máquina", e)
            false
        }
    }
}
