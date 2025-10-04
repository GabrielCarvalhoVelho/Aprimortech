package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.PecaRepository
import com.example.aprimortech.model.Peca
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BuscarPecasUseCase @Inject constructor(
    private val repository: PecaRepository
) {
    operator fun invoke(): Flow<List<Peca>> {
        return repository.buscarTodasPecas()
    }
}
