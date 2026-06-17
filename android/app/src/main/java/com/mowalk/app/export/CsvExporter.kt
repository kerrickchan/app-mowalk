package com.mowalk.app.export

import android.content.Context
import android.content.Intent
import com.mowalk.app.data.local.DailyStepEntity
import com.mowalk.app.data.repository.StepRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.OutputStream

class CsvExporter {

    fun export(
        context: Context,
        repository: StepRepository
    ) {
        // Export will be called from the UI layer with the ActivityResultLauncher
    }

    suspend fun formatCsv(repository: StepRepository): String {
        return withContext(Dispatchers.IO) {
            val sb = StringBuilder()
            sb.append("date,steps,distance,calories,isManuallyEdited\n")

            val today = DailyStepEntity.today()
            val thirtyDaysAgo = java.time.LocalDate.now().minusDays(30).toString()

            repository.getRange(thirtyDaysAgo, today)
                .first()
                .sortedBy { it.date }
                .forEach { entity ->
                    sb.append("${entity.date},${entity.steps},${entity.distance},${entity.calories},${if (entity.isManuallyEdited) 1 else 0}\n")
                }

            sb.toString()
        }
    }

    fun createExportIntent(filename: String = "mowalk_steps.csv"): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, filename)
        }
    }

    suspend fun writeToFile(context: Context, uri: android.net.Uri, csvContent: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(csvContent.toByteArray(Charsets.UTF_8))
                } != null
            }
        } catch (e: Exception) {
            false
        }
    }
}
