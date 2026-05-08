package com.caas.app.domain.usecase

import android.content.Context
import com.caas.app.core.result.Result
import com.caas.app.domain.model.InventorySummaryItem
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportInventoryReportUseCase {

    suspend operator fun invoke(
        context: Context,
        businessName: String,
        branchName: String,
        items: List<InventorySummaryItem>
    ): Result<File> {
        return try {
            val safeBranchName = branchName.replace("[^a-zA-Z0-9_]".toRegex(), "_")
            val file = File(context.cacheDir, "inventario_${safeBranchName}_${System.currentTimeMillis()}.pdf")
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            val document = Document(PageSize.A4)
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()

            val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16f, BaseColor.BLACK)
            val subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11f, BaseColor(90, 90, 90))
            val headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, BaseColor.WHITE)
            val bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10f, BaseColor.BLACK)
            val boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, BaseColor.BLACK)

            document.add(Paragraph("CAAS — Reporte de Inventario", titleFont).apply { spacingAfter = 4f })
            document.add(Paragraph("Negocio: $businessName", subtitleFont))
            document.add(Paragraph("Sucursal: $branchName", subtitleFont))
            document.add(Paragraph("Fecha de generación: ${dateFormat.format(Date())}", subtitleFont).apply { spacingAfter = 16f })

            val table = PdfPTable(5).apply {
                widthPercentage = 100f
                setWidths(floatArrayOf(3f, 1.5f, 1f, 1f, 1.2f))
            }

            val headerBg = BaseColor(40, 40, 40)
            listOf("Producto", "SKU", "Stock", "Mínimo", "Estado").forEach { label ->
                table.addCell(PdfPCell(Phrase(label, headerFont)).apply {
                    backgroundColor = headerBg
                    horizontalAlignment = Element.ALIGN_CENTER
                    paddingTop = 6f
                    paddingBottom = 6f
                    paddingLeft = 6f
                    paddingRight = 6f
                })
            }

            val criticalRowBg = BaseColor(255, 235, 238)

            items.forEach { item ->
                val rowBg = if (item.isCritical) criticalRowBg else BaseColor.WHITE
                val estadoColor = if (item.isCritical) BaseColor(198, 40, 40) else BaseColor(46, 125, 50)
                val estadoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, estadoColor)

                fun cell(text: String, align: Int = Element.ALIGN_LEFT, font: Font = bodyFont) =
                    PdfPCell(Phrase(text, font)).apply {
                        backgroundColor = rowBg
                        horizontalAlignment = align
                        paddingTop = 5f
                        paddingBottom = 5f
                        paddingLeft = 6f
                        paddingRight = 6f
                    }

                table.addCell(cell(item.productName))
                table.addCell(cell(item.sku, Element.ALIGN_CENTER))
                table.addCell(cell(item.quantity.toString(), Element.ALIGN_CENTER))
                table.addCell(cell(item.minStock.toString(), Element.ALIGN_CENTER))
                table.addCell(cell(if (item.isCritical) "Crítico" else "Normal", Element.ALIGN_CENTER, estadoFont))
            }

            document.add(table)

            val critical = items.count { it.isCritical }
            document.add(Paragraph("\nTotal productos: ${items.size}   |   En stock crítico: $critical", boldFont).apply {
                spacingBefore = 12f
            })

            document.close()
            Result.Success(file)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al generar el PDF", e)
        }
    }
}
