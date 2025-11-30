package com.example.smartattendance.api

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceDetailData(
    val courseName: String,
    val instructorName: String,
    val room: String?,
    val day: String,
    val startTime: String,
    val endTime: String,
    val attendanceDate: String,
    val attendanceTime: String
)

object AttendanceDetailApi {
    suspend fun getAttendanceDetail(
        supabase: SupabaseClient,
        courseId: Int,
        scheduleId: Int
    ): AttendanceDetailData? {
        // Fetch course info
        val courseResult = supabase.postgrest["courses"]
            .select(columns = Columns.list("course_name", "lecturer_id")) {
                filter { eq("course_id", courseId) }
            }
            .decodeList<CourseInfo>()
        if (courseResult.isEmpty()) return null
        val course = courseResult.first()

        // Fetch instructor info
        val instructorResult = supabase.postgrest["users"]
            .select(columns = Columns.list("full_name")) {
                filter { eq("user_id", course.lecturer_id) }
            }
            .decodeList<InstructorInfo>()
        val instructorName = instructorResult.firstOrNull()?.full_name ?: "Unknown"

        // Fetch schedule info
        val scheduleResult = supabase.postgrest["schedules"]
            .select(columns = Columns.list("day", "start_time", "end_time", "room")) {
                filter { eq("schedule_id", scheduleId) }
            }
            .decodeList<ScheduleInfo>()
        if (scheduleResult.isEmpty()) return null
        val schedule = scheduleResult.first()

        // Use current date and start time for attendance
        val attendanceDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy")).uppercase()
        val attendanceTime = schedule.start_time.substring(0,5) // e.g. "07:15"

        return AttendanceDetailData(
            courseName = course.course_name,
            instructorName = instructorName,
            room = schedule.room,
            day = schedule.day,
            startTime = schedule.start_time,
            endTime = schedule.end_time,
            attendanceDate = attendanceDate,
            attendanceTime = attendanceTime
        )
    }

    @Serializable
    private data class CourseInfo(
        val course_name: String,
        val lecturer_id: String
    )

    @Serializable
    private data class InstructorInfo(
        val full_name: String
    )

    @Serializable
    private data class ScheduleInfo(
        val day: String,
        val start_time: String,
        val end_time: String,
        val room: String? = null
    )
}
