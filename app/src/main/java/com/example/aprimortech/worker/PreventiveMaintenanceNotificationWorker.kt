package com.example.aprimortech.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aprimortech.R
import com.example.aprimortech.data.repository.MaquinaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeParseException

class PreventiveMaintenanceNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("PreventiveWorker", "=== VERIFICANDO MANUTENÇÕES PREVENTIVAS ===")

            // Obter repository via Application
            val app = applicationContext as com.example.aprimortech.AprimortechApplication
            val maquinaRepository = app.maquinaRepository

            val hoje = getCurrentDate()
            val alvo = addMonthsToDate(hoje, 1) // 1 mês à frente

            android.util.Log.d("PreventiveWorker", "Data hoje: $hoje")
            android.util.Log.d("PreventiveWorker", "Data alvo (1 mês): $alvo")

            // Buscar todas as máquinas (usando flow local se disponível)
            val maquinas = maquinaRepository.buscarMaquinasLocal() // Implementar método síncrono
            android.util.Log.d("PreventiveWorker", "Máquinas encontradas: ${maquinas.size}")

            val maquinasProximas = maquinas.filter { maquina ->
                try {
                    val dataPreventiva = maquina.dataProximaPreventiva
                    android.util.Log.d("PreventiveWorker", "Verificando máquina ${maquina.identificacao}: $dataPreventiva")
                    dataPreventiva == alvo
                } catch (e: Exception) {
                    android.util.Log.w("PreventiveWorker", "Erro ao parsear data da máquina ${maquina.identificacao}", e)
                    false
                }
            }

            android.util.Log.d("PreventiveWorker", "Máquinas com manutenção próxima: ${maquinasProximas.size}")

            if (maquinasProximas.isNotEmpty()) {
                createNotificationChannel()
                val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                maquinasProximas.forEachIndexed { index, maquina ->
                    val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification) // Será criado
                        .setContentTitle("Manutenção Preventiva Próxima")
                        .setContentText("Máquina ${maquina.identificacao} precisa de manutenção em 30 dias")
                        .setStyle(NotificationCompat.BigTextStyle()
                            .bigText("A máquina ${maquina.identificacao} (${maquina.modelo}) está programada para manutenção preventiva em 30 dias. Data: ${maquina.dataProximaPreventiva}"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(NOTIFICATION_ID_BASE + index, notification)
                    android.util.Log.d("PreventiveWorker", "Notificação enviada para máquina: ${maquina.identificacao}")
                }
            }

            android.util.Log.d("PreventiveWorker", "=== VERIFICAÇÃO CONCLUÍDA ===")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("PreventiveWorker", "Erro na verificação de manutenções preventivas", e)
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Manutenções Preventivas",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificações sobre manutenções preventivas próximas"
                    enableLights(true)
                    enableVibration(true)
                }

                notificationManager.createNotificationChannel(channel)
                android.util.Log.d("PreventiveWorker", "Canal de notificação criado")
            }
        }
    }

    private fun getCurrentDate(): String {
        return try {
            LocalDate.now().toString()
        } catch (e: Exception) {
            // Fallback para dispositivos que não suportam java.time
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            String.format("%04d-%02d-%02d", year, month, day)
        }
    }

    private fun addMonthsToDate(dateString: String, months: Int): String {
        return try {
            LocalDate.parse(dateString).plusMonths(months.toLong()).toString()
        } catch (e: Exception) {
            // Fallback usando Calendar
            val parts = dateString.split("-")
            if (parts.size == 3) {
                val calendar = java.util.Calendar.getInstance()
                calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                calendar.add(java.util.Calendar.MONTH, months)

                val year = calendar.get(java.util.Calendar.YEAR)
                val month = calendar.get(java.util.Calendar.MONTH) + 1
                val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                String.format("%04d-%02d-%02d", year, month, day)
            } else {
                dateString
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "preventive_maintenance_channel"
        private const val NOTIFICATION_ID_BASE = 1000
    }
}
