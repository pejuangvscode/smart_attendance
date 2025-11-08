package com.example.smartattendance.data

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceStatistics(
    val present: Int = 0,
    val late: Int = 0,
    val absent: Int = 0,
    val excused: Int = 0,
    val sick: Int = 0,
    val total: Int = 0,
    val attendancePercentage: Double = 0.0
) {
    val presentPercentage: Float
        get() = if (total > 0) (present * 100f / total) else 0f

    val latePercentage: Float
        get() = if (total > 0) (late * 100f / total) else 0f

    val absentPercentage: Float
        get() = if (total > 0) (absent * 100f / total) else 0f

    val excusedPercentage: Float
        get() = if (total > 0) (excused * 100f / total) else 0f

    val sickPercentage: Float
        get() = if (total > 0) (sick * 100f / total) else 0f

    companion object {
        fun empty() = AttendanceStatistics()

        fun fromOverallStatistic(statistic: com.example.smartattendance.api.OverallStatistic): AttendanceStatistics {
            return AttendanceStatistics(
                present = statistic.total_present,
                late = statistic.total_late,
                absent = statistic.total_absent,
                excused = statistic.total_excused,
                sick = statistic.total_sick,
                total = statistic.total_meetings,
                attendancePercentage = statistic.attendance_percentage
            )
        }
    }
}

@Serializable
data class WeeklyStatus(
    val monday: AttendanceStatus? = null,
    val tuesday: AttendanceStatus? = null,
    val wednesday: AttendanceStatus? = null,
    val thursday: AttendanceStatus? = null,
    val friday: AttendanceStatus? = null
)

@Serializable
enum class AttendanceStatus {
    PRESENT,
    LATE,
    ABSENT,
    EXCUSED,
    SICK
}
