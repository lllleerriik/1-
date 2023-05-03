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
        editor.commit()
    }

    val getIntFromStorage = { key: String, defaultValue: Int -> sharedPreferences.getInt(key, defaultValue) }

    val addIntToStorage = { key: String, value: Int ->
        editor.putInt(key, value)
        editor.commit()
    }

    val getStringSetFromStorage = { key: String -> sharedPreferences.getStringSet(key, mutableSetOf())!! }

    val addStringSetToStorage = { key: String, value: List<String> ->
        editor.putStringSet(key, value.toMutableSet())
        editor.commit()
    }

    val reduceStringSetFromStorage = { key: String, value: String? ->
        refreshStringSetFromStorage(key, value) { set, value -> set.remove(value) }
    }

    val extendStringSetInStorage = { key: String, value: String? ->
        refreshStringSetFromStorage(key, value) { set, value -> set.add(value) }
    }

    /**
     * Получает, изменяет по определённому сценарию и сохраняет набор строк
     *
     * @param key Ключ, по которому находится и сохраянется набор строк
     * @param value Строка, которая фигурирует в изменении набора строк
     * @param action Сценарий изменения набора строк
     * @exception NullPointerException Если переданное значение - null
     */
    private fun refreshStringSetFromStorage(
        key: String,
        value: String?,
        action: (MutableSet<String>, String) -> Boolean
    ): Boolean {
        val set = sharedPreferences.getStringSet(key, null) ?: mutableSetOf<String>()
        action(set, value!!)
        editor.putStringSet(key, set)
        return editor.commit()
    }
}