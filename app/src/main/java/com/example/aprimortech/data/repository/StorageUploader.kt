package com.example.aprimortech.data.repository

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * Pequeno utilitário para upload de imagens (em Base64) para o Firebase Storage.
 * - Retorna a URL pública de download quando o upload é bem sucedido.
 * - Retorna null se ocorrer qualquer erro (o chamador pode manter a string original em cache/local).
 */
object StorageUploader {
    // Usar bucket específico fornecido pelo usuário
    private val storage = FirebaseStorage.getInstance("gs://aprimortech-30cad.firebasestorage.app")
    private val auth = FirebaseAuth.getInstance()

    private suspend fun ensureSignedInIfNeeded() {
        try {
            val current = auth.currentUser
            if (current == null) {
                Log.d("StorageUploader", "Usuário não autenticado — tentando signInAnonymously()")
                try {
                    auth.signInAnonymously().await()
                    Log.d("StorageUploader", "Autenticação anônima bem sucedida: uid=${auth.currentUser?.uid}")
                } catch (ex: Exception) {
                    Log.w("StorageUploader", "Falha ao autenticar anonimamente: ${ex.message}", ex)
                }
            } else {
                Log.d("StorageUploader", "Usuário já autenticado: uid=${current.uid}")
            }
        } catch (ex: Exception) {
            Log.w("StorageUploader", "Erro ao verificar/realizar autenticação: ${ex.message}", ex)
        }
    }

    suspend fun uploadBase64Image(base64Input: String, folderPrefix: String = "relatorios/drafts"): String? {
        // Garantir autenticação (útil se regras exigem auth)
        ensureSignedInIfNeeded()

        return try {
            // Limpar eventual header data:image/...;base64,
            val cleaned = base64Input.substringAfter("base64,").replace("\n", "").replace("\r", "").trim()

            // Tentar decodificar
            val decoded: ByteArray = try {
                Base64.decode(cleaned, Base64.DEFAULT)
            } catch (e: Exception) {
                Log.w("StorageUploader", "Falha ao decodificar Base64 direto: ${e.message}", e)
                // fallback: tentar sem limpeza
                try {
                    Base64.decode(base64Input.replace("\\s".toRegex(), ""), Base64.DEFAULT)
                } catch (ex: Exception) {
                    Log.e("StorageUploader", "Não foi possível decodificar base64", ex)
                    return null
                }
            }

            // Tentar transformar em bitmap e recomprimir para jpeg (reduz tamanho)
            val baos = ByteArrayOutputStream()
            try {
                val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                if (bitmap != null) {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos)
                } else {
                    baos.write(decoded)
                }
            } catch (_: Exception) {
                // se falhar ao decodificar bitmap, subimos os bytes originais
                baos.write(decoded)
            }

            val finalBytes = baos.toByteArray()

            val fileName = "foto_${UUID.randomUUID()}.jpg"
            val path = "$folderPrefix/$fileName"
            val ref = storage.reference.child(path)

            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

            // DEBUG: informar bucket e path antes do upload
            try {
                val bucketName = storage.reference.bucket
                Log.d("StorageUploader", "Iniciando upload -> bucket=$bucketName, path=$path, bytes=${finalBytes.size}")
            } catch (_: Exception) {
            }

            ref.putBytes(finalBytes, metadata).await()

            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d("StorageUploader", "Upload ok: $downloadUrl (path=$path)")
            downloadUrl
        } catch (e: Exception) {
            Log.w("StorageUploader", "Erro no upload da imagem: ${e.message}", e)
            null
        }
    }
}
