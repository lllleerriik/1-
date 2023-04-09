package com.webiki.bucketlist

import android.app.Application
import com.orm.SugarRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

class Goal(private var label: String) : SugarRecord() {

    public constructor() : this("")

    override fun toString(): String {
        return label
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Goal

        return label == other.label
    }

    override fun hashCode(): Int {
        return label.hashCode()
    }
}