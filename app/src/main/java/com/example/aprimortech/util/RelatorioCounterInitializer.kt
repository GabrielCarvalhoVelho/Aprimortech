package com.example.aprimortech.util

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

/**
 * Utilitário para inicializar o contador de relatórios no Firestore.
 *
 * IMPORTANTE: Este método deve ser executado apenas UMA VEZ, antes de criar
 * o primeiro relatório. Pode ser chamado no primeiro login do administrador
 * ou durante a configuração inicial do app.
 */
object RelatorioCounterInitializer {

    private const val TAG = "RelatorioCounterInit"

    /**
     * Inicializa o contador de relatórios com o valor inicial especificado.
     *
     * @param firestore Instância do FirebaseFirestore
     * @param valorInicial Valor inicial do contador (padrão: 0)
     * @param forcarReinicializacao Se true, sobrescreve o contador mesmo se já existir
     * @return true se inicializado com sucesso, false caso contrário
     */
    suspend fun inicializarContador(
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
        valorInicial: Long = 0,
        forcarReinicializacao: Boolean = false
    ): Boolean {
        return try {
            val counterDoc = firestore.collection("counters")
                .document("relatorio_counter")

            // Verificar se o contador já existe
            val snapshot = counterDoc.get().await()

            if (snapshot.exists() && !forcarReinicializacao) {
                val currentValue = snapshot.getLong("currentNumber") ?: 0
                Log.i(TAG, "Contador já existe com valor: $currentValue")
                Log.i(TAG, "Use forcarReinicializacao=true para sobrescrever")
                return true
            }

            // Criar ou sobrescrever o contador
            counterDoc.set(mapOf("currentNumber" to valorInicial)).await()

            Log.i(TAG, "✅ Contador inicializado com sucesso! Valor: $valorInicial")
            Log.i(TAG, "Próximo relatório receberá número: ${String.format("%04d", valorInicial + 1)}")

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao inicializar contador: ${e.message}", e)
            false
        }
    }

    /**
     * Verifica se o contador está inicializado e retorna o valor atual.
     *
     * @return Valor atual do contador, ou null se não existir
     */
    suspend fun obterValorAtual(
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Long? {
        return try {
            val snapshot = firestore.collection("counters")
                .document("relatorio_counter")
                .get()
                .await()

            val valor = snapshot.getLong("currentNumber")

            if (valor != null) {
                Log.i(TAG, "Valor atual do contador: $valor")
                Log.i(TAG, "Próximo número será: ${String.format("%04d", valor + 1)}")
            } else {
                Log.w(TAG, "Contador não encontrado! Execute inicializarContador() primeiro.")
            }

            valor
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter valor do contador: ${e.message}", e)
            null
        }
    }

    /**
     * Define manualmente o valor do contador.
     * Use com cuidado para evitar números duplicados!
     *
     * @param novoValor Novo valor do contador
     */
    suspend fun definirValor(
        novoValor: Long,
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Boolean {
        return try {
            firestore.collection("counters")
                .document("relatorio_counter")
                .set(mapOf("currentNumber" to novoValor))
                .await()

            Log.i(TAG, "⚠️ Contador redefinido para: $novoValor")
            Log.i(TAG, "Próximo número será: ${String.format("%04d", novoValor + 1)}")

            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao definir valor do contador: ${e.message}", e)
            false
        }
    }
}

