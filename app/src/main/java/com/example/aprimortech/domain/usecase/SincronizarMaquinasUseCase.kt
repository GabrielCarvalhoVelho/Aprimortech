package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.MaquinaRepository

class SincronizarMaquinasUseCase(private val repository: MaquinaRepository) {
    suspend operator fun invoke(): Boolean {
        return try {
            // Sincroniza máquinas pendentes com Firebase
            repository.sincronizarComFirebase()
            true
        } catch (e: Exception) {
            false
        }
    }
}
