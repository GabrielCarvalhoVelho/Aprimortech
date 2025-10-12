package com.example.aprimortech.domain.usecase

import com.example.aprimortech.data.repository.MaquinaRepository
import com.example.aprimortech.model.Maquina
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class BuscarProximasManutencoesPreventivasUseCase @Inject constructor(
    private val maquinaRepository: MaquinaRepository
) {
    suspend operator fun invoke(diasAntecedencia: Int = 30): List<Maquina> {
        return try {
            val maquinas = maquinaRepository.buscarMaquinasLocal()
            val hoje = Calendar.getInstance()
            val dataLimite = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, diasAntecedencia)
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            maquinas.filter { maquina ->
                try {
                    if (maquina.dataProximaPreventiva.isNotBlank()) {
                        val dataPreventiva = Calendar.getInstance().apply {
                            time = dateFormat.parse(maquina.dataProximaPreventiva) ?: return@filter false
                        }
                        dataPreventiva.after(hoje) && !dataPreventiva.after(dataLimite)
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
