package com.example

import com.example.data.apk.ApkConfig
import com.example.data.apk.FeatureStatus
import com.example.data.apk.ProjectAnalyzer
import com.example.data.apk.WarningLevel
import com.example.model.ProjectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ApkBuilderTest {

    @Test
    fun `valid ApkConfig passes validation`() {
        val config = ApkConfig(
            projectId = "p1",
            appName = "Space Invaders",
            packageName = "com.games.spaceinvaders",
            versionName = "1.0",
            versionCode = 1,
            username = "Developer",
            entryFile = "index.html"
        )
        assertNull(config.validate())
    }

    @Test
    fun `invalid package names are caught by validator`() {
        val noDot = ApkConfig("p1", "Game", "invalidpackage", "1.0", 1)
        assertNotNull(noDot.validate())

        val startsWithDigit = ApkConfig("p1", "Game", "com.123game.test", "1.0", 1)
        assertNotNull(startsWithDigit.validate())

        val emptyAppName = ApkConfig("p1", "   ", "com.example.app", "1.0", 1)
        assertNotNull(emptyAppName.validate())

        val negativeCode = ApkConfig("p1", "Game", "com.example.app", "1.0", -1)
        assertNotNull(negativeCode.validate())
    }

    @Test
    fun `project analyzer correctly sums resource sizes`() {
        val files = listOf(
            ProjectFile("f1", "p1", "index.html", "html", "<html><head><link rel='stylesheet' href='style.css'></head><body><h1>Hello</h1></body></html>"),
            ProjectFile("f2", "p1", "style.css", "css", "body { background: #000; color: #fff; }"),
            ProjectFile("f3", "p1", "game.js", "js", "const canvas = document.createElement('canvas'); const ctx = canvas.getContext('2d');")
        )

        val analysis = ProjectAnalyzer.analyzeResources(files)
        assertEquals(3, analysis.totalFilesCount)
        assertTrue(analysis.htmlSizeBytes > 0)
        assertTrue(analysis.cssSizeBytes > 0)
        assertTrue(analysis.jsSizeBytes > 0)
        assertEquals(analysis.totalSizeBytes, analysis.htmlSizeBytes + analysis.cssSizeBytes + analysis.jsSizeBytes)
    }

    @Test
    fun `project analyzer detects canvas and modern web features`() {
        val files = listOf(
            ProjectFile(
                "f1", "p1", "game.js", "js",
                """
                const canvas = document.getElementById('c');
                const ctx = canvas.getContext('2d');
                localStorage.setItem('highScore', 100);
                fetch('https://api.example.com/score');
                """.trimIndent()
            )
        )

        val compat = ProjectAnalyzer.checkCompatibility(files)
        val canvasFeature = compat.features.find { it.name.contains("Canvas", ignoreCase = true) }
        assertNotNull(canvasFeature)
        assertEquals(FeatureStatus.SUPPORTED, canvasFeature?.status)

        val storageFeature = compat.features.find { it.name.contains("LocalStorage", ignoreCase = true) }
        assertNotNull(storageFeature)
        assertEquals(FeatureStatus.SUPPORTED, storageFeature?.status)
    }

    @Test
    fun `project error check verifies entry file and missing references`() {
        val files = listOf(
            ProjectFile("f1", "p1", "index.html", "html", "<html><head><link rel='stylesheet' href='missing.css'></head><body></body></html>")
        )

        val check = ProjectAnalyzer.checkProjectErrors(files, "index.html")
        assertTrue(check.hasEntryFile)
        assertTrue(check.canPackage)
        assertTrue(check.missingFiles.contains("missing.css"))
    }

    @Test
    fun `auto package name generator creates valid android package identifier`() {
        val pkg = ApkConfig.autoGeneratePackage("Atp", "My HTML Game")
        assertEquals("com.atp.myhtmlgame", pkg)
        assertNull(ApkConfig.validatePackageName(pkg))
    }

    @Test
    fun `compatibility check produces all 12 evaluation categories`() {
        val files = listOf(
            ProjectFile("f1", "p1", "index.html", "html", "<html><body><canvas></canvas><audio></audio><video></video></body></html>")
        )
        val report = ProjectAnalyzer.checkCompatibility(files)
        val names = report.features.map { it.name }
        assertTrue(names.contains("HTML"))
        assertTrue(names.contains("CSS"))
        assertTrue(names.contains("JavaScript"))
        assertTrue(names.contains("Canvas"))
        assertTrue(names.contains("SVG"))
        assertTrue(names.contains("Local Assets"))
        assertTrue(names.contains("LocalStorage"))
        assertTrue(names.contains("Fetch"))
        assertTrue(names.contains("Audio"))
        assertTrue(names.contains("Video"))
        assertTrue(names.contains("Web APIs"))
        assertTrue(names.contains("External Resources"))
    }
}
