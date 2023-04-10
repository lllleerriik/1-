package com.webiki.bucketlist

import android.app.Application
import com.orm.SugarRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

class Goal(
    private var label: String,
    private var isCompleted: Boolean
    ) : SugarRecord(), Comparable<Any> {
    public constructor() : this("", false)
    public constructor(newGoal: Goal) : this(newGoal.label, newGoal.isCompleted)

    fun getCompleted(): Boolean { return this.isCompleted }

    fun setCompleted(flag: Boolean) { this.isCompleted = flag }

    fun getLabel(): String { return this.label }

    override fun toString(): String {
        return label
    }

    public fun getDescription(): String {
        return "${this.label}: ${if (this.isCompleted) "" else "не "}выполнена"
    }

    override fun compareTo(other: Any): Int {
        if (javaClass != other?.javaClass) throw IllegalArgumentException("Не цель")
        other as Goal
        return this.label.compareTo(other.label)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Goal

        return label == other.label && isCompleted == other.isCompleted
    }

    override fun hashCode(): Int {
        return label.hashCode() * 61 + isCompleted.hashCode()
    }
}