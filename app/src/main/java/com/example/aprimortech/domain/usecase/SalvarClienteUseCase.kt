package com.example.aprimortech.domain.usecase

import android.util.Log
import com.example.aprimortech.data.repository.ClienteRepository
import com.example.aprimortech.data.local.entity.ClienteEntity

class SalvarClienteUseCase(private val repository: ClienteRepository) {
    companion object {
        private const val TAG = "SalvarClienteUseCase"
    }

    suspend operator fun invoke(cliente: ClienteEntity): Boolean {
        return try {
            Log.d(TAG, "Iniciando salvamento do cliente: ${cliente.nome}")

            // Usar o método unificado do repository que salva tanto local quanto remoto
            val sucesso = repository.salvarCliente(cliente)

            if (sucesso) {
                Log.d(TAG, "Cliente salvo com sucesso em ambos os locais")
            } else {
                Log.w(TAG, "Cliente salvo localmente, mas houve problema com o Firestore")
            }

            sucesso
        } catch (e: Exception) {
            Log.e(TAG, "Erro no UseCase ao salvar cliente: ${cliente.nome}", e)
            false
        }
    }
}
