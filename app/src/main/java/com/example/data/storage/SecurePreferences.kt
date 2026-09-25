package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AiProvider
import com.example.model.AiSettings
import com.example.model.EditorSettings

class SecurePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("htmllive_secure_prefs", Context.MODE_PRIVATE)

    fun getAiSettings(): AiSettings {
        val providerStr = prefs.getString("ai_provider", AiProvider.GEMINI.name) ?: AiProvider.GEMINI.name
        val provider = runCatching { AiProvider.valueOf(providerStr) }.getOrDefault(AiProvider.GEMINI)

        return AiSettings(
            provider = provider,
            geminiApiKey = prefs.getString("gemini_api_key", "") ?: "",
            openAiApiKey = prefs.getString("openai_api_key", "") ?: "",
            customEndpoint = prefs.getString("custom_endpoint", "https://api.openai.com/v1/chat/completions") ?: "",
            customApiKey = prefs.getString("custom_api_key", "") ?: "",
            customModel = prefs.getString("custom_model", "gpt-4o") ?: "gpt-4o",
            selectedModel = prefs.getString("selected_model", "gemini-2.5-flash") ?: "gemini-2.5-flash",
            mode = prefs.getString("ai_mode", "build_explain") ?: "build_explain",
            experienceLevel = prefs.getString("user_experience_level", "beginner") ?: "beginner"
        )
    }

    fun saveAiSettings(settings: AiSettings) {
        prefs.edit()
            .putString("ai_provider", settings.provider.name)
            .putString("gemini_api_key", settings.geminiApiKey)
            .putString("openai_api_key", settings.openAiApiKey)
            .putString("custom_endpoint", settings.customEndpoint)
            .putString("custom_api_key", settings.customApiKey)
            .putString("custom_model", settings.customModel)
            .putString("selected_model", settings.selectedModel)
            .putString("ai_mode", settings.mode)
            .putString("user_experience_level", settings.experienceLevel)
            .apply()
    }

    fun getEditorSettings(): EditorSettings {
        return EditorSettings(
            fontSizeSp = prefs.getFloat("editor_font_size", 14f),
            tabSize = prefs.getInt("editor_tab_size", 2),
            wordWrap = prefs.getBoolean("editor_word_wrap", false),
            autoIndent = prefs.getBoolean("editor_auto_indent", true),
            bracketMatching = prefs.getBoolean("editor_bracket_matching", true),
            showLineNumbers = prefs.getBoolean("editor_line_numbers", true),
            theme = prefs.getString("editor_theme", "dark") ?: "dark",
            beginnerMode = prefs.getBoolean("beginner_mode", true)
        )
    }

    fun saveEditorSettings(settings: EditorSettings) {
        prefs.edit()
            .putFloat("editor_font_size", settings.fontSizeSp)
            .putInt("editor_tab_size", settings.tabSize)
            .putBoolean("editor_word_wrap", settings.wordWrap)
            .putBoolean("editor_auto_indent", settings.autoIndent)
            .putBoolean("editor_bracket_matching", settings.bracketMatching)
            .putBoolean("editor_line_numbers", settings.showLineNumbers)
            .putString("editor_theme", settings.theme)
            .putBoolean("beginner_mode", settings.beginnerMode)
            .apply()
    }

    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean("has_completed_onboarding", false)
    }

    fun setCompletedOnboarding(completed: Boolean) {
        prefs.edit().putBoolean("has_completed_onboarding", completed).apply()
    }

    fun getDownloadedModuleIds(): Set<String> {
        return prefs.getStringSet("downloaded_module_ids", null) ?: setOf(
            "mod_html_foundation",
            "mod_html_interactive",
            "mod_css_box_model",
            "mod_css_flex_grid",
            "mod_js_basics",
            "mod_game_arcade",
            "mod_web_projects"
        )
    }

    fun setDownloadedModuleIds(ids: Set<String>) {
        prefs.edit().putStringSet("downloaded_module_ids", ids).apply()
    }

    fun getCompletedLessonIds(): Set<String> {
        return prefs.getStringSet("completed_lesson_ids", emptySet()) ?: emptySet()
    }

    fun setCompletedLessonIds(ids: Set<String>) {
        prefs.edit().putStringSet("completed_lesson_ids", ids).apply()
    }

    companion object {
        fun maskKey(key: String): String {
            if (key.length <= 8) return "••••••••"
            return key.take(4) + "••••••••" + key.takeLast(4)
        }
    }
}
