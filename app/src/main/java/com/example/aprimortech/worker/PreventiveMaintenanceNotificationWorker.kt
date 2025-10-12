package com.example.aprimortech.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aprimortech.R
import com.example.aprimortech.data.repository.RelatorioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Worker para verificar manutenções preventivas próximas
 * ATUALIZADO: Agora busca informações de preventiva dos RELATÓRIOS ao invés das máquinas
 */
class PreventiveMaintenanceNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("PreventiveWorker", "=== VERIFICANDO MANUTENÇÕES PREVENTIVAS (dos relatórios) ===")

            // Obter repository de relatórios
            val app = applicationContext as com.example.aprimortech.AprimortechApplication
            val relatorioRepository = app.relatorioRepository

            val hoje = getCurrentDate()
            val alvo = addMonthsToDate(hoje, 1) // 1 mês à frente

            android.util.Log.d("PreventiveWorker", "Data hoje: $hoje")
            android.util.Log.d("PreventiveWorker", "Data alvo (1 mês): $alvo")

            // Buscar relatórios com datas de preventiva próximas
            // TODO: Implementar método específico no repository para buscar relatórios com preventiva próxima
            android.util.Log.d("PreventiveWorker", "Funcionalidade de notificação de preventiva temporariamente desabilitada")
            android.util.Log.d("PreventiveWorker", "Aguardando implementação de busca por data de preventiva nos relatórios")

            android.util.Log.d("PreventiveWorker", "=== VERIFICAÇÃO CONCLUÍDA ===")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("PreventiveWorker", "Erro na verificação de manutenções preventivas", e)
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Manutenções Preventivas"
            val descriptionText = "Notificações sobre manutenções preventivas próximas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        return String.format("%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun addMonthsToDate(date: String, months: Int): String {
        val parts = date.split("-")
        val calendar = Calendar.getInstance().apply {
            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            add(Calendar.MONTH, months)
        }
        return String.format("%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    companion object {
        private const val CHANNEL_ID = "preventive_maintenance_channel"
        private const val NOTIFICATION_ID_BASE = 1000
    }
}
