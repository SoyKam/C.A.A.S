package com.caas.app.domain.usecase

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

class ShareFileUseCase {
    fun invoke(context: Context, file: File, mimeType: String): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
