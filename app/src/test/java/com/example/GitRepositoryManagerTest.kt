package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.git.DetectedProjectType
import com.example.data.git.GitRepositoryManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GitRepositoryManagerTest {

    private lateinit var gitManager: GitRepositoryManager

    @Before
    fun setup() {
        gitManager = GitRepositoryManager(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `normalizeRepoUrl adds dot git and cleans trailing slashes`() {
        val raw = "https://github.com/octocat/Hello-World"
        val normalized = gitManager.normalizeRepoUrl(raw)
        assertEquals("https://github.com/octocat/Hello-World.git", normalized)

        val rawWithSlash = "https://github.com/octocat/Hello-World/"
        val normalizedSlash = gitManager.normalizeRepoUrl(rawWithSlash)
        assertEquals("https://github.com/octocat/Hello-World.git", normalizedSlash)
    }

    @Test
    fun `extractOwnerAndRepo parses url parts correctly`() {
        val url = "https://github.com/octocat/Spoon-Knife.git"
        val (owner, repo) = gitManager.extractOwnerAndRepo(url)
        assertEquals("octocat", owner)
        assertEquals("Spoon-Knife", repo)
    }

    @Test
    fun `detectProjectType identifies static HTML projects`() {
        val tempDir = File.createTempFile("test_repo", "").apply {
            delete()
            mkdirs()
        }
        val indexFile = File(tempDir, "index.html")
        indexFile.writeText("<!DOCTYPE html><html><body><h1>Test</h1></body></html>")

        val detected = gitManager.detectProjectType(tempDir)
        assertEquals(DetectedProjectType.STATIC_HTML, detected)

        tempDir.deleteRecursively()
    }
}
