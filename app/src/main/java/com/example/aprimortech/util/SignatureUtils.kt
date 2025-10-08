package com.example.aprimortech.util

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.Base64
import java.io.ByteArrayOutputStream

object SignatureUtils {
    fun convertSignatureToBitmap(
        paths: SnapshotStateList<SnapshotStateList<Offset>>,
        width: Int = 400,
        height: Int = 200
    ): Bitmap? {
        if (paths.isEmpty() || paths.all { it.isEmpty() }) return null
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fundo branco
        canvas.drawColor(Color.WHITE)

        // Desenhar as linhas da assinatura
        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 4f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        paths.forEach { pathPoints ->
            if (pathPoints.size > 1) {
                val path = Path()
                path.moveTo(pathPoints[0].x, pathPoints[0].y)
                for (i in 1 until pathPoints.size) {
                    path.lineTo(pathPoints[i].x, pathPoints[i].y)
                }
                canvas.drawPath(path, paint)
            }
        }
        return bitmap
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun signatureToBase64(paths: SnapshotStateList<SnapshotStateList<Offset>>): String? {
        val bitmap = convertSignatureToBitmap(paths) ?: return null
        return bitmapToBase64(bitmap)
    }

    /**
     * Converte assinatura para Bitmap (para upload no Firebase Storage)
     */
    fun signatureToBitmap(paths: SnapshotStateList<SnapshotStateList<Offset>>): Bitmap? {
        android.util.Log.d("SignatureUtils", "=== signatureToBitmap CHAMADO ===")
        android.util.Log.d("SignatureUtils", "Número de paths: ${paths.size}")

        paths.forEachIndexed { index, path ->
            android.util.Log.d("SignatureUtils", "Path $index tem ${path.size} pontos")
        }

        val bitmap = convertSignatureToBitmap(paths)
        android.util.Log.d("SignatureUtils", "Bitmap criado: ${bitmap != null}")
        if (bitmap != null) {
            android.util.Log.d("SignatureUtils", "Bitmap dimensões: ${bitmap.width}x${bitmap.height}")
        }

        return bitmap
    }
}
