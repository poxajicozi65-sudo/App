package com.example.ui.builder

import android.app.Application
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.FormData
import com.example.data.model.Project
import com.example.data.repository.ProjectRepository
import com.example.generator.MiniAppGenerator
import com.example.generator.ZipExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStream

class BuilderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository
    private var currentProjectId: Long = 0

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProjectRepository(db.projectDao())
    }

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _formData = MutableStateFlow(FormData())
    val formData: StateFlow<FormData> = _formData.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationSuccess = MutableStateFlow<Long?>(null)
    val generationSuccess: StateFlow<Long?> = _generationSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadProject(projectId: Long) {
        currentProjectId = projectId
        if (projectId > 0) {
            viewModelScope.launch {
                val proj = repository.getProjectById(projectId)
                if (proj != null) {
                    _formData.value = FormData.fromJson(proj.formDataJson)
                }
            }
        } else {
            _formData.value = FormData()
            _currentStep.value = 0
        }
    }

    fun setStep(step: Int) {
        if (step in 0..5) {
            _currentStep.value = step
        }
    }

    fun nextStep() {
        if (_currentStep.value < 5) {
            _currentStep.value += 1
        }
    }

    fun prevStep() {
        if (_currentStep.value > 0) {
            _currentStep.value -= 1
        }
    }

    fun updateFormData(transform: (FormData) -> FormData) {
        _formData.value = transform(_formData.value)
    }

    fun handleLogoUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null && bytes.size < 2 * 1024 * 1024) { // Max 2MB for logo
                    val mimeType = context.contentResolver.getType(uri) ?: "image/png"
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    val dataUri = "data:$mimeType;base64,$base64"
                    updateFormData { it.copy(logoData = dataUri) }
                } else {
                    _errorMessage.value = "Image is too large. Please select an image under 2MB."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load image: ${e.message}"
            }
        }
    }

    fun parsePastedFirebaseConfig(pastedText: String): Boolean {
        return try {
            val json = JSONObject(pastedText.trim())
            val apiKey = json.optString("apiKey", "")
            val authDomain = json.optString("authDomain", "")
            val projectId = json.optString("projectId", "")
            val storageBucket = json.optString("storageBucket", "")
            val messagingSenderId = json.optString("messagingSenderId", "")
            val appId = json.optString("appId", "")

            updateFormData {
                it.copy(
                    apiKey = apiKey,
                    authDomain = authDomain,
                    projectId = projectId,
                    storageBucket = storageBucket,
                    messagingSenderId = messagingSenderId,
                    appId = appId,
                    fullConfigJson = json.toString(2)
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun generateProject() {
        viewModelScope.launch {
            _isGenerating.value = true
            _errorMessage.value = null
            try {
                val current = _formData.value
                val projToSave = Project(
                    id = currentProjectId,
                    name = current.appName.ifBlank { "Mini App" },
                    updatedAt = System.currentTimeMillis(),
                    formDataJson = current.toJson()
                )
                val savedId = repository.saveProject(projToSave)
                currentProjectId = savedId

                // Generate files into app's private filesDir
                val context = getApplication<Application>()
                val result = MiniAppGenerator.generate(context, savedId, current)

                // Create ZIP bundle
                val cleanAppName = current.appName.filter { it.isLetterOrDigit() }.ifBlank { "miniapp" }
                ZipExporter.createZip(result.projectDir, "${cleanAppName}_bundle.zip")

                _generationSuccess.value = savedId
            } catch (e: Exception) {
                _errorMessage.value = "Generation failed: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun clearGenerationSuccess() {
        _generationSuccess.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
