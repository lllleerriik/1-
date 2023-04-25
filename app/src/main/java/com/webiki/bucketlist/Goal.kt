package com.webiki.bucketlist

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

    fun getCompleted(): Boolean { return this.isCompleted }

    fun setCompleted(flag: Boolean) { this.isCompleted = flag }

    fun getLabel(): String { return this.label }

    fun getCreateDate(): Long { return this.createDate }

    fun getPriority(): GoalPriority { return this.priority }

    override fun toString(): String {
        return label
    }

    public fun getDescription(): String {
        return "${this.label}: ${if (this.isCompleted) "" else "не "}выполнена; время создания (мс): $createDate; приоритет: $priority"
    }

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