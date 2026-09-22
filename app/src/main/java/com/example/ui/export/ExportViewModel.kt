package com.example.ui.export

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.DiffHelper
import com.example.ai.GeminiService
import com.example.data.local.AppDatabase
import com.example.data.model.FormData
import com.example.data.model.Project
import com.example.data.preferences.SecurePreferences
import com.example.data.repository.ProjectRepository
import com.example.generator.ZipExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ExportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository
    private val geminiService = GeminiService()
    private val securePrefs = SecurePreferences(application)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProjectRepository(db.projectDao())
    }

    private var currentProjectId: Long = 0

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _formData = MutableStateFlow(FormData())
    val formData: StateFlow<FormData> = _formData.asStateFlow()

    private val _filesMap = MutableStateFlow<Map<String, File>>(emptyMap())
    val filesMap: StateFlow<Map<String, File>> = _filesMap.asStateFlow()

    private val _zipFile = MutableStateFlow<File?>(null)
    val zipFile: StateFlow<File?> = _zipFile.asStateFlow()

    // AI Refinement State
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _pendingDiffResult = MutableStateFlow<DiffHelper.DiffResult?>(null)
    val pendingDiffResult: StateFlow<DiffHelper.DiffResult?> = _pendingDiffResult.asStateFlow()

    private val _pendingModifiedContent = MutableStateFlow<String?>(null)
    val pendingModifiedContent: StateFlow<String?> = _pendingModifiedContent.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadProject(projectId: Long) {
        currentProjectId = projectId
        viewModelScope.launch {
            val p = repository.getProjectById(projectId)
            _project.value = p
            if (p != null) {
                _formData.value = FormData.fromJson(p.formDataJson)
                refreshFiles()
            }
        }
    }

    fun refreshFiles() {
        val context = getApplication<Application>()
        val projectDir = File(context.filesDir, "projects/$currentProjectId")
        if (projectDir.exists()) {
            val map = mutableMapOf<String, File>()
            listOf("index.html", "admin.html", "firestore.rules", "SETUP.md").forEach { name ->
                val f = File(projectDir, name)
                if (f.exists()) {
                    map[name] = f
                }
            }
            _filesMap.value = map

            // Locate zip
            val zip = projectDir.listFiles()?.firstOrNull { it.name.endsWith(".zip") }
            _zipFile.value = zip
        }
    }

    fun getFileContent(fileName: String): String {
        return _filesMap.value[fileName]?.readText() ?: ""
    }

    fun shareFile(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (file.name.endsWith(".zip")) "application/zip" else "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share ${file.name}"))
        } catch (e: Exception) {
            _errorMessage.value = "Failed to share: ${e.message}"
        }
    }

    fun shareZip(context: Context) {
        val zip = _zipFile.value
        if (zip != null && zip.exists()) {
            shareFile(context, zip)
        } else {
            _errorMessage.value = "ZIP bundle not found."
        }
    }

    fun requestAiEdit(instruction: String, useThinking: Boolean) {
        val apiKey = securePrefs.getGeminiApiKey()
        if (apiKey.isBlank()) {
            _errorMessage.value = "Gemini API key is required for AI mode. Please configure it in Settings."
            return
        }

        val indexFile = _filesMap.value["index.html"]
        if (indexFile == null || !indexFile.exists()) {
            _errorMessage.value = "index.html not found."
            return
        }

        viewModelScope.launch {
            _isAiLoading.value = true
            _statusMessage.value = if (useThinking) "Analyzing request with Gemini 3.1 Pro (Deep Thinking)..." else "Synthesizing code modifications with Gemini 3.5 Flash..."
            try {
                val currentContent = indexFile.readText()
                val result = geminiService.refineCode(
                    apiKey = apiKey,
                    originalFileContent = currentContent,
                    instruction = instruction,
                    useThinking = useThinking,
                    fileName = "index.html"
                )

                result.onSuccess { modifiedContent ->
                    val diff = DiffHelper.computeDiff(currentContent, modifiedContent)
                    _pendingModifiedContent.value = modifiedContent
                    _pendingDiffResult.value = diff
                    _statusMessage.value = null
                }.onFailure { ex ->
                    _errorMessage.value = "AI generation error: ${ex.message}"
                    _statusMessage.value = null
                }
            } catch (e: Exception) {
                _errorMessage.value = "Unexpected error: ${e.message}"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun applyAiModification() {
        val modified = _pendingModifiedContent.value ?: return
        val indexFile = _filesMap.value["index.html"] ?: return
        try {
            indexFile.writeText(modified)
            // Re-zip bundle
            val context = getApplication<Application>()
            val projectDir = File(context.filesDir, "projects/$currentProjectId")
            val cleanAppName = _formData.value.appName.filter { it.isLetterOrDigit() }.ifBlank { "miniapp" }
            ZipExporter.createZip(projectDir, "${cleanAppName}_bundle.zip")

            refreshFiles()
            _pendingDiffResult.value = null
            _pendingModifiedContent.value = null
            _statusMessage.value = "Changes applied to index.html successfully!"
        } catch (e: Exception) {
            _errorMessage.value = "Failed to write changes: ${e.message}"
        }
    }

    fun dismissDiff() {
        _pendingDiffResult.value = null
        _pendingModifiedContent.value = null
    }

    fun clearStatus() {
        _statusMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
