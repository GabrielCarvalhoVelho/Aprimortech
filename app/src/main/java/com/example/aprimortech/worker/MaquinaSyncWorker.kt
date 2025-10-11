package com.example.aprimortech.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.aprimortech.AprimortechApplication
import java.util.concurrent.TimeUnit

/**
 * Worker para sincronização automática de máquinas em background
 * Sincroniza dados quando o dispositivo está online
 */
class MaquinaSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "MaquinaSyncWorker"
        private const val WORK_NAME = "maquina_sync_work"
        private const val PERIODIC_WORK_NAME = "maquina_periodic_sync"

        /**
         * Executa sincronização imediata (one-time)
         */
        fun syncNow(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<MaquinaSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED) // Só executa com rede
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )

            Log.d(TAG, "🔄 Sincronização imediata agendada")
        }

        /**
         * Agenda sincronização periódica (a cada 15 minutos quando houver rede)
         */
        fun schedulePeriodicSync(context: Context) {
            val periodicWorkRequest = PeriodicWorkRequestBuilder<MaquinaSyncWorker>(
                15, TimeUnit.MINUTES // Mínimo permitido pelo Android
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP, // Mantém se já existir
                    periodicWorkRequest
                )

            Log.d(TAG, "⏰ Sincronização periódica agendada (a cada 15 minutos)")
        }

        /**
         * Cancela todas as sincronizações agendadas
         */
        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
            Log.d(TAG, "❌ Sincronizações canceladas")
        }
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "🔄 Iniciando sincronização em background...")

            val app = applicationContext as? AprimortechApplication
            if (app == null) {
                Log.e(TAG, "❌ Erro: AprimortechApplication não encontrado")
                return Result.failure()
            }

            // Sincroniza máquinas pendentes
            val maquinasSincronizadas = app.maquinaRepository.sincronizarMaquinasPendentes()

            // Atualiza cache local com dados do Firebase
            app.maquinaRepository.sincronizarComFirebase()

            Log.d(TAG, "✅ Sincronização concluída: $maquinasSincronizadas máquinas sincronizadas")

            // Notifica sucesso
            setProgress(
                workDataOf(
                    "sincronizados" to maquinasSincronizadas,
                    "timestamp" to System.currentTimeMillis()
                )
            )

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro na sincronização em background", e)

            // Tenta novamente se for um erro temporário
            if (runAttemptCount < 3) {
                Log.d(TAG, "⚠️ Tentativa ${runAttemptCount + 1}/3 - Reagendando...")
                Result.retry()
            } else {
                Log.e(TAG, "❌ Falha após 3 tentativas")
                Result.failure()
            }
        }
    }
}

