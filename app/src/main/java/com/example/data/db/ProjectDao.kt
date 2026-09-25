package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    suspend fun getProjectById(projectId: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProject(projectId: String)

    // Project Files
    @Query("SELECT * FROM project_files WHERE projectId = :projectId ORDER BY isMain DESC, name ASC")
    fun getFilesForProject(projectId: String): Flow<List<ProjectFileEntity>>

    @Query("SELECT * FROM project_files WHERE projectId = :projectId")
    suspend fun getFilesForProjectSync(projectId: String): List<ProjectFileEntity>

    @Query("SELECT * FROM project_files WHERE projectId = :projectId AND name = :fileName LIMIT 1")
    suspend fun getFileByName(projectId: String, fileName: String): ProjectFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ProjectFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<ProjectFileEntity>)

    @Query("DELETE FROM project_files WHERE projectId = :projectId AND name = :fileName")
    suspend fun deleteFile(projectId: String, fileName: String)

    @Query("DELETE FROM project_files WHERE projectId = :projectId")
    suspend fun deleteFilesForProject(projectId: String)

    // Snapshots
    @Query("SELECT * FROM project_snapshots WHERE projectId = :projectId ORDER BY versionNumber DESC")
    fun getSnapshotsForProject(projectId: String): Flow<List<ProjectSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: ProjectSnapshotEntity)

    @Query("SELECT COUNT(*) FROM project_snapshots WHERE projectId = :projectId")
    suspend fun getSnapshotCount(projectId: String): Int

    // Learning Progress
    @Query("SELECT * FROM course_progress WHERE category = :category LIMIT 1")
    suspend fun getCourseProgress(category: String): CourseProgressEntity?

    @Query("SELECT * FROM course_progress")
    fun getAllCourseProgress(): Flow<List<CourseProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCourseProgress(progress: CourseProgressEntity)
}
