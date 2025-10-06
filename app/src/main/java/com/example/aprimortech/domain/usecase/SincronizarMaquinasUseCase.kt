package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.MaquinaRepository

class SincronizarMaquinasUseCase(private val repository: MaquinaRepository) {
    suspend operator fun invoke(): Boolean {
        return try {
            // Como estamos usando apenas Firebase, não há sincronização necessária
            // Retorna true indicando que a "sincronização" foi bem-sucedida
            true
        } catch (e: Exception) {
            false
        }
    }
}
