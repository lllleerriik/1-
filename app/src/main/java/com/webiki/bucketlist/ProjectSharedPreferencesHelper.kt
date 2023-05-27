package com.webiki.bucketlist

import android.content.Context

/**
 * Класс-помощник создания и заполнения SharedPreferences
 *
 * @param ctx Контекст создания SharedPreferences
 */
class ProjectSharedPreferencesHelper(ctx: Context) {
    private val sharedPreferences = ctx.getSharedPreferences(
        ctx.getString(R.string.sharedPreferencesName),
        Context.MODE_PRIVATE
    )
    private val editor = sharedPreferences.edit()

    val getBooleanFromStorage = { key: String, defaultValue: Boolean -> sharedPreferences.getBoolean(key, defaultValue) }

    val addBooleanToStorage = { key: String, value: Boolean ->
        editor.putBoolean(key, value)
        editor.apply()
        true
    }

    val getIntFromStorage = { key: String, defaultValue: Int -> sharedPreferences.getInt(key, defaultValue) }

    val addIntToStorage = { key: String, value: Int ->
        editor.putInt(key, value)
        editor.apply()
        true
    }

    val getStringSetFromStorage = { key: String -> sharedPreferences.getStringSet(key, mutableSetOf())!! }

    val addStringSetToStorage = { key: String, value: List<String> ->
        editor.putStringSet(key, value.toMutableSet())
        editor.apply()
        true
    }
}