package com.zaizoor.notifierpro

import android.content.Context
import android.content.SharedPreferences

object KeywordManager {
    private const val PREF_NAME = "notifier_keywords"
    private const val KEY_SUBJECTS = "subjects"
    private const val KEY_BODY = "body"

    // Default values to keep the app working instantly
    private val defaultSubjects = setOf("sold", "ship now")
    private val defaultBody = setOf("amazon", "order", "order placed")

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getSubjectKeywords(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_SUBJECTS, defaultSubjects) ?: defaultSubjects
    }

    fun getBodyKeywords(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_BODY, defaultBody) ?: defaultBody
    }

    fun saveSubjectKeywords(context: Context, keywords: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_SUBJECTS, keywords).apply()
    }

    fun saveBodyKeywords(context: Context, keywords: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_BODY, keywords).apply()
    }
}
