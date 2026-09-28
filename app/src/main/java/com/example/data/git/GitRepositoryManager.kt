package com.example.data.git

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.api.MergeResult
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.util.io.DisabledOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class GitRepositoryManager(private val context: Context) {

    private val gitRootDir: File by lazy {
        File(context.filesDir, "git_repos").apply { if (!exists()) mkdirs() }
    }

    fun getRepoDir(projectId: String): File {
        return File(gitRootDir, projectId).apply { if (!exists()) mkdirs() }
    }

    /**
     * Normalizes a repository URL by trimming, validating, and ensuring proper .git suffix for HTTP(S) git URLs.
     */
    fun normalizeRepoUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return ""

        var url = trimmed
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            // Check if user entered "github.com/user/repo" or "user/repo"
            url = if (url.startsWith("github.com/", ignoreCase = true)) {
                "https://$url"
            } else if (url.contains("/") && !url.contains(" ")) {
                "https://github.com/$url"
            } else {
                "https://$url"
            }
        }

        // Clean trailing slash
        while (url.endsWith("/")) {
            url = url.dropLast(1)
        }

        // Add .git if it's standard GitHub/GitLab repository
        if (!url.endsWith(".git", ignoreCase = true) && (url.contains("github.com") || url.contains("gitlab.com"))) {
            url = "$url.git"
        }

        return url
    }

    fun extractOwnerAndRepo(url: String): Pair<String, String> {
        val clean = url.removeSuffix(".git").removeSuffix("/")
        val parts = clean.split("/")
        if (parts.size >= 2) {
            val repo = parts.last()
            val owner = parts[parts.size - 2]
            return Pair(owner, repo)
        }
        return Pair("unknown", "project")
    }

    /**
     * Clones a repository with real, non-faked progress tracking via JGit ProgressMonitor.
     */
    suspend fun cloneRepository(
        repoUrl: String,
        targetDir: File,
        credentials: GitCredentials? = null,
        onProgress: (GitProgressUpdate) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val normalizedUrl = normalizeRepoUrl(repoUrl)
            if (normalizedUrl.isEmpty()) {
                throw IllegalArgumentException("Invalid Git repository URL provided.")
            }

            if (targetDir.exists() && targetDir.listFiles()?.isNotEmpty() == true) {
                targetDir.deleteRecursively()
            }
            targetDir.mkdirs()

            onProgress(
                GitProgressUpdate(
                    stage = "Connecting...",
                    statusMessage = "Connecting to remote repository $normalizedUrl...",
                    percentage = 0.05f
                )
            )

            val cloneCommand = Git.cloneRepository()
                .setURI(normalizedUrl)
                .setDirectory(targetDir)
                .setCloneAllBranches(true)

            if (credentials != null && credentials.username.isNotBlank() && credentials.tokenOrPassword.isNotBlank()) {
                cloneCommand.setCredentialsProvider(
                    UsernamePasswordCredentialsProvider(credentials.username, credentials.tokenOrPassword)
                )
            }

            val monitor = object : ProgressMonitor {
                private var currentTask = ""
                private var totalWork = 0
                private var completedWork = 0

                override fun start(totalTasks: Int) {
                    onProgress(
                        GitProgressUpdate(
                            stage = "Cloning repository...",
                            statusMessage = "Starting Git clone operations ($totalTasks tasks)...",
                            percentage = 0.1f
                        )
                    )
                }

                override fun beginTask(title: String?, total: Int) {
                    currentTask = title ?: "Processing"
                    totalWork = total
                    completedWork = 0
                    val pct = if (total > 0) (completedWork.toFloat() / total) * 0.8f + 0.1f else 0.2f
                    onProgress(
                        GitProgressUpdate(
                            stage = currentTask,
                            current = completedWork,
                            total = totalWork,
                            percentage = pct.coerceIn(0.1f, 0.95f),
                            statusMessage = "$currentTask..."
                        )
                    )
                }

                override fun update(completed: Int) {
                    completedWork += completed
                    val pct = if (totalWork > 0) {
                        (completedWork.toFloat() / totalWork).coerceIn(0f, 1f) * 0.8f + 0.1f
                    } else 0.5f

                    onProgress(
                        GitProgressUpdate(
                            stage = currentTask,
                            current = completedWork,
                            total = totalWork,
                            percentage = pct.coerceIn(0.1f, 0.95f),
                            statusMessage = if (totalWork > 0) "$currentTask: $completedWork / $totalWork" else "$currentTask..."
                        )
                    )
                }

                override fun endTask() {}
                override fun isCancelled(): Boolean = false
            }

            cloneCommand.setProgressMonitor(monitor)
            val git = cloneCommand.call()
            git.close()

            onProgress(
                GitProgressUpdate(
                    stage = "Completed",
                    percentage = 1.0f,
                    statusMessage = "Project imported successfully.",
                    isCompleted = true
                )
            )

            targetDir
        }.onFailure { err ->
            val friendlyReason = mapGitErrorToFriendlyMessage(err)
            onProgress(
                GitProgressUpdate(
                    stage = "Failed",
                    percentage = 0f,
                    statusMessage = "Clone failed: $friendlyReason",
                    error = friendlyReason
                )
            )
        }
    }

    /**
     * Pulls updates for an existing local repository.
     */
    suspend fun pullRepository(
        repoDir: File,
        credentials: GitCredentials? = null,
        onProgress: (GitProgressUpdate) -> Unit
    ): Result<GitPullSummary> = withContext(Dispatchers.IO) {
        runCatching {
            if (!repoDir.exists() || !File(repoDir, ".git").exists()) {
                throw IllegalStateException("Directory is not an initialized Git repository.")
            }

            onProgress(GitProgressUpdate(stage = "Connecting...", statusMessage = "Checking remote changes...", percentage = 0.2f))

            val git = Git.open(repoDir)
            val pullCommand = git.pull()

            if (credentials != null && credentials.username.isNotBlank() && credentials.tokenOrPassword.isNotBlank()) {
                pullCommand.setCredentialsProvider(
                    UsernamePasswordCredentialsProvider(credentials.username, credentials.tokenOrPassword)
                )
            }

            val pullResult = pullCommand.call()
            val isSuccess = pullResult.isSuccessful

            val mergeResult = pullResult.mergeResult
            val conflicts = mutableListOf<GitMergeConflict>()

            if (mergeResult != null && mergeResult.mergeStatus == MergeResult.MergeStatus.CONFLICTING) {
                val conflictingFiles = mergeResult.conflicts?.keys ?: emptySet()
                for (fileKey in conflictingFiles) {
                    val conflictFile = File(repoDir, fileKey)
                    val content = if (conflictFile.exists()) conflictFile.readText() else ""
                    conflicts.add(
                        GitMergeConflict(
                            filePath = fileKey,
                            localVersion = extractConflictVersion(content, isLocal = true),
                            remoteVersion = extractConflictVersion(content, isLocal = false),
                            conflictMarkerText = content
                        )
                    )
                }
            }

            // Inspect changed files
            val status = git.status().call()
            val changedFiles = (status.modified + status.changed + status.added + status.removed).distinct()

            git.close()

            GitPullSummary(
                isSuccessful = isSuccess,
                statusMessage = if (conflicts.isNotEmpty()) "Merge conflict detected in ${conflicts.size} file(s)" else "Repository up to date.",
                updatedFiles = changedFiles,
                conflicts = conflicts
            )
        }
    }

    /**
     * Gets real Git status (modified, added, deleted, untracked, conflicting).
     */
    suspend fun getStatus(repoDir: File): GitStatusResult = withContext(Dispatchers.IO) {
        if (!repoDir.exists() || !File(repoDir, ".git").exists()) {
            return@withContext GitStatusResult()
        }

        try {
            val git = Git.open(repoDir)
            val status = git.status().call()
            val result = GitStatusResult(
                modified = (status.modified + status.changed).distinct().sorted(),
                added = status.added.distinct().sorted(),
                deleted = (status.missing + status.removed).distinct().sorted(),
                untracked = status.untracked.distinct().sorted(),
                conflicting = status.conflicting.distinct().sorted()
            )
            git.close()
            result
        } catch (_: Exception) {
            GitStatusResult()
        }
    }

    /**
     * Generates a line-by-line diff for changed files.
     */
    suspend fun getDiff(repoDir: File, targetFilePath: String? = null): List<GitFileDiff> = withContext(Dispatchers.IO) {
        if (!repoDir.exists() || !File(repoDir, ".git").exists()) {
            return@withContext emptyList()
        }

        val diffs = mutableListOf<GitFileDiff>()
        try {
            val git = Git.open(repoDir)
            val diffEntries = git.diff().setShowNameAndStatusOnly(false).call()
            val out = ByteArrayOutputStream()
            val formatter = DiffFormatter(out).apply {
                setRepository(git.repository)
                setDiffComparator(RawTextComparator.DEFAULT)
            }
            for (entry in diffEntries) {
                val path = entry.newPath.ifEmpty { entry.oldPath }
                if (targetFilePath != null && path != targetFilePath) continue
                out.reset()
                formatter.format(entry)
                val diffOutput = out.toString("UTF-8")
                val parsedLines = parseUnifiedDiffLines(diffOutput)
                diffs.add(
                    GitFileDiff(
                        filePath = path,
                        diffContent = diffOutput,
                        lines = parsedLines
                    )
                )
            }
            formatter.close()
            git.close()
        } catch (_: Exception) {
            // Fallback: file comparison with working tree
            val status = try { Git.open(repoDir).status().call() } catch (_: Exception) { null }
            val modifiedFiles = (status?.modified?.toList() ?: emptyList()) + (status?.untracked?.toList() ?: emptyList())
            for (path in modifiedFiles) {
                if (targetFilePath != null && path != targetFilePath) continue
                val file = File(repoDir, path)
                if (file.exists() && file.isFile) {
                    val content = try { file.readText() } catch (_: Exception) { "" }
                    diffs.add(
                        GitFileDiff(
                            filePath = path,
                            diffContent = "+ " + content.lines().joinToString("\n+ "),
                            lines = content.lines().map { DiffLine(DiffLineType.ADDED, it) }
                        )
                    )
                }
            }
        }
        diffs
    }

    /**
     * Commits staged or specified files to the local Git repository.
     */
    suspend fun commit(
        repoDir: File,
        message: String,
        selectedFiles: List<String>? = null,
        authorName: String = "HTML Live User",
        authorEmail: String = "user@htmllive.dev"
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            if (!repoDir.exists() || !File(repoDir, ".git").exists()) {
                throw IllegalStateException("Not a valid Git repository.")
            }
            if (message.isBlank()) {
                throw IllegalArgumentException("Commit message cannot be empty.")
            }

            val git = Git.open(repoDir)

            if (selectedFiles.isNullOrEmpty()) {
                git.add().addFilepattern(".").call()
            } else {
                for (file in selectedFiles) {
                    git.add().addFilepattern(file).call()
                }
            }

            val commit = git.commit()
                .setMessage(message.trim())
                .setAuthor(authorName, authorEmail)
                .setCommitter(authorName, authorEmail)
                .call()

            val commitId = commit.name
            git.close()
            commitId
        }
    }

    /**
     * Pushes commits to remote repository with authentication.
     */
    suspend fun push(
        repoDir: File,
        credentials: GitCredentials? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val git = Git.open(repoDir)
            val pushCommand = git.push()

            if (credentials != null && credentials.username.isNotBlank() && credentials.tokenOrPassword.isNotBlank()) {
                pushCommand.setCredentialsProvider(
                    UsernamePasswordCredentialsProvider(credentials.username, credentials.tokenOrPassword)
                )
            }

            val pushResults = pushCommand.call()
            git.close()

            val messages = pushResults.mapNotNull { it.messages }.joinToString("\n")
            if (messages.isNotBlank()) messages else "Pushed changes successfully to remote."
        }
    }

    /**
     * Lists local and remote branches.
     */
    suspend fun getBranches(repoDir: File): List<String> = withContext(Dispatchers.IO) {
        if (!repoDir.exists() || !File(repoDir, ".git").exists()) return@withContext emptyList()
        try {
            val git = Git.open(repoDir)
            val branchRefs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call()
            val branchNames = branchRefs.map { ref ->
                Repository.shortenRefName(ref.name).removePrefix("origin/")
            }.distinct().filter { it.isNotBlank() }
            git.close()
            branchNames.ifEmpty { listOf("main") }
        } catch (_: Exception) {
            listOf("main")
        }
    }

    /**
     * Switches (checkouts) branch and updates local working tree.
     */
    suspend fun switchBranch(repoDir: File, branchName: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val git = Git.open(repoDir)
            git.checkout()
                .setName(branchName)
                .call()
            git.close()
        }
    }

    /**
     * Resolves a merge conflict manually or by keeping local/remote.
     */
    suspend fun resolveConflict(
        repoDir: File,
        filePath: String,
        resolvedContent: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(repoDir, filePath)
            file.writeText(resolvedContent)

            val git = Git.open(repoDir)
            git.add().addFilepattern(filePath).call()
            git.close()
        }
    }

    /**
     * Gathers project info, remote info, branch, and calculates directory size.
     */
    suspend fun getRepoInfo(repoDir: File, repoUrl: String, projectId: String): GitRepositoryInfo = withContext(Dispatchers.IO) {
        var defaultBranch = "main"
        var currentBranch = "main"
        var remoteUrl = repoUrl
        val (owner, repoName) = extractOwnerAndRepo(repoUrl)

        if (repoDir.exists() && File(repoDir, ".git").exists()) {
            try {
                val git = Git.open(repoDir)
                currentBranch = git.repository.branch ?: "main"
                val config = git.repository.config
                val remoteUri = config.getString("remote", "origin", "url")
                if (!remoteUri.isNullOrBlank()) {
                    remoteUrl = remoteUri
                }
                git.close()
            } catch (_: Exception) {}
        }

        val totalSize = calculateDirectorySize(repoDir)
        val hasIndexHtml = File(repoDir, "index.html").exists()
        val hasPackageJson = File(repoDir, "package.json").exists()
        val hasReadme = File(repoDir, "README.md").exists() || File(repoDir, "readme.md").exists() || File(repoDir, "README.txt").exists()
        val detectedType = detectProjectType(repoDir)

        GitRepositoryInfo(
            projectId = projectId,
            repoUrl = remoteUrl,
            owner = owner,
            repoName = repoName,
            defaultBranch = defaultBranch,
            currentBranch = currentBranch,
            remoteUrl = remoteUrl,
            lastSyncTime = System.currentTimeMillis(),
            localProjectSizeBytes = totalSize,
            detectedType = detectedType,
            hasIndexHtml = hasIndexHtml,
            hasPackageJson = hasPackageJson,
            hasReadme = hasReadme
        )
    }

    /**
     * Detects project type based on actual files.
     */
    fun detectProjectType(repoDir: File): DetectedProjectType {
        if (!repoDir.exists()) return DetectedProjectType.UNKNOWN

        val indexHtml = File(repoDir, "index.html")
        val packageJson = File(repoDir, "package.json")
        val viteConfig = File(repoDir, "vite.config.js").exists() || File(repoDir, "vite.config.ts").exists()
        val webpackConfig = File(repoDir, "webpack.config.js").exists()

        if (packageJson.exists() || viteConfig || webpackConfig) {
            return DetectedProjectType.NODE_WEB_PROJECT
        }

        if (indexHtml.exists()) {
            val content = try { indexHtml.readText() } catch (_: Exception) { "" }
            if (content.contains("<canvas", ignoreCase = true) || content.contains("requestAnimationFrame", ignoreCase = true)) {
                return DetectedProjectType.CANVAS_GAME
            }
            val hasCss = File(repoDir, "style.css").exists() || File(repoDir, "css").isDirectory
            val hasJs = File(repoDir, "script.js").exists() || File(repoDir, "js").isDirectory || File(repoDir, "main.js").exists()

            return if (hasCss || hasJs) {
                DetectedProjectType.HTML_CSS_JS
            } else {
                DetectedProjectType.STATIC_HTML
            }
        }

        // Check any .html files
        val anyHtml = repoDir.walkTopDown().maxDepth(2).any { it.isFile && it.extension.equals("html", ignoreCase = true) }
        if (anyHtml) return DetectedProjectType.JS_WEB_APP

        return DetectedProjectType.UNKNOWN
    }

    fun readReadme(repoDir: File): String? {
        val candidates = listOf("README.md", "readme.md", "README.txt", "Readme.md")
        for (name in candidates) {
            val file = File(repoDir, name)
            if (file.exists() && file.isFile) {
                return try { file.readText() } catch (_: Exception) { null }
            }
        }
        return null
    }

    fun writeReadme(repoDir: File, content: String): Boolean {
        val candidates = listOf("README.md", "readme.md", "README.txt")
        val target = candidates.map { File(repoDir, it) }.firstOrNull { it.exists() } ?: File(repoDir, "README.md")
        return try {
            target.writeText(content)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun parseUnifiedDiffLines(diffText: String): List<DiffLine> {
        val lines = diffText.lines()
        val result = mutableListOf<DiffLine>()
        var oldLine = 1
        var newLine = 1

        for (line in lines) {
            if (line.startsWith("@@")) {
                // Header chunk: @@ -1,5 +1,6 @@
                result.add(DiffLine(DiffLineType.CONTEXT, line))
                continue
            }
            if (line.startsWith("---") || line.startsWith("+++") || line.startsWith("diff --git") || line.startsWith("index ")) {
                continue
            }
            if (line.startsWith("+")) {
                result.add(DiffLine(DiffLineType.ADDED, line.drop(1), null, newLine++))
            } else if (line.startsWith("-")) {
                result.add(DiffLine(DiffLineType.DELETED, line.drop(1), oldLine++, null))
            } else {
                result.add(DiffLine(DiffLineType.CONTEXT, if (line.startsWith(" ")) line.drop(1) else line, oldLine++, newLine++))
            }
        }
        return result
    }

    private fun extractConflictVersion(content: String, isLocal: Boolean): String {
        val markerStart = "<<<<<<<"
        val markerMid = "======="
        val markerEnd = ">>>>>>>"

        val lines = content.lines()
        val builder = StringBuilder()
        var insideConflict = false
        var capturing = false

        for (line in lines) {
            if (line.startsWith(markerStart)) {
                insideConflict = true
                capturing = isLocal
                continue
            }
            if (line.startsWith(markerMid)) {
                capturing = !isLocal
                continue
            }
            if (line.startsWith(markerEnd)) {
                insideConflict = false
                capturing = false
                continue
            }
            if (!insideConflict || capturing) {
                builder.appendLine(line)
            }
        }
        return builder.toString()
    }

    private fun calculateDirectorySize(dir: File): Long {
        if (!dir.exists()) return 0L
        var total = 0L
        dir.walkTopDown().forEach { file ->
            if (file.isFile) total += file.length()
        }
        return total
    }

    private fun mapGitErrorToFriendlyMessage(err: Throwable): String {
        val msg = err.message ?: ""
        return when {
            msg.contains("not found", ignoreCase = true) || msg.contains("404") ->
                "The repository could not be found. Check if the URL is correct."
            msg.contains("not authorized", ignoreCase = true) || msg.contains("authentication", ignoreCase = true) || msg.contains("401") || msg.contains("403") ->
                "Authentication required. The repository is private or requires a Personal Access Token."
            msg.contains("UnknownHostException", ignoreCase = true) || msg.contains("timed out", ignoreCase = true) ->
                "Network unavailable or connection timed out. Please check your internet connection."
            msg.contains("SSL", ignoreCase = true) || msg.contains("certificate", ignoreCase = true) ->
                "TLS/SSL certificate validation error while connecting to remote host."
            msg.contains("conflict", ignoreCase = true) ->
                "Merge conflict occurred between local modifications and remote branch."
            msg.contains("No space left", ignoreCase = true) ->
                "Insufficient device storage space to clone repository."
            else -> msg.ifEmpty { "An unexpected Git operation error occurred." }
        }
    }
}

data class GitPullSummary(
    val isSuccessful: Boolean,
    val statusMessage: String,
    val updatedFiles: List<String>,
    val conflicts: List<GitMergeConflict> = emptyList()
)
