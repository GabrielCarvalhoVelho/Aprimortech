package com.example.aprimortech.data.repository

import android.util.Log
import com.example.aprimortech.data.local.dao.MaquinaDao
import com.example.aprimortech.data.local.entity.MaquinaEntity
import com.example.aprimortech.data.remote.MaquinaRemoteDataSource
import com.example.aprimortech.model.Maquina
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MaquinaRepository(
    private val maquinaDao: MaquinaDao,
    private val remoteDataSource: MaquinaRemoteDataSource
) {
    companion object { private const val TAG = "MaquinaRepository" }

    suspend fun getAllLocal(): List<MaquinaEntity> = withContext(Dispatchers.IO) { maquinaDao.getAll() }
    suspend fun getAllRemote(): List<Maquina> = withContext(Dispatchers.IO) { remoteDataSource.getAll() }

    suspend fun insertLocal(maquina: MaquinaEntity) = withContext(Dispatchers.IO) { maquinaDao.insert(maquina) }
    suspend fun insertRemote(maquina: Maquina): Boolean = withContext(Dispatchers.IO) { remoteDataSource.insert(maquina) }

    suspend fun updateLocal(maquina: MaquinaEntity) = withContext(Dispatchers.IO) { maquinaDao.update(maquina) }
    suspend fun updateRemote(maquina: Maquina): Boolean = withContext(Dispatchers.IO) { remoteDataSource.update(maquina) }

    suspend fun deleteLocal(maquina: MaquinaEntity) = withContext(Dispatchers.IO) { maquinaDao.delete(maquina) }
    suspend fun deleteRemote(id: String): Boolean = withContext(Dispatchers.IO) { remoteDataSource.delete(id) }

    // Métodos para buscar valores únicos já utilizados (autocomplete)
    suspend fun getFabricantesUtilizados(): List<String> = withContext(Dispatchers.IO) {
        try {
            maquinaDao.getAll().map { it.fabricante }.filter { it.isNotBlank() }.distinct().sorted()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar fabricantes", e)
            emptyList()
        }
    }

    suspend fun getModelosUtilizados(): List<String> = withContext(Dispatchers.IO) {
        try {
            maquinaDao.getAll().map { it.modelo }.filter { it.isNotBlank() }.distinct().sorted()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar modelos", e)
            emptyList()
        }
    }

    suspend fun getCodigosTintaUtilizados(): List<String> = withContext(Dispatchers.IO) {
        try {
            maquinaDao.getAll().map { it.codigoTinta }.filter { it.isNotBlank() }.distinct().sorted()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar códigos de tinta", e)
            emptyList()
        }
    }

    suspend fun getCodigosSolventeUtilizados(): List<String> = withContext(Dispatchers.IO) {
        try {
            maquinaDao.getAll().map { it.codigoSolvente }.filter { it.isNotBlank() }.distinct().sorted()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar códigos de solvente", e)
            emptyList()
        }
    }

    // Verificar se número de série já existe
    suspend fun numeroSerieJaExiste(numeroSerie: String, excludeId: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val existentes = maquinaDao.getAll().filter {
                it.numeroSerie.equals(numeroSerie, ignoreCase = true) && it.id != excludeId
            }
            existentes.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar número de série", e)
            false
        }
    }

    suspend fun salvarMaquina(maquina: MaquinaEntity): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Salvando máquina: ${maquina.nomeMaquina}")
            maquinaDao.insert(maquina)
            val dominio = maquina.toDomain()
            val sucesso = remoteDataSource.insert(dominio)
            if (!sucesso) Log.w(TAG, "Máquina salva localmente mas falhou no Firestore")
            sucesso
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar máquina", e)
            false
        }
    }

    suspend fun sincronizarDadosExistentes(): Int = withContext(Dispatchers.IO) {
        return@withContext try {
            val locais = maquinaDao.getAll()
            val remotos = remoteDataSource.getAll().map { it.id }.toSet()
            val pendentes = locais.filter { it.id !in remotos }
            var count = 0
            pendentes.forEach { if (remoteDataSource.insert(it.toDomain())) count++ }
            count
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao sincronizar máquinas", e)
            0
        }
    }

    private fun MaquinaEntity.toDomain() = Maquina(
        id = id,
        clienteId = clienteId,
        nomeMaquina = nomeMaquina,
        fabricante = fabricante,
        numeroSerie = numeroSerie,
        modelo = modelo,
        identificacao = identificacao,
        anoFabricacao = anoFabricacao,
        codigoTinta = codigoTinta,
        codigoSolvente = codigoSolvente,
        dataProximaPreventiva = dataProximaPreventiva,
        codigoConfiguracao = codigoConfiguracao
    )
}
