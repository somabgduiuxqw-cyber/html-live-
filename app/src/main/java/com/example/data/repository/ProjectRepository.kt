package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.db.ProjectDao
import com.example.data.db.ProjectEntity
import com.example.data.db.ProjectFileEntity
import com.example.data.db.ProjectSnapshotEntity
import com.example.model.Project
import com.example.model.ProjectFile
import com.example.model.ProjectSnapshot
import com.example.model.ProjectType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ProjectRepository(private val context: Context) {
    private val dao: ProjectDao = AppDatabase.getDatabase(context).projectDao()

    val allProjects: Flow<List<Project>> = dao.getAllProjects().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun initDefaultProjectsIfNeeded() = withContext(Dispatchers.IO) {
        val count = dao.getAllProjects().first().size
        if (count == 0) {
            // Seed starter templates
            for (template in TemplateRepository.templates) {
                val projId = UUID.randomUUID().toString()
                val project = Project(
                    id = projId,
                    name = template.title,
                    description = template.description,
                    type = template.type,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                dao.insertProject(ProjectEntity.fromDomain(project))

                val files = listOf(
                    ProjectFileEntity(UUID.randomUUID().toString(), projId, "index.html", "index.html", template.htmlContent, true),
                    ProjectFileEntity(UUID.randomUUID().toString(), projId, "style.css", "style.css", template.cssContent, false),
                    ProjectFileEntity(UUID.randomUUID().toString(), projId, "script.js", "script.js", template.jsContent, false)
                )
                dao.insertFiles(files)
            }
        }
    }

    fun getFilesForProject(projectId: String): Flow<List<ProjectFile>> {
        return dao.getFilesForProject(projectId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getProjectById(projectId: String): Project? = withContext(Dispatchers.IO) {
        dao.getProjectById(projectId)?.toDomain()
    }

    suspend fun createProject(
        name: String,
        description: String = "",
        type: ProjectType = ProjectType.WEBSITE,
        template: TemplateProject? = null
    ): Project = withContext(Dispatchers.IO) {
        val projId = UUID.randomUUID().toString()
        val project = Project(
            id = projId,
            name = name,
            description = description,
            type = type
        )
        dao.insertProject(ProjectEntity.fromDomain(project))

        val html = template?.htmlContent ?: """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>$name</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <h1>$name</h1>
  <p>Created with HTML Live</p>
  <script src="script.js"></script>
</body>
</html>"""
        val css = template?.cssContent ?: "body { font-family: sans-serif; padding: 20px; background: #0f172a; color: #fff; }"
        val js = template?.jsContent ?: "console.log('$name running');"

        val files = listOf(
            ProjectFileEntity(UUID.randomUUID().toString(), projId, "index.html", "index.html", html, true),
            ProjectFileEntity(UUID.randomUUID().toString(), projId, "style.css", "style.css", css, false),
            ProjectFileEntity(UUID.randomUUID().toString(), projId, "script.js", "script.js", js, false)
        )
        dao.insertFiles(files)
        project
    }

    suspend fun updateProject(project: Project) = withContext(Dispatchers.IO) {
        dao.updateProject(ProjectEntity.fromDomain(project.copy(updatedAt = System.currentTimeMillis())))
    }

    suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        dao.deleteFilesForProject(projectId)
        dao.deleteProject(projectId)
    }

    suspend fun saveFile(projectId: String, fileName: String, content: String) = withContext(Dispatchers.IO) {
        val existing = dao.getFileByName(projectId, fileName)
        if (existing != null) {
            dao.insertFile(existing.copy(content = content))
        } else {
            val isMain = fileName.equals("index.html", ignoreCase = true)
            dao.insertFile(ProjectFileEntity(UUID.randomUUID().toString(), projectId, fileName, fileName, content, isMain))
        }
        val proj = dao.getProjectById(projectId)
        if (proj != null) {
            dao.updateProject(proj.copy(updatedAt = System.currentTimeMillis(), lastOpenedFile = fileName))
        }
    }

    suspend fun deleteFile(projectId: String, fileName: String) = withContext(Dispatchers.IO) {
        dao.deleteFile(projectId, fileName)
    }

    suspend fun getFilesSync(projectId: String): List<ProjectFile> = withContext(Dispatchers.IO) {
        dao.getFilesForProjectSync(projectId).map { it.toDomain() }
    }

    // Version Snapshots
    suspend fun createSnapshot(projectId: String, label: String = "Auto Backup") = withContext(Dispatchers.IO) {
        val files = dao.getFilesForProjectSync(projectId)
        val count = dao.getSnapshotCount(projectId)
        val filesJson = StringBuilder("[")
        files.forEachIndexed { i, f ->
            val escapedContent = f.content.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
            filesJson.append("""{"name":"${f.name}","content":"$escapedContent"}""")
            if (i < files.size - 1) filesJson.append(",")
        }
        filesJson.append("]")

        val snapshot = ProjectSnapshotEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            versionNumber = count + 1,
            label = label,
            createdAt = System.currentTimeMillis(),
            filesJson = filesJson.toString()
        )
        dao.insertSnapshot(snapshot)
    }

    fun getSnapshots(projectId: String): Flow<List<ProjectSnapshot>> {
        return dao.getSnapshotsForProject(projectId).map { list -> list.map { it.toDomain() } }
    }

    // Export Project to ZIP File
    suspend fun exportProjectToZip(projectId: String): File? = withContext(Dispatchers.IO) {
        val project = dao.getProjectById(projectId) ?: return@withContext null
        val files = dao.getFilesForProjectSync(projectId)
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeName = project.name.replace("[^a-zA-Z0-9_.-]".toRegex(), "_")
        val zipFile = File(exportDir, "$safeName.zip")

        FileOutputStream(zipFile).use { fos ->
            ZipOutputStream(fos).use { zos ->
                for (file in files) {
                    val entry = ZipEntry(file.path)
                    zos.putNextEntry(entry)
                    zos.write(file.content.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }
        }
        zipFile
    }

    // Import Project from ZIP InputStream
    suspend fun importProjectFromZip(name: String, zipBytes: ByteArray): Project = withContext(Dispatchers.IO) {
        val projId = UUID.randomUUID().toString()
        val project = Project(
            id = projId,
            name = name,
            description = "Imported from ZIP archive",
            type = ProjectType.WEBSITE
        )
        dao.insertProject(ProjectEntity.fromDomain(project))

        val entities = mutableListOf<ProjectFileEntity>()
        ZipInputStream(zipBytes.inputStream()).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val fileName = File(entry.name).name
                    val content = zis.bufferedReader(Charsets.UTF_8).readText()
                    val isMain = fileName.equals("index.html", ignoreCase = true)
                    entities.add(ProjectFileEntity(UUID.randomUUID().toString(), projId, fileName, entry.name, content, isMain))
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        if (entities.none { it.name.equals("index.html", ignoreCase = true) }) {
            entities.add(ProjectFileEntity(UUID.randomUUID().toString(), projId, "index.html", "index.html", "<!DOCTYPE html><html><body><h1>$name</h1></body></html>", true))
        }

        dao.insertFiles(entities)
        project
    }
}
