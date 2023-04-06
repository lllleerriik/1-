package com.webiki.bucketlist

import android.app.Application
import com.orm.SugarRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

class Goal(private var label: String, private var description: String, private var deadline: LocalDate) : SugarRecord() {
    companion object {
        var Separator = "$%$"
        var DateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")


        fun toGoal(str: String): Goal {
            return toGoal(str.split(Separator) as Array<String>)
        }

        fun toGoal(items: Array<String>): Goal {
            return Goal(items[0], items[1], LocalDate.parse(items[2], DateFormat))
        }
    }

    public constructor() : this("", "", LocalDate.MIN) {}

    override fun toString(): String {
        return "Название: $label. Описание: $description. Выполнить до: ${deadline.format(DateFormat)}."
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Goal

        if (label != other.label ||
            description != other.description ||
            deadline != other.deadline) return false

        return true
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + deadline.hashCode()
        return result
    }
}