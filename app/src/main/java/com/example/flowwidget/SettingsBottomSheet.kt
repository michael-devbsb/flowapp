package com.example.flowwidget

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.io.OutputStreamWriter

class SettingsBottomSheet(private val onSaved: () -> Unit) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_settings, container, false)

        val etReminderTime = view.findViewById<EditText>(R.id.et_reminder_time)
        val btnExport = view.findViewById<Button>(R.id.btn_export)
        val btnSave = view.findViewById<Button>(R.id.btn_save_settings)

        val prefs = requireContext().getSharedPreferences("flow_prefs", Context.MODE_PRIVATE)
        val currentTime = prefs.getInt("reminder_minutes", 15)
        etReminderTime.setText(currentTime.toString())

        btnSave.setOnClickListener {
            val newTime = etReminderTime.text.toString().toIntOrNull() ?: 15
            prefs.edit().putInt("reminder_minutes", newTime).apply()
            
            // Re-agendar todos os alarmes com o novo tempo
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(requireContext())
                val blocks = withContext(Dispatchers.IO) {
                    db.routineDao().getAllBlocks()
                }
                blocks.forEach { block ->
                    NotificationScheduler.scheduleAlarm(requireContext(), block)
                }
                Toast.makeText(context, "Configurações salvas!", Toast.LENGTH_SHORT).show()
                onSaved()
                dismiss()
            }
        }

        btnExport.setOnClickListener {
            exportToCSV()
        }

        return view
    }

    private fun exportToCSV() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val blocks = withContext(Dispatchers.IO) {
                db.routineDao().getAllBlocks()
            }

            if (blocks.isEmpty()) {
                Toast.makeText(context, "Nenhuma rotina para exportar", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val csvContent = StringBuilder()
            csvContent.append("ID,Nome,Inicio,Fim,Tipo,Dias/Data,Cor,Tarefas\n")
            blocks.forEach { block ->
                val type = if (block.isFixed) "Fixa" else "Pontual"
                val whenStr = if (block.isFixed) block.selectedDays else block.date
                val sanitizedTasks = block.tasks.replace("\n", "; ")
                csvContent.append("${block.id},${block.name},${block.startTime},${block.endTime},$type,$whenStr,${block.colorHex},\"$sanitizedTasks\"\n")
            }

            saveFileToDownloads("FlowWidget_Routine_Export.csv", csvContent.toString())
        }
    }

    private fun saveFileToDownloads(fileName: String, content: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = requireContext().contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    requireContext().contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(content.toByteArray())
                    }
                    Toast.makeText(context, "Exportado para Downloads!", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Funcionalidade disponível para Android 10+", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao exportar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
