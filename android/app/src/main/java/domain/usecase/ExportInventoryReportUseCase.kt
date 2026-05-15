package com.caas.app.domain.usecase

import android.content.Context
import com.caas.app.core.result.Result
import com.caas.app.domain.model.InventorySummaryItem
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
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
            val file = File(context.cacheDir, "Reporte_Inventario_${safeBranchName}_${System.currentTimeMillis()}.pdf")
            
            val document = Document(PageSize.A4, 36f, 36f, 54f, 54f)
            val writer = PdfWriter.getInstance(document, FileOutputStream(file))
            
            // Footer con número de página y fecha
            val footerEvent = object : PdfPageEventHelper() {
                override fun onEndPage(writer: PdfWriter, document: Document) {
                    val cb = writer.directContent
                    val footerText = "Generado por CAAS • Página ${writer.pageNumber}"
                    val dateText = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    
                    val font = FontFactory.getFont(FontFactory.HELVETICA, 8f, BaseColor.GRAY)
                    
                    ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, Phrase(dateText, font),
                        document.left(), document.bottom() - 10, 0f)
                    
                    ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, Phrase(footerText, font),
                        document.right(), document.bottom() - 10, 0f)
                }
            }
            writer.pageEvent = footerEvent

            document.open()

            // COLORES CAAS
            val orangeCAAS = BaseColor(255, 128, 0)
            val darkGray = BaseColor(40, 40, 40)
            val lightGray = BaseColor(245, 245, 245)

            // ENCABEZADO NARANJA
            val headerTable = PdfPTable(1).apply { widthPercentage = 100f }
            val headerCell = PdfPCell(Phrase("REPORTE DE INVENTARIO", 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20f, BaseColor.WHITE))).apply {
                backgroundColor = orangeCAAS
                horizontalAlignment = Element.ALIGN_CENTER
                verticalAlignment = Element.ALIGN_MIDDLE
                paddingTop = 20f
                paddingBottom = 20f
                border = Rectangle.NO_BORDER
            }
            headerTable.addCell(headerCell)
            document.add(headerTable)

            // ESPACIO Y LOGO/NOMBRE
            document.add(Paragraph("CAAS — Gestión de Inventario", 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f, orangeCAAS)).apply {
                spacingBefore = 10f
            })

            // INFORMACIÓN DEL NEGOCIO
            val infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10f, darkGray)
            document.add(Paragraph("Negocio: $businessName", infoFont))
            document.add(Paragraph("Sucursal: $branchName", infoFont))
            document.add(Paragraph("Total de productos en este reporte: ${items.size}", infoFont))
            document.add(Paragraph("\n"))

            // RESUMEN DE MÉTRICAS (Cards visuales)
            val metricsTable = PdfPTable(3).apply { 
                widthPercentage = 100f
                spacingBefore = 10f
                spacingAfter = 20f
            }
            
            fun addMetricCell(table: PdfPTable, label: String, value: String, color: BaseColor) {
                val cell = PdfPCell().apply {
                    backgroundColor = lightGray
                    setPadding(12f)
                    border = Rectangle.BOX
                    borderColor = BaseColor.LIGHT_GRAY
                }
                cell.addElement(Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA, 8f, BaseColor.GRAY)))
                cell.addElement(Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16f, color)))
                table.addCell(cell)
            }

            val totalStock = items.sumOf { it.quantity }
            val criticalItems = items.count { it.isCritical }

            addMetricCell(metricsTable, "PRODUCTOS", items.size.toString(), darkGray)
            addMetricCell(metricsTable, "STOCK TOTAL", totalStock.toString(), BaseColor(34, 160, 107))
            addMetricCell(metricsTable, "BAJO STOCK", criticalItems.toString(), BaseColor(222, 53, 11))
            
            document.add(metricsTable)

            // TABLA DE PRODUCTOS
            val table = PdfPTable(floatArrayOf(3.5f, 1.5f, 1f, 1.2f)).apply {
                widthPercentage = 100f
                headerRows = 1
            }

            // Encabezado de tabla
            listOf("Producto / Descripción", "SKU", "Cant.", "Estado").forEach { text ->
                table.addCell(PdfPCell(Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, BaseColor.WHITE))).apply {
                    backgroundColor = darkGray
                    horizontalAlignment = Element.ALIGN_CENTER
                    setPadding(8f)
                })
            }

            // Datos
            items.forEachIndexed { index, item ->
                val bg = if (index % 2 == 0) BaseColor.WHITE else lightGray
                val font = FontFactory.getFont(FontFactory.HELVETICA, 9f, darkGray)
                
                table.addCell(PdfPCell(Phrase(item.productName, font)).apply {
                    backgroundColor = bg
                    setPadding(6f)
                })
                table.addCell(PdfPCell(Phrase(item.sku, font)).apply {
                    backgroundColor = bg
                    setPadding(6f)
                    horizontalAlignment = Element.ALIGN_CENTER
                })
                table.addCell(PdfPCell(Phrase(item.quantity.toString(), font)).apply {
                    backgroundColor = bg
                    setPadding(6f)
                    horizontalAlignment = Element.ALIGN_CENTER
                })
                
                val statusText = if (item.isCritical) "CRÍTICO" else "NORMAL"
                val statusColor = if (item.isCritical) BaseColor(222, 53, 11) else BaseColor(34, 160, 107)
                val statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, statusColor)
                
                table.addCell(PdfPCell(Phrase(statusText, statusFont)).apply {
                    backgroundColor = bg
                    setPadding(6f)
                    horizontalAlignment = Element.ALIGN_CENTER
                })
            }

            document.add(table)
            document.close()

            Result.Success(file)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al generar el PDF", e)
        }
    }
}
