package com.example.data.apk

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.model.ProjectFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ApkBuilderManager(private val context: Context) {

    private val apksDir: File by lazy {
        File(context.filesDir, "generated_apks").apply { if (!exists()) mkdirs() }
    }

    suspend fun buildApk(
        config: ApkConfig,
        files: List<ProjectFile>,
        onProgress: (ApkBuildProgress) -> Unit
    ): ApkValidationResult = withContext(Dispatchers.IO) {
        val validationErr = config.validate()
        if (validationErr != null) {
            onProgress(ApkBuildProgress(step = ApkBuildStep.FAILED, error = validationErr))
            return@withContext ApkValidationResult(isValid = false, errorMessage = validationErr)
        }

        try {
            // Step 1: Prepare directories
            onProgress(
                ApkBuildProgress(
                    step = ApkBuildStep.PACKAGING,
                    statusMessage = "Preparing APK workspace and template...",
                    percentage = 0.15f,
                    isPackaging = true
                )
            )

            val safeName = config.appName.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val unsignedApk = File(apksDir, "${safeName}_unsigned.apk")
            val finalSignedApk = File(apksDir, "${safeName}.apk")

            if (unsignedApk.exists()) unsignedApk.delete()
            if (finalSignedApk.exists()) finalSignedApk.delete()

            // Step 2: Open WebView template APK from assets
            onProgress(
                ApkBuildProgress(
                    step = ApkBuildStep.PACKAGING,
                    statusMessage = "Loading reusable Android WebView runtime template...",
                    percentage = 0.35f,
                    isPackaging = true
                )
            )

            val templateEntries = mutableMapOf<String, ByteArray>()
            context.assets.open("template/webview_template.apk").use { assetIn ->
                ZipInputStream(assetIn).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory && !entry.name.startsWith("META-INF/")) {
                            templateEntries[entry.name] = zis.readBytes()
                        }
                        entry = zis.nextEntry
                    }
                }
            }

            // Step 3: Bundle project files into assets/website/
            onProgress(
                ApkBuildProgress(
                    step = ApkBuildStep.PACKAGING,
                    statusMessage = "Bundling ${files.size} HTML/CSS/JS and asset files...",
                    percentage = 0.55f,
                    isPackaging = true
                )
            )

            // Generate app_config.json
            val configJson = JSONObject().apply {
                put("appName", config.appName)
                put("packageName", config.packageName)
                put("versionName", config.versionName)
                put("versionCode", config.versionCode)
                put("username", config.username)
                put("showStartupScreen", config.showStartupScreen)
                put("internetEnabled", config.internetAccess)
                put("entryFile", config.entryFile)
            }
            val configBytes = configJson.toString(2).toByteArray(Charsets.UTF_8)

            // Write unsigned APK
            ZipOutputStream(FileOutputStream(unsignedApk)).use { zos ->
                // Write template files (AndroidManifest, classes.dex, resources.arsc, res)
                for ((name, bytes) in templateEntries) {
                    val ze = ZipEntry(name)
                    zos.putNextEntry(ze)
                    zos.write(bytes)
                    zos.closeEntry()
                }

                // Write app_config.json
                val configEntry = ZipEntry("assets/app_config.json")
                zos.putNextEntry(configEntry)
                zos.write(configBytes)
                zos.closeEntry()

                // Write bundled website files preserving exact relative structure
                for (file in files) {
                    val relativePath = file.name.trimStart('/')
                    val entryPath = "assets/website/$relativePath"
                    val fileBytes = file.content.toByteArray(Charsets.UTF_8)

                    val ze = ZipEntry(entryPath)
                    zos.putNextEntry(ze)
                    zos.write(fileBytes)
                    zos.closeEntry()
                }
            }

            // Step 4: Sign APK with supported lightweight mechanism
            onProgress(
                ApkBuildProgress(
                    step = ApkBuildStep.PACKAGING,
                    statusMessage = "Signing APK with cryptographic signature (v1 scheme)...",
                    percentage = 0.80f,
                    isPackaging = true
                )
            )

            ApkSignerHelper.signApk(unsignedApk, finalSignedApk)
            if (unsignedApk.exists()) unsignedApk.delete()

            // Step 5: Real APK Validation
            onProgress(
                ApkBuildProgress(
                    step = ApkBuildStep.VERIFYING,
                    statusMessage = "Verifying APK integrity, manifest, and assets...",
                    percentage = 0.95f,
                    isPackaging = true
                )
            )

            val validation = validateApk(finalSignedApk, config, files.size)

            if (validation.isValid) {
                onProgress(
                    ApkBuildProgress(
                        step = ApkBuildStep.READY,
                        statusMessage = "APK created and verified successfully.",
                        percentage = 1.0f,
                        isPackaging = false
                    )
                )
            } else {
                onProgress(
                    ApkBuildProgress(
                        step = ApkBuildStep.FAILED,
                        statusMessage = "APK validation failed: ${validation.errorMessage}",
                        percentage = 0f,
                        error = validation.errorMessage
                    )
                )
            }

            validation
        } catch (e: Exception) {
            val err = "APK packaging failed: ${e.message}"
            onProgress(ApkBuildProgress(step = ApkBuildStep.FAILED, statusMessage = err, error = err))
            ApkValidationResult(isValid = false, errorMessage = err)
        }
    }

    fun validateApk(apkFile: File, config: ApkConfig, localFilesCount: Int): ApkValidationResult {
        if (!apkFile.exists() || apkFile.length() <= 0) {
            return ApkValidationResult(isValid = false, errorMessage = "Generated APK file does not exist or is empty.")
        }

        var hasManifest = false
        var hasClassesDex = false
        var hasWebsiteAssets = false
        var hasEntryHtml = false
        var hasManifestMf = false
        var hasCertSf = false
        var hasCertRsa = false
        var totalEntries = 0

        try {
            ZipFile(apkFile).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    totalEntries++
                    when (entry.name) {
                        "AndroidManifest.xml" -> hasManifest = true
                        "classes.dex" -> hasClassesDex = true
                        "META-INF/MANIFEST.MF" -> hasManifestMf = true
                        "META-INF/CERT.SF" -> hasCertSf = true
                        "META-INF/CERT.RSA" -> hasCertRsa = true
                        "assets/website/${config.entryFile}" -> hasEntryHtml = true
                    }
                    if (entry.name.startsWith("assets/website/")) {
                        hasWebsiteAssets = true
                    }
                }
            }
        } catch (e: Exception) {
            return ApkValidationResult(isValid = false, errorMessage = "Corrupt APK archive: ${e.message}")
        }

        if (!hasManifest) {
            return ApkValidationResult(isValid = false, errorMessage = "AndroidManifest.xml is missing from APK.")
        }
        if (!hasClassesDex) {
            return ApkValidationResult(isValid = false, errorMessage = "classes.dex is missing from APK.")
        }
        if (!hasWebsiteAssets || !hasEntryHtml) {
            return ApkValidationResult(isValid = false, errorMessage = "Website entry file '${config.entryFile}' was not bundled into APK.")
        }
        if (!hasManifestMf || !hasCertSf || !hasCertRsa) {
            return ApkValidationResult(isValid = false, errorMessage = "APK signature files (META-INF) are missing.")
        }

        return ApkValidationResult(
            isValid = true,
            outputFile = apkFile,
            fileSizeBytes = apkFile.length(),
            hasManifest = true,
            hasClassesDex = true,
            hasWebsiteAssets = true,
            hasEntryHtml = true,
            isSigned = true,
            localFilesCount = localFilesCount,
            internetEnabled = config.internetAccess
        )
    }

    fun getInstallIntent(apkFile: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getShareIntent(apkFile: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, apkFile.name)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
    }
}
