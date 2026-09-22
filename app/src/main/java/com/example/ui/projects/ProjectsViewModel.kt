package com.example.ui.projects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Project
import com.example.data.preferences.SecurePreferences
import com.example.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository
    private val securePrefs: SecurePreferences = SecurePreferences(application)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProjectRepository(db.projectDao())
    }

    val projects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _hasSeenDisclaimer = MutableStateFlow(securePrefs.hasSeenDisclaimer())
    val hasSeenDisclaimer: StateFlow<Boolean> = _hasSeenDisclaimer.asStateFlow()

    private val _geminiApiKey = MutableStateFlow(securePrefs.getGeminiApiKey())
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    fun markDisclaimerSeen() {
        securePrefs.setDisclaimerSeen(true)
        _hasSeenDisclaimer.value = true
    }

    fun saveGeminiApiKey(key: String) {
        securePrefs.setGeminiApiKey(key)
        _geminiApiKey.value = key
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    fun duplicateProject(project: Project) {
        viewModelScope.launch {
            val duplicate = Project(
                id = 0,
                name = "${project.name} (Copy)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                formDataJson = project.formDataJson
            )
            repository.saveProject(duplicate)
        }
    }
}
