package com.example.smartattendance.api

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleResponse(
    val schedule_id: Int,
    val course_id: Int,
    val day: String,
    val start_time: String,
    val end_time: String,
    val room: String? = null,
    val courses: CourseInfo? = null
)

@Serializable
data class CourseInfo(
    val course_code: String,
    val course_name: String
)

data class DaySchedule(
    val dayName: String,
    val schedules: List<ScheduleItem>
)

data class ScheduleItem(
    val scheduleId: Int,
    val courseId: Int,
    val title: String,
    val time: String,
    val room: String?
)

class ScheduleApi(private val supabase: SupabaseClient) {
    suspend fun getUserSchedules(userId: String): Result<List<DaySchedule>> {
        return try {


            // Get all enrollments for the user

            val enrollmentsResponse = supabase.postgrest["enrollments"]
                .select(Columns.list("course_id")) {
                    filter {
                        eq("user_id", userId)
                    }
                }


            val enrollments = enrollmentsResponse.decodeList<ScheduleEnrollmentRecord>()


            if (enrollments.isEmpty()) {

                return Result.success(createEmptySchedule())
            }

            val courseIds = enrollments.map { it.course_id }


            // Get schedules for enrolled courses

            val schedulesResponse = supabase.postgrest["schedules"]
                .select(
                    columns = Columns.list(
                        "schedule_id",
                        "course_id",
                        "day",
                        "start_time",
                        "end_time",
                        "room"
                    )
                ) {
                    filter {
                        isIn("course_id", courseIds)
                    }
                }


            val schedules = schedulesResponse.decodeList<ScheduleResponse>()


            if (schedules.isEmpty()) {

                return Result.success(createEmptySchedule())
            }

            // Get course information separately

            val coursesResponse = supabase.postgrest["courses"]
                .select(Columns.list("course_id", "course_code", "course_name")) {
                    filter {
                        isIn("course_id", courseIds)
                    }
                }


            val courses = coursesResponse.decodeList<CourseWithId>()
            val coursesMap = courses.associateBy { it.course_id }


            // Merge schedule with course info

            val schedulesWithCourseInfo = schedules.map { schedule ->
                val courseInfo = coursesMap[schedule.course_id]
                schedule.copy(
                    courses = if (courseInfo != null) {
                        CourseInfo(
                            course_code = courseInfo.course_code,
                            course_name = courseInfo.course_name
                        )
                    } else null
                )
            }

            // Group schedules by day

            val daySchedules = groupSchedulesByDay(schedulesWithCourseInfo)


            Result.success(daySchedules)

        } catch (e: Exception) {



            // Return empty schedule instead of failure to prevent loading forever
            Result.success(createEmptySchedule())
        }
    }

    private fun createEmptySchedule(): List<DaySchedule> {
        val daysOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        return daysOrder.map { day ->
            DaySchedule(
                dayName = day,
                schedules = emptyList()
            )
        }
    }

    private fun groupSchedulesByDay(schedules: List<ScheduleResponse>): List<DaySchedule> {
        val daysOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

        val groupedSchedules = schedules.groupBy { it.day }

        return daysOrder.mapNotNull { day ->
            val daySchedules = groupedSchedules[day]
            if (daySchedules != null && daySchedules.isNotEmpty()) {
                DaySchedule(
                    dayName = day,
                    schedules = daySchedules.map { schedule ->
                        ScheduleItem(
                            scheduleId = schedule.schedule_id,
                            courseId = schedule.course_id,
                            title = formatCourseTitle(schedule),
                            time = formatTime(schedule.start_time, schedule.end_time),
                            room = schedule.room
                        )
                    }.sortedBy { it.time }
                )
            } else {
                DaySchedule(
                    dayName = day,
                    schedules = emptyList()
                )
            }
        }
    }

    private fun formatCourseTitle(schedule: ScheduleResponse): String {
        val courseName = schedule.courses?.course_name ?: "Unknown Course"
        val courseCode = schedule.courses?.course_code ?: ""
        val room = if (schedule.room != null) " - ${schedule.room}" else ""
        return "$courseName - $courseCode$room"
    }

    private fun formatTime(startTime: String, endTime: String): String {
        // Format from HH:MM:SS to HH:MM AM/PM
        return try {
            val start = parseTime(startTime)
            val end = parseTime(endTime)
            "$start - $end"
        } catch (e: Exception) {
            "$startTime - $endTime"
        }
    }

    private fun parseTime(time: String): String {
        // Parse time like "07:15:00" to "7:15 AM"
        val parts = time.split(":")
        if (parts.size >= 2) {
            val hour = parts[0].toIntOrNull() ?: 0
            val minute = parts[1]
            val period = if (hour >= 12) "PM" else "AM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            return "$displayHour:$minute $period"
        }
        return time
    }

    suspend fun getTodaySchedules(userId: String): Result<List<ScheduleItem>> {
        return try {


            // Check if today is weekend
            if (isWeekend()) {

                return Result.success(emptyList())
            }

            // Get current day name
            val currentDay = getCurrentDayName()

            // Get all schedules
            val allSchedulesResult = getUserSchedules(userId)

            allSchedulesResult.fold(
                onSuccess = { daySchedules ->
                    val todaySchedule = daySchedules.find { it.dayName == currentDay }
                    Result.success(todaySchedule?.schedules ?: emptyList())
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    private fun getCurrentDayName(): String {
        val dayOfWeek = java.time.LocalDate.now().dayOfWeek
        return when (dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "Monday"
            java.time.DayOfWeek.TUESDAY -> "Tuesday"
            java.time.DayOfWeek.WEDNESDAY -> "Wednesday"
            java.time.DayOfWeek.THURSDAY -> "Thursday"
            java.time.DayOfWeek.FRIDAY -> "Friday"
            else -> "Monday" // Fallback (should not reach here if isWeekend() is checked first)
        }
    }

    private fun isWeekend(): Boolean {
        val dayOfWeek = java.time.LocalDate.now().dayOfWeek
        return dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY
    }
}

@Serializable
private data class ScheduleEnrollmentRecord(
    val course_id: Int
)

@Serializable
private data class CourseWithId(
    val course_id: Int,
    val course_code: String,
    val course_name: String
)
