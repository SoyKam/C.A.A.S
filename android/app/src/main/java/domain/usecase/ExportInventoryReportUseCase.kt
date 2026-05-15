package com.caas.app.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import com.caas.app.core.result.Result
import com.caas.app.domain.model.InventorySummaryItem
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import java.io.ByteArrayOutputStream
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
        items: List<InventorySummaryItem>,
        charts: List<Bitmap>? = null
    ): Result<File> {
        return try {
            val safeBranchName = branchName.replace("[^a-zA-Z0-9_]".toRegex(), "_")
            val file = File(context.cacheDir, "Reporte_Maestro_${safeBranchName}_${System.currentTimeMillis()}.pdf")
            
            val document = Document(PageSize.A4, 36f, 36f, 54f, 54f)
            val writer = PdfWriter.getInstance(document, FileOutputStream(file))
            
            // Evento para Footer
            writer.pageEvent = object : PdfPageEventHelper() {
                override fun onEndPage(writer: PdfWriter, document: Document) {
                    val cb = writer.directContent
                    val footerText = "CAAS — Gestión Inteligente de Inventario • Página ${writer.pageNumber}"
                    val font = FontFactory.getFont(FontFactory.HELVETICA, 8f, BaseColor.GRAY)
                    
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, Phrase(footerText, font),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.bottom() - 20, 0f)
                }
            }

            document.open()

            // COLORES Y FUENTES
            val orangeCAAS = BaseColor(255, 128, 0)
            val darkGray = BaseColor(40, 40, 40)
            val lightGray = BaseColor(245, 245, 245)
            val white = BaseColor.WHITE
            
            val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22f, white)
            val subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f, orangeCAAS)
            val bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10f, darkGray)
            val headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, white)

            // ENCABEZADO PRINCIPAL
            val headerTable = PdfPTable(1).apply { widthPercentage = 100f }
            headerTable.addCell(PdfPCell(Phrase("REPORTE MAESTRO DE NEGOCIO", titleFont)).apply {
                backgroundColor = orangeCAAS
                horizontalAlignment = Element.ALIGN_CENTER
                paddingTop = 25f
                paddingBottom = 25f
                border = Rectangle.NO_BORDER
            })
            document.add(headerTable)

            // INFO GENERAL
            document.add(Paragraph("\nResumen Ejecutivo", subTitleFont))
            document.add(Paragraph("Negocio: $businessName", bodyFont))
            document.add(Paragraph("Sucursal: $branchName", bodyFont))
            document.add(Paragraph("Generado el: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}", bodyFont))
            document.add(Paragraph("\n"))

            // MÉTRICAS CLAVE
            val metricsTable = PdfPTable(3).apply { widthPercentage = 100f }
            
            fun addMetric(label: String, value: String, valColor: BaseColor) {
                val cell = PdfPCell().apply {
                    backgroundColor = lightGray
                    setPadding(15f)
                    border = Rectangle.BOX
                    borderColor = BaseColor.LIGHT_GRAY
                }
                cell.addElement(Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA, 8f, BaseColor.GRAY)))
                cell.addElement(Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, valColor)))
                metricsTable.addCell(cell)
            }

            val totalStock = items.sumOf { it.quantity }
            val criticalCount = items.count { it.isCritical }

            addMetric("PRODUCTOS TOTALES", items.size.toString(), darkGray)
            addMetric("UNIDADES EN STOCK", totalStock.toString(), BaseColor(34, 160, 107))
            addMetric("ALERTAS CRÍTICAS", criticalCount.toString(), BaseColor(222, 53, 11))
            
            document.add(metricsTable)

            // GRÁFICAS (si se proporcionan)
            charts?.let {
                document.add(Paragraph("\nAnálisis Visual", subTitleFont))
                val chartTable = PdfPTable(it.size).apply { 
                    widthPercentage = 100f
                    spacingBefore = 10f
                }
                
                for (bitmap in it) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val img = Image.getInstance(stream.toByteArray())
                    img.scaleToFit(250f, 250f)
                    
                    val cell = PdfPCell(img).apply {
                        border = Rectangle.NO_BORDER
                        horizontalAlignment = Element.ALIGN_CENTER
                        setPadding(10f)
                    }
                    chartTable.addCell(cell)
                }
                document.add(chartTable)
            }

            // TABLA DETALLADA (En nueva página si es necesario)
            document.add(Paragraph("\nDetalle de Inventario", subTitleFont))
            val table = PdfPTable(floatArrayOf(3.5f, 1.5f, 1f, 1.2f)).apply {
                widthPercentage = 100f
                headerRows = 1
                spacingBefore = 10f
            }

            listOf("Descripción del Producto", "SKU", "Stock", "Estado").forEach { text ->
                table.addCell(PdfPCell(Phrase(text, headerFont)).apply {
                    backgroundColor = darkGray
                    horizontalAlignment = Element.ALIGN_CENTER
                    setPadding(10f)
                })
            }

            items.forEachIndexed { index, item ->
                val bg = if (index % 2 == 0) white else lightGray
                val rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9f, darkGray)
                
                table.addCell(PdfPCell(Phrase(item.productName, rowFont)).apply { backgroundColor = bg; setPadding(8f) })
                table.addCell(PdfPCell(Phrase(item.sku, rowFont)).apply { backgroundColor = bg; setPadding(8f); horizontalAlignment = Element.ALIGN_CENTER })
                table.addCell(PdfPCell(Phrase(item.quantity.toString(), rowFont)).apply { backgroundColor = bg; setPadding(8f); horizontalAlignment = Element.ALIGN_CENTER })
                
                val statusText = if (item.isCritical) "BAJO STOCK" else "ÓPTIMO"
                val statusColor = if (item.isCritical) BaseColor(222, 53, 11) else BaseColor(34, 160, 107)
                val statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, statusColor)
                
                table.addCell(PdfPCell(Phrase(statusText, statusFont)).apply {
                    backgroundColor = bg
                    setPadding(8f)
                    horizontalAlignment = Element.ALIGN_CENTER
                })
            }

            document.add(table)
            document.close()
            
            Result.Success(file)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al generar el reporte PDF", e)
        }
    }
}
