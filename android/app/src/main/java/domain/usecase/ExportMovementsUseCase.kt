package com.caas.app.domain.usecase

import android.content.Context
import com.caas.app.core.result.Result
import com.caas.app.data.model.MovementType
import com.caas.app.data.model.StockMovement
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportMovementsUseCase {

    suspend operator fun invoke(
        context: Context,
        businessName: String,
        branchName: String,
        movements: List<StockMovement>,
        startDate: Long,
        endDate: Long
    ): Result<File> {
        return try {
            val safeBranchName = branchName.replace("[^a-zA-Z0-9_]".toRegex(), "_")
            val file = File(context.cacheDir, "movimientos_${safeBranchName}_${System.currentTimeMillis()}.xlsx")
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val workbook = XSSFWorkbook()

            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.GREY_50_PERCENT.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                setFont(workbook.createFont().apply {
                    bold = true
                    color = IndexedColors.WHITE.index
                })
            }

            // Hoja 1 — Movimientos
            val sheet1 = workbook.createSheet("Movimientos")
            val headers1 = listOf("Fecha", "Producto", "Tipo", "Cantidad", "Motivo", "Registrado por")
            val headerRow1 = sheet1.createRow(0)
            headers1.forEachIndexed { i, h ->
                headerRow1.createCell(i).apply {
                    setCellValue(h)
                    cellStyle = headerStyle
                }
            }
            sheet1.setColumnWidth(0, 22 * 256)
            sheet1.setColumnWidth(1, 35 * 256)
            sheet1.setColumnWidth(2, 15 * 256)
            sheet1.setColumnWidth(3, 12 * 256)
            sheet1.setColumnWidth(4, 30 * 256)
            sheet1.setColumnWidth(5, 25 * 256)

            movements.forEachIndexed { index, mov ->
                sheet1.createRow(index + 1).apply {
                    createCell(0).setCellValue(dateFormat.format(Date(mov.createdAt)))
                    createCell(1).setCellValue(mov.productName)
                    createCell(2).setCellValue(mov.type.toSpanish())
                    createCell(3).setCellValue(mov.quantity.toDouble())
                    createCell(4).setCellValue(mov.reason)
                    createCell(5).setCellValue(mov.createdBy)
                }
            }

            // Hoja 2 — Resumen
            val sheet2 = workbook.createSheet("Resumen")
            val entries = movements.count { it.type == MovementType.ENTRY }
            val exits = movements.count { it.type != MovementType.ENTRY }

            listOf(
                "Negocio" to businessName,
                "Sucursal" to branchName,
                "Período desde" to dayFormat.format(Date(startDate)),
                "Período hasta" to dayFormat.format(Date(endDate)),
                "Total movimientos" to movements.size.toString(),
                "Total entradas" to entries.toString(),
                "Total salidas" to exits.toString()
            ).forEachIndexed { i, (label, value) ->
                sheet2.createRow(i).apply {
                    createCell(0).apply { setCellValue(label); cellStyle = headerStyle }
                    createCell(1).setCellValue(value)
                }
            }
            sheet2.setColumnWidth(0, 25 * 256)
            sheet2.setColumnWidth(1, 30 * 256)

            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()
            Result.Success(file)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al generar el Excel", e)
        }
    }

    private fun MovementType.toSpanish() = when (this) {
        MovementType.ENTRY -> "Entrada"
        MovementType.SALE -> "Venta"
        MovementType.DAMAGE -> "Daño"
        MovementType.TRANSFER -> "Traslado"
    }
}
