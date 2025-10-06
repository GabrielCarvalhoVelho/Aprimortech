package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.MaquinaRepository
import com.example.aprimortech.model.Maquina
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

class BuscarProximasManutencoesPreventivasUseCase @Inject constructor(
    private val maquinaRepository: MaquinaRepository
) {
    suspend operator fun invoke(diasAntecedencia: Int = 30): List<Maquina> {
        return try {
            val maquinas = maquinaRepository.buscarMaquinasLocal()
            val hoje = LocalDate.now()
            val dataLimite = hoje.plusDays(diasAntecedencia.toLong())

            maquinas.filter { maquina ->
                try {
                    if (maquina.dataProximaPreventiva.isNotBlank()) {
                        val dataPreventiva = LocalDate.parse(maquina.dataProximaPreventiva, DateTimeFormatter.ISO_LOCAL_DATE)
                        dataPreventiva.isAfter(hoje) && !dataPreventiva.isAfter(dataLimite)
                    } else {
                        false
                    }
                } catch (e: DateTimeParseException) {
                    false
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
