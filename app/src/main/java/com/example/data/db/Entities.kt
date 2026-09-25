package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Project
import com.example.model.ProjectFile
import com.example.model.ProjectSnapshot
import com.example.model.ProjectType

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val type: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastOpenedFile: String,
    val isFavorite: Boolean
) {
    fun toDomain(): Project {
        return Project(
            id = id,
            name = name,
            description = description,
            type = runCatching { ProjectType.valueOf(type) }.getOrDefault(ProjectType.WEBSITE),
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastOpenedFile = lastOpenedFile,
            isFavorite = isFavorite
        )
    }

    companion object {
        fun fromDomain(project: Project): ProjectEntity {
            return ProjectEntity(
                id = project.id,
                name = project.name,
                description = project.description,
                type = project.type.name,
                createdAt = project.createdAt,
                updatedAt = project.updatedAt,
                lastOpenedFile = project.lastOpenedFile,
                isFavorite = project.isFavorite
            )
        }
    }
}

@Entity(tableName = "project_files")
data class ProjectFileEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val name: String,
    val path: String,
    val content: String,
    val isMain: Boolean
) {
    fun toDomain(): ProjectFile {
        return ProjectFile(
            id = id,
            projectId = projectId,
            name = name,
            path = path,
            content = content,
            isMain = isMain
        )
    }

    companion object {
        fun fromDomain(file: ProjectFile): ProjectFileEntity {
            return ProjectFileEntity(
                id = file.id,
                projectId = file.projectId,
                name = file.name,
                path = file.path,
                content = file.content,
                isMain = file.isMain
            )
        }
    }
}

@Entity(tableName = "project_snapshots")
data class ProjectSnapshotEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val versionNumber: Int,
    val label: String,
    val createdAt: Long,
    val filesJson: String
) {
    fun toDomain(): ProjectSnapshot {
        return ProjectSnapshot(
            id = id,
            projectId = projectId,
            versionNumber = versionNumber,
            label = label,
            createdAt = createdAt,
            filesJson = filesJson
        )
    }

    companion object {
        fun fromDomain(snapshot: ProjectSnapshot): ProjectSnapshotEntity {
            return ProjectSnapshotEntity(
                id = snapshot.id,
                projectId = snapshot.projectId,
                versionNumber = snapshot.versionNumber,
                label = snapshot.label,
                createdAt = snapshot.createdAt,
                filesJson = snapshot.filesJson
            )
        }
    }
}

@Entity(tableName = "course_progress")
data class CourseProgressEntity(
    @PrimaryKey val category: String,
    val completedLessonIds: String // Comma separated IDs
)
