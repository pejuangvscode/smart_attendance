package com.example.smartattendance

// Shared data class representing the schedule info passed between screens
data class CurrentScheduleInfo(
    val courseName: String,
    val time: String,
    val isActive: Boolean,
    val scheduleId: Int? = null,
    val courseId: Int? = null
)

