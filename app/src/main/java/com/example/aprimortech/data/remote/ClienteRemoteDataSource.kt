package com.example.aprimortech.data.remote

import com.example.aprimortech.model.Cliente
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ClienteRemoteDataSource(private val db: FirebaseFirestore) {
    private val collection = db.collection("clientes")

    suspend fun getAll(): List<Cliente> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Cliente::class.java)?.copy(id = it.id) }
    }

    suspend fun getById(id: String): Cliente? {
        val doc = collection.document(id).get().await()
        return doc.toObject(Cliente::class.java)?.copy(id = doc.id)
    }

    suspend fun insert(cliente: Cliente) {
        collection.document(cliente.id).set(cliente).await()
    }

    suspend fun update(cliente: Cliente) {
        collection.document(cliente.id).set(cliente).await()
    }

    suspend fun delete(id: String) {
        collection.document(id).delete().await()
    }
}
