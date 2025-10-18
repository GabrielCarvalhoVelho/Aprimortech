package com.example.aprimortech

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Base64
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import com.example.aprimortech.model.RelatorioCompleto
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.*

object PdfExporter {

    fun exportRelatorioCompleto(
        context: Context,
        relatorio: RelatorioCompleto,
        companyLogoResId: Int? = R.drawable.logo_aprimortech,
        companyLine1: String? = "JP - SOLUÇÕES TÉCNICAS E COMÉRCIO INDUSTRIAL - CNPJ: 26.549.228/0001-29",
        companyLine2: String? = "Rua Plínio Pasqui, 186, Vila Dom Pedro II - São Paulo/SP - CEP: 02244-030",
        companyLogoMaxHeight: Int? = 48
    ): android.net.Uri {
        val document = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842

        val margin = 32
        val contentWidth = pageWidth - margin * 2
        val gutter = 12
        val footerReserve = 56

        val brand = Color.rgb(26, 74, 92)
        val ink = "#1A1A1A".toColorInt()
        val inkLight = "#666666".toColorInt()
        val rule = "#E5E7EB".toColorInt()

        // Fontes padrão do Android
        val fontRegular = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        val fontBold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = fontBold
            textSize = 11.5f
            color = brand
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = fontBold
            textSize = 9.5f
            color = ink
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = fontRegular
            textSize = 9.5f
            color = ink
        }
        val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = fontRegular
            textSize = 8.5f
            color = inkLight
        }
        val linePaint = Paint().apply {
            color = rule
            strokeWidth = 1f
        }

        // use a single non-deprecated Locale instance for pt-BR
        val localePtBr = Locale.forLanguageTag("pt-BR")

        val companyLine1Val = companyLine1 ?: "JP - SOLUÇÕES TÉCNICAS E COMÉRCIO INDUSTRIAL - CNPJ: 26.549.228/0001-29"
        val companyLine2Val = companyLine2 ?: "Rua Plínio Pasqui, 186, Vila Dom Pedro II - São Paulo/SP - CEP: 02244-030"

        var currentPage = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        var y = margin
        val baseLine = 13

        var partsHeaderDrawnOnPage = false

        fun breakTextIntoLines(text: String?, paint: Paint, maxWidth: Int): List<String> {
            val t = (text ?: "").trim()
            if (t.isEmpty()) return listOf("—")
            val words = t.split(" ")
            val lines = mutableListOf<String>()
            var current = ""
            for (w in words) {
                val test = if (current.isEmpty()) w else "$current $w"
                if (paint.measureText(test) > maxWidth) {
                    if (current.isNotEmpty()) lines.add(current)
                    current = w
                } else {
                    current = test
                }
            }
            if (current.isNotEmpty()) lines.add(current)
            return if (lines.isEmpty()) listOf(t) else lines
        }

        fun drawHeader() {
            val logoMaxH = companyLogoMaxHeight ?: 48
            val headerHeight = logoMaxH + 8 + baseLine * 2
            val headerTop = margin
            val headerBottom = headerTop + headerHeight

            var rightBlockLeft = margin
            companyLogoResId?.let { resId ->
                try {
                    val bmp = BitmapFactory.decodeResource(context.resources, resId)
                    if (bmp != null) {
                        val scale = minOf(
                            (logoMaxH.toFloat() / bmp.height.toFloat()),
                            (contentWidth * 0.35f) / bmp.width.toFloat()
                        )
                        val logoW = (bmp.width * scale).toInt()
                        val logoH = (bmp.height * scale).toInt()
                        val left = margin
                        val top = headerTop
                        val dest = Rect(left, top, left + logoW, top + logoH)
                        canvas.drawBitmap(bmp, null, dest, null)
                        rightBlockLeft = left + logoW + gutter
                    }
                } catch (_: Exception) {}
            }

            val rightBlockWidth = pageWidth - margin - rightBlockLeft
            // draw company line 1 and line 2 separately
            val infoLines1 = breakTextIntoLines(companyLine1Val, smallTextPaint, rightBlockWidth)
            var ty = headerTop + 4
            for (l in infoLines1) {
                canvas.drawText(l, rightBlockLeft.toFloat(), ty.toFloat(), smallTextPaint)
                ty += baseLine
            }
            val infoLines2 = breakTextIntoLines(companyLine2Val, smallTextPaint, rightBlockWidth)
            for (l in infoLines2) {
                canvas.drawText(l, rightBlockLeft.toFloat(), ty.toFloat(), smallTextPaint)
                ty += baseLine
            }

            // Ajuste: reduzir espaçamento extra entre o cabeçalho e o conteúdo
            // Antes: y = maxOf(headerBottom, ty + 4); y += 6
            // Agora: usa um pequeno padding adicional (2px) para manter separação, mas diminuir o espaço total
            y = maxOf(headerBottom, ty + 2)
            y += 2

            canvas.drawLine(margin.toFloat(), y.toFloat(), (pageWidth - margin).toFloat(), y.toFloat(), linePaint)
            y += 10
        }

        fun drawFooter() {
            val footerTop = pageHeight - margin - footerReserve
            canvas.drawLine(margin.toFloat(), footerTop.toFloat(), (pageWidth - margin).toFloat(), footerTop.toFloat(), linePaint)

            val leftColWidth = (contentWidth * 0.75f).toInt()
            // draw company line1 and line2 in footer
            val fLines1 = breakTextIntoLines(companyLine1Val, smallTextPaint, leftColWidth)
            val fLines2 = breakTextIntoLines(companyLine2Val, smallTextPaint, leftColWidth)
            var fy = footerTop + 14
            for (p in (fLines1 + fLines2).take(3)) {
                canvas.drawText(p, margin.toFloat(), fy.toFloat(), smallTextPaint)
                fy += 10
            }

            val pageStr = "Página $currentPage"
            val pw = smallTextPaint.measureText(pageStr)
            canvas.drawText(pageStr, pageWidth - margin - pw, (footerTop + 18).toFloat(), smallTextPaint)
        }

        fun startNewPage() {
            try { drawFooter() } catch (_: Exception) {}
            document.finishPage(page)
            currentPage++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            y = margin
            partsHeaderDrawnOnPage = false
            try { drawHeader() } catch (_: Exception) {}
        }

        fun checkNewPage(neededSpace: Int) {
            if (y + neededSpace > pageHeight - margin - footerReserve) startNewPage()
        }

        fun drawSectionTitle(title: String) {
            checkNewPage(baseLine * 3)
            val barH = baseLine + 6
            val bg = Paint().apply { color = Color.argb(22, 26, 74, 92) }
            canvas.drawRect(margin.toFloat(), (y - 2).toFloat(), (pageWidth - margin).toFloat(), (y - 2 + barH).toFloat(), bg)
            sectionPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(title, (margin + 8).toFloat(), (y + baseLine - 2).toFloat(), sectionPaint)
            y += barH + 4
        }

        fun drawEmptyPlaceholder() {
            val p = Paint(textPaint).apply { color = inkLight }
            canvas.drawText("—", (margin + 8).toFloat(), y.toFloat(), p)
            y += baseLine + 6
        }

        fun drawKeyValueTable(rows: List<Pair<String, String>>) {
            val labelW = (contentWidth * 0.32f).toInt()
            val valueW = contentWidth - labelW - 16

            rows.forEachIndexed { idx, (label, value) ->
                val lines = breakTextIntoLines(value.ifBlank { "—" }, textPaint, valueW)
                val rowH = maxOf(baseLine, lines.size * baseLine) + 10

                checkNewPage(rowH + 2)

                if (idx % 2 == 0) {
                    val zebra = Paint().apply { color = Color.argb(12, 0, 0, 0) }
                    canvas.drawRect(margin.toFloat(), y.toFloat(), (pageWidth - margin).toFloat(), (y + rowH).toFloat(), zebra)
                }

                canvas.drawText(label, (margin + 8).toFloat(), (y + baseLine).toFloat(), labelPaint)

                var vy = y + baseLine
                for (l in lines) {
                    canvas.drawText(l, (margin + 8 + labelW + 8).toFloat(), vy.toFloat(), textPaint)
                    vy += baseLine
                }

                canvas.drawLine(margin.toFloat(), (y + rowH).toFloat(), (pageWidth - margin).toFloat(), (y + rowH).toFloat(), linePaint)
                y += rowH
            }
            y += 6
        }

        fun drawIndexedList(items: List<String>) {
            val textW = contentWidth - 32
            items.forEachIndexed { idx, t ->
                val lines = breakTextIntoLines(t.ifBlank { "—" }, textPaint, textW)
                val blockH = lines.size * baseLine + 6
                checkNewPage(blockH + baseLine)

                canvas.drawText("${idx + 1}.", (margin + 8).toFloat(), (y + baseLine).toFloat(), labelPaint)
                var ly = y + baseLine
                for (l in lines) {
                    canvas.drawText(l, (margin + 32).toFloat(), ly.toFloat(), textPaint)
                    ly += baseLine
                }
                y = ly + 4
            }
            y += 4
        }

        fun drawPartsTableHeader(codeW: Int, qtyW: Int) {
            val headerH = baseLine + 8
            val bg = Paint().apply { color = Color.argb(26, 26, 74, 92) }
            canvas.drawRect(margin.toFloat(), y.toFloat(), (pageWidth - margin).toFloat(), (y + headerH).toFloat(), bg)

            val codeX = (margin + 8).toFloat()
            val descX = (margin + codeW + 16).toFloat()
            val qtyX = (pageWidth - margin - qtyW).toFloat()

            val headerPaint = Paint(labelPaint)
            canvas.drawText("Código", codeX, (y + baseLine).toFloat(), headerPaint)
            canvas.drawText("Descrição", descX, (y + baseLine).toFloat(), headerPaint)
            headerPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Qtde", qtyX, (y + baseLine).toFloat(), headerPaint)
            headerPaint.textAlign = Paint.Align.LEFT

            y += headerH
            canvas.drawLine(margin.toFloat(), y.toFloat(), (pageWidth - margin).toFloat(), y.toFloat(), linePaint)
            partsHeaderDrawnOnPage = true
        }

        fun drawPartsList(rows: List<Triple<String, String, String>>) {
            if (rows.isEmpty()) return

            val codeW = (contentWidth * 0.20f).toInt()
            val qtyW = (contentWidth * 0.14f).toInt()
            val descW = contentWidth - codeW - qtyW - 24

            if (!partsHeaderDrawnOnPage) drawPartsTableHeader(codeW, qtyW)

            rows.forEachIndexed { idx, (code, desc, qty) ->
                val lines = breakTextIntoLines(desc.ifBlank { "—" }, textPaint, descW)
                val rowH = maxOf(baseLine, lines.size * baseLine) + 10
                checkNewPage(rowH + 2)
                if (!partsHeaderDrawnOnPage) drawPartsTableHeader(codeW, qtyW)

                if (idx % 2 == 0) {
                    val zebra = Paint().apply { color = Color.argb(10, 0, 0, 0) }
                    canvas.drawRect(margin.toFloat(), y.toFloat(), (pageWidth - margin).toFloat(), (y + rowH).toFloat(), zebra)
                }

                canvas.drawText(code.ifBlank { "—" }, (margin + 8).toFloat(), (y + baseLine).toFloat(), labelPaint)

                var ly = y + baseLine
                for (l in lines) {
                    canvas.drawText(l, (margin + codeW + 16).toFloat(), ly.toFloat(), textPaint)
                    ly += baseLine
                }

                val qtyPaint = Paint(textPaint).apply { textAlign = Paint.Align.RIGHT }
                canvas.drawText(qty.ifBlank { "0" }, (pageWidth - margin - 6).toFloat(), (y + baseLine).toFloat(), qtyPaint)

                canvas.drawLine(margin.toFloat(), (y + rowH).toFloat(), (pageWidth - margin).toFloat(), (y + rowH).toFloat(), linePaint)
                y += rowH
            }
            y += 6
        }

        fun drawFullText(content: String) {
            // Some texts may arrive URL-encoded where spaces are represented as '+' or as %20.
            // Try to URL-decode safely; if that fails, fall back to replacing '+' with spaces.
            val cleaned = try {
                java.net.URLDecoder.decode(content, "UTF-8")
            } catch (_: Exception) {
                content.replace('+', ' ')
            }

            val lines = breakTextIntoLines(cleaned.ifBlank { "—" }, textPaint, contentWidth)
            val blockH = lines.size * baseLine + 6
            checkNewPage(blockH)
            var ly = y
            for (l in lines) {
                canvas.drawText(l, (margin + 8).toFloat(), (ly + baseLine).toFloat(), textPaint)
                ly += baseLine
            }
            y = ly + 8
        }

        val debugLogs = mutableListOf<String>()

        fun drawSignatures(items: List<Pair<String, String?>>) {
            val colW = contentWidth / 2
            val rowH = 64
            for (row in 0 until 2) {
                checkNewPage(rowH + 24)
                val top = y
                val bottom = y + rowH
                for (col in 0 until 2) {
                    val leftX = margin + col * colW
                    val rightX = leftX + colW
                    val lineY = bottom - 18
                    val idx = row * 2 + col
                    val label = items.getOrNull(idx)?.first ?: ""
                    val base64 = items.getOrNull(idx)?.second

                    // debug header
                    debugLogs.add("Assinatura[$idx] label='${label.take(80)}' present=${!base64.isNullOrBlank()}")

                    // label
                    canvas.drawText(label, (leftX + 8).toFloat(), (top + 16).toFloat(), labelPaint)

                    if (!base64.isNullOrBlank()) {
                        try {
                            // sanitize: remove optional data URI prefix and whitespace/newlines
                            val cleaned = base64.substringAfter("base64,").replace("\\s".toRegex(), "")
                            debugLogs.add("Assinatura[$idx] prefix='${base64.take(20)}' cleanedLen=${cleaned.length}")

                            // If the cleaned value looks like a URL (http/https/gs://), skip base64 decode
                            if (cleaned.startsWith("http://") || cleaned.startsWith("https://") || cleaned.startsWith("gs://")) {
                                debugLogs.add("Assinatura[$idx] appears to be URL: ${cleaned.take(120)}")
                                // If it's an HTTP/HTTPS URL, try to fetch the image bytes and decode
                                var fetchedBmp: Bitmap? = null
                                if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
                                    try {
                                        val url = java.net.URL(cleaned)
                                        val conn = url.openConnection() as java.net.HttpURLConnection
                                        conn.connectTimeout = 5000
                                        conn.readTimeout = 5000
                                        conn.requestMethod = "GET"
                                        conn.doInput = true
                                        conn.connect()
                                        conn.inputStream.use { ins ->
                                            val bytes = ins.readBytes()
                                            fetchedBmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        }
                                        conn.disconnect()
                                    } catch (ex: Exception) {
                                        debugLogs.add("Assinatura[$idx] fetch error: ${ex.message}")
                                        fetchedBmp = null
                                    }
                                }

                                if (fetchedBmp != null) {
                                    debugLogs.add("Assinatura[$idx] fetched image ok: ${fetchedBmp.width}x${fetchedBmp.height}")
                                    val bmp = fetchedBmp
                                    val sigMaxW = colW - 24
                                    val sigMaxH = rowH - 28
                                    val scale = minOf(sigMaxW.toFloat() / bmp.width.toFloat(), sigMaxH.toFloat() / bmp.height.toFloat(), 1f)
                                    val bmpW = (bmp.width * scale).toInt()
                                    val bmpH = (bmp.height * scale).toInt()
                                    val cx = leftX + (colW - bmpW) / 2
                                    val cy = top + 22
                                    val dest = Rect(cx, cy, cx + bmpW, cy + bmpH)
                                    canvas.drawBitmap(bmp, null, dest, null)
                                    canvas.drawLine((leftX + 8).toFloat(), lineY.toFloat(), (rightX - 8).toFloat(), lineY.toFloat(), linePaint)
                                } else {
                                    debugLogs.add("Assinatura[$idx] fetch produced no bitmap")
                                    // Could be gs:// or fetch failed; draw placeholder line
                                    canvas.drawLine((leftX + 8).toFloat(), lineY.toFloat(), (rightX - 8).toFloat(), lineY.toFloat(), linePaint)
                                }
                            } else {
                                // Try multiple Base64 flags to decode different encodings (DEFAULT, NO_WRAP, URL_SAFE, combinations)
                                var bmp: Bitmap? = null
                                val flagsList = listOf(
                                    Base64.DEFAULT,
                                    Base64.NO_WRAP,
                                    Base64.URL_SAFE,
                                    Base64.NO_PADDING or Base64.NO_WRAP
                                )
                                for (flag in flagsList) {
                                    try {
                                        val decoded = Base64.decode(cleaned, flag)
                                        val candidate = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                                        if (candidate != null) {
                                            bmp = candidate
                                            debugLogs.add("Assinatura[$idx] decoded ok with flag=$flag size=${decoded.size}")
                                            break
                                        }
                                    } catch (_: Exception) {
                                        // ignore and try next flag
                                    }
                                }

                                if (bmp != null) {
                                    debugLogs.add("Assinatura[$idx] bitmap ${bmp.width}x${bmp.height}")
                                    // scale to fit
                                    val sigMaxW = colW - 24
                                    val sigMaxH = rowH - 28
                                    val scale = minOf(sigMaxW.toFloat() / bmp.width.toFloat(), sigMaxH.toFloat() / bmp.height.toFloat(), 1f)
                                    val bmpW = (bmp.width * scale).toInt()
                                    val bmpH = (bmp.height * scale).toInt()
                                    val cx = leftX + (colW - bmpW) / 2
                                    val cy = top + 22
                                    val dest = Rect(cx, cy, cx + bmpW, cy + bmpH)
                                    canvas.drawBitmap(bmp, null, dest, null)
                                    // draw line below signature image
                                    canvas.drawLine((leftX + 8).toFloat(), lineY.toFloat(), (rightX - 8).toFloat(), lineY.toFloat(), linePaint)
                                } else {
                                    debugLogs.add("Assinatura[$idx] decode failed for all flags")
                                    // decode failed for all flags
                                    canvas.drawLine((leftX + 8).toFloat(), lineY.toFloat(), (rightX - 8).toFloat(), lineY.toFloat(), linePaint)
                                }
                            }
                        } catch (ex: Exception) {
                            debugLogs.add("Assinatura[$idx] exception: ${ex.message}")
                            // On error decoding, draw fallback line
                            canvas.drawLine((leftX + 8).toFloat(), lineY.toFloat(), (rightX - 8).toFloat(), lineY.toFloat(), linePaint)
                        }
                    } else {
                        debugLogs.add("Assinatura[$idx] is blank/null")
                        // no signature image: draw empty line
                        canvas.drawLine((leftX + 8).toFloat(), lineY.toFloat(), (rightX - 8).toFloat(), lineY.toFloat(), linePaint)
                    }

                    canvas.drawText("Assinatura", (leftX + 8).toFloat(), (lineY + 14).toFloat(), smallTextPaint)
                }
                y = bottom + 12
            }
        }

        // Render
        drawHeader()

        drawSectionTitle("DADOS DO CLIENTE")
        drawKeyValueTable(
            listOf(
                "Nome" to relatorio.clienteNome,
                "Endereço" to relatorio.clienteEndereco,
                "Cidade" to relatorio.clienteCidade,
                "Estado" to relatorio.clienteEstado,
                "Telefone" to relatorio.clienteTelefone,
                "Celular" to relatorio.clienteCelular
            )
        )

        drawSectionTitle("CONTATOS")
        if (relatorio.clienteContatos.isNotEmpty()) {
            relatorio.clienteContatos.forEachIndexed { idx, c ->
                drawKeyValueTable(
                    listOf(
                        "Nome" to c.nome,
                        "Setor" to c.setor,
                        "Celular" to c.celular
                    )
                )
                if (idx < relatorio.clienteContatos.lastIndex) y += 2
            }
        } else drawEmptyPlaceholder()

        drawSectionTitle("DADOS DO EQUIPAMENTO")
        drawKeyValueTable(
            listOf(
                "Fabricante" to relatorio.equipamentoFabricante,
                "Número de Série" to relatorio.equipamentoNumeroSerie,
                "Código de Configuração" to relatorio.equipamentoCodigoConfiguracao,
                "Modelo" to relatorio.equipamentoModelo,
                "Identificação" to relatorio.equipamentoIdentificacao,
                "Ano de Fabricação" to relatorio.equipamentoAnoFabricacao,
                "Código Tinta" to relatorio.equipamentoCodigoTinta,
                "Código Solvente" to relatorio.equipamentoCodigoSolvente,
                "Data Próxima Preventiva" to relatorio.equipamentoDataProximaPreventiva,
                "Hora Próxima Preventiva" to relatorio.equipamentoHoraProximaPreventiva
            )
        )

        drawSectionTitle("DEFEITOS IDENTIFICADOS")
        if (relatorio.defeitos.isNotEmpty()) drawIndexedList(relatorio.defeitos) else drawEmptyPlaceholder()

        drawSectionTitle("SERVIÇOS REALIZADOS")
        if (relatorio.servicos.isNotEmpty()) drawIndexedList(relatorio.servicos) else drawEmptyPlaceholder()

        drawSectionTitle("PEÇAS UTILIZADAS")
        val partsRows = relatorio.pecas.map { p -> Triple(p.codigo, p.descricao, p.quantidade.toString()) }
        if (partsRows.isNotEmpty()) drawPartsList(partsRows) else drawEmptyPlaceholder()

        // Compute totals to show clearly in the report
        val valorTotalHoras = relatorio.totalHorasTecnicas * relatorio.valorHoraTecnica
        val valorTotalDeslocamentoCalc = relatorio.quantidadeKm * relatorio.valorPorKm + relatorio.valorPedagios
        // Valor total do serviço = horas técnicas + deslocamento
        val valorTotalServico = valorTotalHoras + valorTotalDeslocamentoCalc

        drawSectionTitle("HORAS TÉCNICAS")
        drawKeyValueTable(
            listOf(
                "Horário de Entrada" to relatorio.horarioEntrada,
                "Horário de Saída" to relatorio.horarioSaida,
                // mostrar total de horas (numérico) e valor total calculado explicitamente
                "Total Horas" to String.format(localePtBr, "%.2f h", relatorio.totalHorasTecnicas),
                "Valor Hora Técnica" to formatCurrency(relatorio.valorHoraTecnica),
                "Valor Total (Horas Técnicas)" to formatCurrency(valorTotalHoras)
            )
        )

        drawSectionTitle("DESLOCAMENTO")
        drawKeyValueTable(
            listOf(
                "Quantidade de KM" to "${relatorio.quantidadeKm} km",
                "Valor por KM" to formatCurrency(relatorio.valorPorKm),
                "Valor dos Pedágios" to formatCurrency(relatorio.valorPedagios),
                // usar o valor calculado explicitamente e mostrar que é calculado
                "Valor Total (Deslocamento)" to formatCurrency(valorTotalDeslocamentoCalc)
            )
        )

        // Exibir o valor total do serviço de forma evidente
        drawSectionTitle("TOTAL DO SERVIÇO")
        drawKeyValueTable(
            listOf(
                "Valor Total do Serviço" to formatCurrency(valorTotalServico)
            )
        )

        drawSectionTitle("OBSERVAÇÕES")
        drawFullText(relatorio.observacoes)

        drawSectionTitle("ASSINATURAS")
        val sigItems = listOf(
            "Técnico 1" to relatorio.assinaturaTecnico1,
            "Técnico 2" to relatorio.assinaturaTecnico2,
            "Cliente 1" to relatorio.assinaturaCliente1,
            "Cliente 2" to relatorio.assinaturaCliente2
        )
        drawSignatures(sigItems)

        drawFooter()
        document.finishPage(page)

        // prepare output directory and file
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!
        if (!dir.exists()) dir.mkdirs()

        // build a filesystem-safe filename: relatorio_nomedocliente_dd_MM_yy
        val clientRaw = relatorio.clienteNome
        // normalize to remove accents, then replace non-alphanumeric with underscores
        val normalized = java.text.Normalizer.normalize(clientRaw.trim().lowercase(localePtBr), java.text.Normalizer.Form.NFD)
        val withoutDiacritics = normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        val safeClient = withoutDiacritics.replace("[^a-z0-9]+".toRegex(), "_").trim('_')
        val dateStr = java.text.SimpleDateFormat("dd_MM_yy", localePtBr).format(Date())
        val baseName = "relatorio_${if (safeClient.isNotBlank()) safeClient else "cliente"}_$dateStr"
        val file = File(dir, "$baseName.pdf")

        // write PDF
        FileOutputStream(file).use { document.writeTo(it) }

        // write debug file (same base name)
        try {
            val debugFile = File(dir, "$baseName.debug.txt")
            FileOutputStream(debugFile).use { fos ->
                fos.write(debugLogs.joinToString("\n").toByteArray())
            }
        } catch (_: Exception) {}

        document.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    @Suppress("DEPRECATION")
    private fun formatCurrency(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
        return format.format(value)
    }
}
