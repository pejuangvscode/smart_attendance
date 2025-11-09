package com.example.smartattendance.api

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class AttendanceHistoryItem(
    @SerialName("attendance_id") val attendanceId: Int,
    @SerialName("enrollment_id") val enrollmentId: Int,
    @SerialName("schedule_id") val scheduleId: Int,
    @SerialName("attendance_date") val attendanceDate: String,
    @SerialName("status") val status: String,
    @SerialName("recorded_at") val recordedAt: String
)

@Serializable
data class EnrollmentData(
    @SerialName("enrollment_id") val enrollmentId: Int,
    @SerialName("user_id") val userId: String,
    @SerialName("course_id") val courseId: Int
)

@Serializable
data class ScheduleData(
    @SerialName("schedule_id") val scheduleId: Int,
    @SerialName("course_id") val courseId: Int,
    @SerialName("day") val day: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("room") val room: String?
)

@Serializable
data class CourseData(
    @SerialName("course_id") val courseId: Int,
    @SerialName("course_code") val courseCode: String,
    @SerialName("course_name") val courseName: String,
    @SerialName("lecturer_id") val lecturerId: String
)

data class HistoryGroupedItem(
    val date: String,
    val items: List<HistoryItemDetail>
)

data class HistoryItemDetail(
    val title: String,
    val subtitle: String,
    val status: String
)

object HistoryApi {
    private const val TAG = "HistoryApi"

    suspend fun getAttendanceHistory(
        supabase: SupabaseClient,
        userId: String
    ): Result<List<HistoryGroupedItem>> {
        return try {
            Log.d(TAG, "Fetching attendance history for user: $userId")

            // 1. Get all enrollments for this user
            val enrollmentsResponse = supabase.postgrest["enrollments"]
                .select() {
                    filter {
                        eq("user_id", userId)
                    }
                }

            val enrollments = enrollmentsResponse.decodeList<EnrollmentData>()

            if (enrollments.isEmpty()) {
                Log.d(TAG, "No enrollments found for user")
                return Result.success(emptyList())
            }

            val enrollmentIds = enrollments.map { it.enrollmentId }
            Log.d(TAG, "Found ${enrollmentIds.size} enrollments")

            // 2. Get all attendances - fetch all and filter client-side
            val allAttendancesResponse = supabase.postgrest["attendances"]
                .select()

            val allAttendances = allAttendancesResponse.decodeList<AttendanceHistoryItem>()

            // Filter for user's enrollments
            val attendances = allAttendances.filter { it.enrollmentId in enrollmentIds }
            Log.d(TAG, "Found ${attendances.size} attendance records for user")

            if (attendances.isEmpty()) {
                Log.d(TAG, "No attendance records found")
                return Result.success(emptyList())
            }

            // 3. Get all schedules
            val scheduleIds = attendances.map { it.scheduleId }.distinct()
            val allSchedulesResponse = supabase.postgrest["schedules"]
                .select()

            val allSchedules = allSchedulesResponse.decodeList<ScheduleData>()
            val schedules = allSchedules.filter { it.scheduleId in scheduleIds }

            // 4. Get all courses
            val courseIds = schedules.map { it.courseId }.distinct()
            val allCoursesResponse = supabase.postgrest["courses"]
                .select()

            val allCourses = allCoursesResponse.decodeList<CourseData>()
            val courses = allCourses.filter { it.courseId in courseIds }

            // 5. Create maps for quick lookup
            val scheduleMap = schedules.associateBy { it.scheduleId }
            val courseMap = courses.associateBy { it.courseId }

            // 6. Build history items
            val historyItems = attendances.mapNotNull { attendance ->
                val schedule = scheduleMap[attendance.scheduleId]
                val course = schedule?.let { courseMap[it.courseId] }

                if (course != null && schedule != null) {
                    HistoryItemDetail(
                        title = course.courseName,
                        subtitle = formatTime(schedule.startTime, schedule.endTime),
                        status = formatStatus(attendance.status)
                    ) to attendance.attendanceDate
                } else {
                    null
                }
            }

            // 7. Group by date and sort
            val grouped = historyItems
                .groupBy { it.second }
                .map { (date, items) ->
                    HistoryGroupedItem(
                        date = formatDateHeader(date),
                        items = items.map { it.first }
                    )
                }
                .sortedByDescending { parseDate(it.date) }

            Log.d(TAG, "Successfully grouped into ${grouped.size} date groups")
            Result.success(grouped)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching attendance history", e)
            Result.failure(e)
        }
    }

    private fun formatTime(startTime: String, endTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("h:mma", Locale.getDefault())

            val start = inputFormat.parse(startTime)
            val end = inputFormat.parse(endTime)

            "${outputFormat.format(start!!)} - ${outputFormat.format(end!!)}"
        } catch (e: Exception) {
            "$startTime - $endTime"
        }
    }

    private fun formatStatus(status: String): String {
        return when (status.lowercase()) {
            "present" -> "Present"
            "late" -> "Late"
            "absent" -> "Absent"
            else -> status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    private fun formatDateHeader(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateStr) ?: return dateStr

            val calendar = Calendar.getInstance()
            calendar.time = date

            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }

            val outputFormat = SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault())
            val formattedDate = outputFormat.format(date)

            when {
                isSameDay(calendar, today) -> "Today - $formattedDate"
                isSameDay(calendar, yesterday) -> "Yesterday - $formattedDate"
                else -> formattedDate
            }
        } catch (_: Exception) {
            dateStr
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun parseDate(dateHeader: String): Date {
        return try {
            // Remove "Today - " or "Yesterday - " prefix
            val cleanDate = dateHeader
                .replace("Today - ", "")
                .replace("Yesterday - ", "")

            val format = SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault())
            format.parse(cleanDate) ?: Date()
        } catch (_: Exception) {
            Date()
        }
    }
}

