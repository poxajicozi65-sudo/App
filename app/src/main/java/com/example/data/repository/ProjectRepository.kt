package com.example.data.repository

import com.example.data.local.ProjectDao
import com.example.data.model.Project
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()

    suspend fun getProjectById(id: Long): Project? {
        return projectDao.getProjectById(id)
    }

    suspend fun saveProject(project: Project): Long {
        return if (project.id > 0) {
            projectDao.updateProject(project)
            project.id
        } else {
            projectDao.insertProject(project)
        }
    }

    suspend fun deleteProject(id: Long) {
        projectDao.deleteProjectById(id)
    }
}
