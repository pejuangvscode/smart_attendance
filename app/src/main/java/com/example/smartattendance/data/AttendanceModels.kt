package com.example.smartattendance.data

import android.graphics.Bitmap

enum class AttendanceStatus {
    PENDING,
    APPROVED,
    REJECTED,
    NOT_YET,
    PRESENT
}

data class AttendanceReport(
    val id: String,
    val studentName: String,
    val className: String,
    val subject: String,
    val date: String,
    val time: String,
    val timestamp: String,
    val isLate: Boolean,
    val photo: Bitmap?,
    val reason: String? = null,
    val status: AttendanceStatus = AttendanceStatus.PENDING
)

data class AttendanceRecord(
    val className: String,
    val subject: String,
    val type: String,
    val status: AttendanceStatus,
    val time: String = "",
    val date: String = ""
)
