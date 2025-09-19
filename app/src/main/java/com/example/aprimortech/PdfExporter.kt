package com.example.aprimortech

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    fun exportRelatorio(
        context: Context,
        relatorio: RelatorioUiModel
    ): android.net.Uri {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 18f
            color = Color.BLACK
        }
        val labelPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            color = Color.DKGRAY
        }
        val textPaint = Paint().apply {
            textSize = 12f
            color = Color.BLACK
        }

        var y = 50
        val margin = 40

        // Título
        canvas.drawText("Relatório Técnico", margin.toFloat(), y.toFloat(), titlePaint)
        y += 40

        // Função auxiliar para imprimir pares campo/valor
        fun drawField(label: String, value: String) {
            canvas.drawText(label, margin.toFloat(), y.toFloat(), labelPaint)
            y += 16
            val lines = breakTextIntoLines(value, textPaint, 595 - margin * 2)
            for (line in lines) {
                canvas.drawText(line, margin.toFloat(), y.toFloat(), textPaint)
                y += 16
            }
            y += 8
        }

        // Todos os campos do RelatorioUiModel
        drawField("ID", relatorio.id.toString())
        drawField("Cliente", relatorio.cliente)
        drawField("Data", relatorio.data)
        drawField("Endereço", relatorio.endereco)
        drawField("Técnico", relatorio.tecnico)
        drawField("Setor", relatorio.setor)
        drawField("Contato", relatorio.contato)
        drawField("Equipamento", relatorio.equipamento)
        drawField("Peças Utilizadas", relatorio.pecasUtilizadas)
        drawField("Horas Trabalhadas", relatorio.horasTrabalhadas)
        drawField("Deslocamento", relatorio.deslocamento)
        drawField("Descrição do Serviço", relatorio.descricao)

        document.finishPage(page)

        // Salvar PDF em /Android/data/<pacote>/files/Documents/
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "relatorio_${relatorio.id}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // Quebra textos longos em várias linhas
    private fun breakTextIntoLines(text: String, paint: Paint, maxWidth: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) > maxWidth) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        return lines
    }
}
