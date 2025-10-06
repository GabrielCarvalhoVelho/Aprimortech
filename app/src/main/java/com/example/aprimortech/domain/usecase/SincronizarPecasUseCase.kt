package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.PecaRepository

class SincronizarPecasUseCase(private val repository: PecaRepository) {
    suspend operator fun invoke(): Boolean {
        return try {
            // Como estamos usando apenas Firebase, não há sincronização necessária
            true
        } catch (e: Exception) {
            false
        }
    }
}
