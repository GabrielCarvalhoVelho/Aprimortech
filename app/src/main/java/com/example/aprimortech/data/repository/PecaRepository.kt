package com.example.aprimortech.data.repository

import com.example.aprimortech.data.local.dao.PecaDao
import com.example.aprimortech.data.local.entity.PecaEntity
import com.example.aprimortech.data.remote.PecaRemoteDataSource
import com.example.aprimortech.model.Peca
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PecaRepository @Inject constructor(
    private val pecaDao: PecaDao,
    private val remoteDataSource: PecaRemoteDataSource
) {
    fun buscarTodasPecas(): Flow<List<Peca>> {
        return pecaDao.buscarTodasPecas().map { entidades ->
            entidades.map { entidade -> entidade.toPeca() }
        }
    }

    suspend fun buscarPecaPorId(id: String): Peca? {
        return pecaDao.buscarPecaPorId(id)?.toPeca()
    }

    fun buscarFabricantesDisponiveis(): Flow<List<String>> {
        return pecaDao.buscarFabricantesDisponiveis()
    }

    fun buscarCategoriasDisponiveis(): Flow<List<String>> {
        return pecaDao.buscarCategoriasDisponiveis()
    }

    suspend fun salvarPeca(peca: Peca) {
        try {
            android.util.Log.d("PecaRepository", "=== INICIANDO SALVAMENTO DE PEÇA ===")
            android.util.Log.d("PecaRepository", "Peça: ${peca.nome}, ID: ${peca.id}")
            android.util.Log.d("PecaRepository", "Dados completos: $peca")

            // Salva localmente
            android.util.Log.d("PecaRepository", "Salvando peça localmente...")
            pecaDao.inserirPeca(peca.toPecaEntity())
            android.util.Log.d("PecaRepository", "Peça salva localmente com sucesso")

            // Salva no Firebase
            android.util.Log.d("PecaRepository", "Salvando peça no Firebase...")
            remoteDataSource.salvarPeca(peca)
            android.util.Log.d("PecaRepository", "Peça salva no Firebase com sucesso")
            android.util.Log.d("PecaRepository", "=== SALVAMENTO CONCLUÍDO ===")
        } catch (e: Exception) {
            android.util.Log.e("PecaRepository", "=== ERRO NO SALVAMENTO ===", e)
            android.util.Log.e("PecaRepository", "Peça que falhou: ${peca.nome}")
            throw e
        }
    }

    suspend fun salvarPecas(pecas: List<Peca>) {
        pecaDao.inserirPecas(pecas.map { it.toPecaEntity() })
    }

    suspend fun excluirPeca(peca: Peca) {
        try {
            android.util.Log.d("PecaRepository", "=== INICIANDO EXCLUSÃO DE PEÇA ===")
            android.util.Log.d("PecaRepository", "Peça: ${peca.nome}, ID: ${peca.id}")

            // Exclui localmente
            android.util.Log.d("PecaRepository", "Excluindo peça localmente...")
            pecaDao.excluirPeca(peca.toPecaEntity())
            android.util.Log.d("PecaRepository", "Peça excluída localmente com sucesso")

            // Exclui do Firebase
            if (peca.id.isNotEmpty()) {
                android.util.Log.d("PecaRepository", "Excluindo peça do Firebase...")
                remoteDataSource.excluirPeca(peca.id)
                android.util.Log.d("PecaRepository", "Peça excluída do Firebase com sucesso")
            } else {
                android.util.Log.w("PecaRepository", "ID da peça está vazio, não será excluída do Firebase")
            }
            android.util.Log.d("PecaRepository", "=== EXCLUSÃO CONCLUÍDA ===")
        } catch (e: Exception) {
            android.util.Log.e("PecaRepository", "=== ERRO NA EXCLUSÃO ===", e)
            throw e
        }
    }

    suspend fun sincronizarPecas(pecas: List<Peca>) {
        try {
            android.util.Log.d("PecaRepository", "=== INICIANDO SINCRONIZAÇÃO ===")
            android.util.Log.d("PecaRepository", "Quantidade de peças: ${pecas.size}")
            pecas.forEachIndexed { index, peca ->
                android.util.Log.d("PecaRepository", "Peça $index: ${peca.nome} (ID: ${peca.id})")
            }

            // Sincroniza com Firebase
            android.util.Log.d("PecaRepository", "Sincronizando com Firebase...")
            remoteDataSource.sincronizarPecas(pecas)
            android.util.Log.d("PecaRepository", "Sincronização com Firebase concluída")

            // Atualiza dados locais
            android.util.Log.d("PecaRepository", "Atualizando dados locais...")
            pecaDao.excluirTodasPecas()
            android.util.Log.d("PecaRepository", "Dados locais limpos")
            pecaDao.inserirPecas(pecas.map { it.toPecaEntity() })
            android.util.Log.d("PecaRepository", "Dados locais atualizados")
            android.util.Log.d("PecaRepository", "=== SINCRONIZAÇÃO CONCLUÍDA ===")
        } catch (e: Exception) {
            android.util.Log.e("PecaRepository", "=== ERRO NA SINCRONIZAÇÃO ===", e)
            throw e
        }
    }

    suspend fun buscarPecasDoServidor(): List<Peca> {
        return try {
            android.util.Log.d("PecaRepository", "=== BUSCANDO PEÇAS DO SERVIDOR ===")
            val pecasRemotas = remoteDataSource.buscarTodasPecas()
            android.util.Log.d("PecaRepository", "Peças encontradas no servidor: ${pecasRemotas.size}")

            // Atualiza cache local
            android.util.Log.d("PecaRepository", "Atualizando cache local...")
            pecaDao.excluirTodasPecas()
            pecaDao.inserirPecas(pecasRemotas.map { it.toPecaEntity() })
            android.util.Log.d("PecaRepository", "Cache local atualizado")
            android.util.Log.d("PecaRepository", "=== BUSCA CONCLUÍDA ===")
            pecasRemotas
        } catch (e: Exception) {
            android.util.Log.e("PecaRepository", "=== ERRO NA BUSCA DO SERVIDOR ===", e)
            emptyList()
        }
    }
}

// Extension functions para conversão entre entidades
private fun PecaEntity.toPeca(): Peca {
    return Peca(
        id = id,
        nome = nome,
        codigo = codigo,
        descricao = descricao,
        fabricante = fabricante,
        categoria = categoria,
        preco = preco
    )
}

private fun Peca.toPecaEntity(): PecaEntity {
    return PecaEntity(
        id = id,
        nome = nome,
        codigo = codigo,
        descricao = descricao,
        fabricante = fabricante,
        categoria = categoria,
        preco = preco
    )
}
