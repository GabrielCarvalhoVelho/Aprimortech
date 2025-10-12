package com.example.aprimortech

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.aprimortech.model.RelatorioCompleto
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.*

object PdfExporter {

    /**
     * Exporta um relatório completo com todos os campos para PDF
     */
    fun exportRelatorioCompleto(
        context: Context,
        relatorio: RelatorioCompleto
    ): android.net.Uri {
        val document = PdfDocument()
        val pageWidth = 595 // A4
        val pageHeight = 842
        val margin = 40
        val contentWidth = pageWidth - (margin * 2)

        var currentPage = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var y = margin

        // Estilos de texto
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
            color = Color.BLACK
        }
        val sectionPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 14f
            color = Color.rgb(26, 74, 92) // Brand color
        }
        val labelPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 10f
            color = Color.DKGRAY
        }
        val textPaint = Paint().apply {
            textSize = 10f
            color = Color.BLACK
        }
        val smallTextPaint = Paint().apply {
            textSize = 9f
            color = Color.GRAY
        }

        // Função para verificar se precisa de nova página
        fun checkNewPage(neededSpace: Int): Boolean {
            if (y + neededSpace > pageHeight - margin) {
                document.finishPage(page)
                currentPage++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = margin
                return true
            }
            return false
        }

        // Função para desenhar campo
        fun drawField(label: String, value: String, indent: Int = 0) {
            checkNewPage(40)
            canvas.drawText(label, (margin + indent).toFloat(), y.toFloat(), labelPaint)
            y += 14
            val lines = breakTextIntoLines(value, textPaint, contentWidth - indent)
            for (line in lines) {
                checkNewPage(14)
                canvas.drawText(line, (margin + indent).toFloat(), y.toFloat(), textPaint)
                y += 14
            }
            y += 4
        }

        // Função para desenhar seção
        fun drawSection(title: String) {
            checkNewPage(40)
            y += 8
            canvas.drawText(title, margin.toFloat(), y.toFloat(), sectionPaint)
            y += 20
            // Linha separadora
            val linePaint = Paint().apply {
                color = Color.rgb(26, 74, 92)
                strokeWidth = 2f
            }
            canvas.drawLine(margin.toFloat(), y.toFloat(), (pageWidth - margin).toFloat(), y.toFloat(), linePaint)
            y += 12
        }

        // Cabeçalho
        canvas.drawText("RELATÓRIO TÉCNICO", margin.toFloat(), y.toFloat(), titlePaint)
        y += 30
        canvas.drawText("Data: ${relatorio.dataRelatorio}", margin.toFloat(), y.toFloat(), textPaint)
        y += 24

        // Seção Cliente
        drawSection("CLIENTE")
        drawField("Nome", relatorio.clienteNome)
        drawField("Endereço", relatorio.clienteEndereco)
        drawField("Cidade", relatorio.clienteCidade)
        drawField("Estado", relatorio.clienteEstado)
        drawField("Telefone", relatorio.clienteTelefone)
        drawField("Celular", relatorio.clienteCelular)

        if (relatorio.clienteContatos.isNotEmpty()) {
            checkNewPage(30)
            canvas.drawText("Contatos:", margin.toFloat(), y.toFloat(), labelPaint)
            y += 14
            relatorio.clienteContatos.forEach { contato ->
                checkNewPage(28)
                canvas.drawText("• ${contato.nome} - ${contato.setor}", (margin + 10).toFloat(), y.toFloat(), textPaint)
                y += 12
                if (contato.celular.isNotEmpty()) {
                    canvas.drawText("  ${contato.celular}", (margin + 15).toFloat(), y.toFloat(), smallTextPaint)
                    y += 12
                }
            }
            y += 6
        }

        // Seção Equipamento
        drawSection("DADOS DO EQUIPAMENTO")
        drawField("Fabricante", relatorio.equipamentoFabricante)
        drawField("Número de Série", relatorio.equipamentoNumeroSerie)
        drawField("Código de Configuração", relatorio.equipamentoCodigoConfiguracao)
        drawField("Modelo", relatorio.equipamentoModelo)
        drawField("Identificação", relatorio.equipamentoIdentificacao)
        drawField("Ano de Fabricação", relatorio.equipamentoAnoFabricacao)
        drawField("Código Tinta", relatorio.equipamentoCodigoTinta)
        drawField("Código Solvente", relatorio.equipamentoCodigoSolvente)
        drawField("Data Próxima Preventiva", relatorio.equipamentoDataProximaPreventiva)
        drawField("Hora Próxima Preventiva", relatorio.equipamentoHoraProximaPreventiva)

        // Seção Defeitos
        if (relatorio.defeitos.isNotEmpty()) {
            drawSection("DEFEITOS IDENTIFICADOS")
            relatorio.defeitos.forEachIndexed { index, defeito ->
                checkNewPage(18)
                canvas.drawText("${index + 1}. $defeito", margin.toFloat(), y.toFloat(), textPaint)
                y += 16
            }
        }

        // Seção Serviços
        if (relatorio.servicos.isNotEmpty()) {
            drawSection("SERVIÇOS REALIZADOS")
            relatorio.servicos.forEachIndexed { index, servico ->
                checkNewPage(18)
                canvas.drawText("${index + 1}. $servico", margin.toFloat(), y.toFloat(), textPaint)
                y += 16
            }
        }

        // Seção Peças
        if (relatorio.pecas.isNotEmpty()) {
            drawSection("PEÇAS UTILIZADAS")
            relatorio.pecas.forEach { peca ->
                checkNewPage(30)
                canvas.drawText("${peca.codigo} - ${peca.descricao}", margin.toFloat(), y.toFloat(), textPaint)
                y += 14
                canvas.drawText("Quantidade: ${peca.quantidade}", (margin + 10).toFloat(), y.toFloat(), smallTextPaint)
                y += 18
            }
        }

        // Seção Horas Técnicas
        drawSection("HORAS TÉCNICAS")
        drawField("Horário de Entrada", relatorio.horarioEntrada)
        drawField("Horário de Saída", relatorio.horarioSaida)
        drawField("Valor Hora Técnica", formatCurrency(relatorio.valorHoraTecnica))
        drawField("Total", formatCurrency(relatorio.totalHorasTecnicas))

        // Seção Deslocamento
        drawSection("DESLOCAMENTO")
        drawField("Quantidade de KM", "${relatorio.quantidadeKm} km")
        drawField("Valor por KM", formatCurrency(relatorio.valorPorKm))
        drawField("Valor dos Pedágios", formatCurrency(relatorio.valorPedagios))
        drawField("Valor Total", formatCurrency(relatorio.valorTotalDeslocamento))

        // Seção Observações
        if (relatorio.observacoes.isNotEmpty()) {
            drawSection("OBSERVAÇÕES")
            val lines = breakTextIntoLines(relatorio.observacoes, textPaint, contentWidth)
            for (line in lines) {
                checkNewPage(14)
                canvas.drawText(line, margin.toFloat(), y.toFloat(), textPaint)
                y += 14
            }
            y += 10
        }

        // Seção Assinaturas
        drawSection("ASSINATURAS")

        // Assinaturas dos Técnicos
        checkNewPage(150)
        canvas.drawText("Técnico 1: ${relatorio.nomeTecnico}", margin.toFloat(), y.toFloat(), labelPaint)
        y += 20
        if (!relatorio.assinaturaTecnico1.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(relatorio.assinaturaTecnico1, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                val destRect = RectF(margin.toFloat(), y.toFloat(), (margin + 200).toFloat(), (y + 80).toFloat())
                canvas.drawBitmap(bitmap, null, destRect, null)
                y += 90
            } catch (_: Exception) {
                canvas.drawText("Erro ao carregar assinatura", margin.toFloat(), y.toFloat(), smallTextPaint)
                y += 90
            }
        } else {
            canvas.drawText("__________________________", margin.toFloat(), y.toFloat(), textPaint)
            y += 90
        }
        checkNewPage(150)
        canvas.drawText("Técnico 2", margin.toFloat(), y.toFloat(), labelPaint)
        y += 20
        if (!relatorio.assinaturaTecnico2.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(relatorio.assinaturaTecnico2, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                val destRect = RectF(margin.toFloat(), y.toFloat(), (margin + 200).toFloat(), (y + 80).toFloat())
                canvas.drawBitmap(bitmap, null, destRect, null)
                y += 90
            } catch (_: Exception) {
                canvas.drawText("Erro ao carregar assinatura", margin.toFloat(), y.toFloat(), smallTextPaint)
                y += 90
            }
        } else {
            canvas.drawText("__________________________", margin.toFloat(), y.toFloat(), textPaint)
            y += 90
        }

        // Assinaturas dos Clientes
        checkNewPage(150)
        canvas.drawText("Cliente 1", margin.toFloat(), y.toFloat(), labelPaint)
        y += 20
        if (!relatorio.assinaturaCliente1.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(relatorio.assinaturaCliente1, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                val destRect = RectF(margin.toFloat(), y.toFloat(), (margin + 200).toFloat(), (y + 80).toFloat())
                canvas.drawBitmap(bitmap, null, destRect, null)
                y += 90
            } catch (_: Exception) {
                canvas.drawText("Erro ao carregar assinatura", margin.toFloat(), y.toFloat(), smallTextPaint)
                y += 90
            }
        } else {
            canvas.drawText("__________________________", margin.toFloat(), y.toFloat(), textPaint)
            y += 90
        }
        checkNewPage(150)
        canvas.drawText("Cliente 2", margin.toFloat(), y.toFloat(), labelPaint)
        y += 20
        if (!relatorio.assinaturaCliente2.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(relatorio.assinaturaCliente2, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                val destRect = RectF(margin.toFloat(), y.toFloat(), (margin + 200).toFloat(), (y + 80).toFloat())
                canvas.drawBitmap(bitmap, null, destRect, null)
                y += 90
            } catch (_: Exception) {
                canvas.drawText("Erro ao carregar assinatura", margin.toFloat(), y.toFloat(), smallTextPaint)
                y += 90
            }
        } else {
            canvas.drawText("__________________________", margin.toFloat(), y.toFloat(), textPaint)
            y += 90
        }

        document.finishPage(page)

        // Salvar PDF
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!
        if (!dir.exists()) dir.mkdirs()
        val timestamp = System.currentTimeMillis()
        val file = File(dir, "relatorio_completo_${relatorio.id}_$timestamp.pdf")
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
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        return if (lines.isEmpty()) listOf(text) else lines
    }

    @Suppress("DEPRECATION")
    private fun formatCurrency(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return format.format(value)
    }
}
