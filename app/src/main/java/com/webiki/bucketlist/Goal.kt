package com.webiki.bucketlist

import android.util.Log
import com.orm.SugarRecord
import com.webiki.bucketlist.enums.GoalCategory
import com.webiki.bucketlist.enums.GoalPriority

/**
 * Класс, описывающий цель
 *
 * @param label Название цели
 * @param isCompleted Выполнена ли цель
 * @param createDate Время создания цели (мс)
 * @param priority Приоритет цели
 */
class Goal(
    private var label: String,
    private var isCompleted: Boolean,
    private var createDate: Long,
    private var priority: GoalPriority,
    private var category: GoalCategory
    ) : SugarRecord(), Comparable<Any> {
    constructor()
            : this("", false, 0, GoalPriority.Middle, GoalCategory.Other)
    constructor(label: String, isCompleted: Boolean, priority: GoalPriority = GoalPriority.Middle, category: GoalCategory = GoalCategory.Other)
            : this(label, isCompleted, System.currentTimeMillis(), priority, category)

    val GOAL_PARTIES_SEPARATOR = "$)&"

    fun getCompleted(): Boolean { return this.isCompleted }

    fun setCompleted(flag: Boolean) { this.isCompleted = flag }

    fun getLabel(): String { return this.label }

    fun getCreateDate(): Long { return this.createDate }

    fun getPriority(): GoalPriority { return this.priority }

    fun getCategory(): GoalCategory { return this.category }

    override fun toString(): String {
        return label
    }

    companion object {
        val GOAL_PARTIES_SEPARATOR = "$)&"
        fun parseFromString(source: String): Goal {
            val goalParties = source.split(GOAL_PARTIES_SEPARATOR)
            Log.d("DEB", goalParties.toString())
            return Goal(goalParties[0],
                goalParties[1] == "true",
                goalParties[2].toLong(),
                enumValues<GoalPriority>().first { it.value == goalParties[3].toInt() },
                enumValues<GoalCategory>().first { it.value == goalParties[4] })
        }
    }

    fun parseToString(): String = "$label$GOAL_PARTIES_SEPARATOR" +
            "$isCompleted$GOAL_PARTIES_SEPARATOR" +
            "$createDate$GOAL_PARTIES_SEPARATOR" +
            "${priority.value}$GOAL_PARTIES_SEPARATOR" +
            "${category.value}"

    fun withChangedCompletion() = Goal(label, !isCompleted, createDate, priority, category)

    override fun compareTo(other: Any): Int {
        if (javaClass != other.javaClass) throw IllegalArgumentException("Не цель")
        other as Goal
        return this.getCreateDate().compareTo(other.getCreateDate())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Goal

        return label == other.label
                && isCompleted == other.isCompleted
    }

    override fun hashCode(): Int {
        return label.hashCode() * 61 + isCompleted.hashCode()
    }
}