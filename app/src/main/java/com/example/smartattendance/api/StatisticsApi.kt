package com.example.smartattendance.api

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.serialization.Serializable
import android.util.Log

@Serializable
data class CourseStatistic(
    val course_code: String,
    val course_name: String,
    val total_pertemuan: Int,
    val hadir: Int,
    val terlambat: Int,
    val tidak_hadir: Int,
    val persentase_kehadiran: Double
)

@Serializable
data class OverallStatistic(
    val total_present: Int,
    val total_late: Int,
    val total_absent: Int,
    val total_excused: Int = 0,
    val total_sick: Int = 0,
    val total_meetings: Int,
    val attendance_percentage: Double
)

@Serializable
data class AttendanceRecord(
    val status: String
)

@Serializable
data class EnrollmentRecord(
    val enrollment_id: Int
)

@Serializable
data class WeeklyAttendanceRecord(
    val attendance_date: String,
    val status: String
)

class StatisticsApi(private val supabase: SupabaseClient) {

    suspend fun getUserStatistics(userId: String): Result<OverallStatistic> {
        return try {
            Log.d("StatisticsApi", "Fetching statistics for user: $userId")

            // Get all enrollments for the user
            val enrollmentsResponse = supabase.postgrest["enrollments"]
                .select(Columns.list("enrollment_id")) {
                    filter {
                        eq("user_id", userId)
                    }
                }

            val enrollments = enrollmentsResponse.decodeList<EnrollmentRecord>()

            if (enrollments.isEmpty()) {
                Log.d("StatisticsApi", "No enrollments found for user")
                return Result.success(OverallStatistic(
                    total_present = 0,
                    total_late = 0,
                    total_absent = 0,
                    total_meetings = 0,
                    attendance_percentage = 0.0
                ))
            }

            val enrollmentIds = enrollments.map { enrollment -> enrollment.enrollment_id }

            // Get all attendance records for user's enrollments
            val attendanceResponse = supabase.postgrest["attendances"]
                .select(Columns.list("status")) {
                    filter {
                        isIn("enrollment_id", enrollmentIds)
                    }
                }

            val attendanceRecords = attendanceResponse.decodeList<AttendanceRecord>()

            // Calculate statistics
            val totalPresent = attendanceRecords.count { record -> record.status == "present" }
            val totalLate = attendanceRecords.count { record -> record.status == "late" }
            val totalAbsent = attendanceRecords.count { record -> record.status == "absent" }
            val totalMeetings = attendanceRecords.size
            val attendancePercentage = if (totalMeetings > 0) {
                (totalPresent * 100.0 / totalMeetings)
            } else 0.0

            val statistics = OverallStatistic(
                total_present = totalPresent,
                total_late = totalLate,
                total_absent = totalAbsent,
                total_meetings = totalMeetings,
                attendance_percentage = attendancePercentage
            )

            Log.d("StatisticsApi", "Statistics calculated: $statistics")
            Result.success(statistics)

        } catch (e: Exception) {
            Log.e("StatisticsApi", "Error fetching user statistics", e)
            // Return default values if query fails
            val defaultStats = OverallStatistic(
                total_present = 0,
                total_late = 0,
                total_absent = 0,
                total_meetings = 0,
                attendance_percentage = 0.0
            )
            Result.success(defaultStats)
        }
    }

    suspend fun getCourseStatistics(userId: String): Result<List<CourseStatistic>> {
        return try {
            Log.d("StatisticsApi", "Fetching course statistics for user: $userId")

            // This is a complex query that requires RPC function
            // For now, let's return empty list and implement later if needed
            Result.success(emptyList())

        } catch (e: Exception) {
            Log.e("StatisticsApi", "Error fetching course statistics", e)
            Result.failure(e)
        }
    }

    suspend fun getWeeklyStatus(userId: String): Result<Map<String, String?>> {
        return try {
            Log.d("StatisticsApi", "Fetching weekly status for user: $userId")

            // Get all enrollments for the user
            val enrollmentsResponse = supabase.postgrest["enrollments"]
                .select(Columns.list("enrollment_id")) {
                    filter {
                        eq("user_id", userId)
                    }
                }

            val enrollments = enrollmentsResponse.decodeList<EnrollmentRecord>()

            if (enrollments.isEmpty()) {
                Log.d("StatisticsApi", "No enrollments found for user")
                return Result.success(emptyMap())
            }

            val enrollmentIds = enrollments.map { enrollment -> enrollment.enrollment_id }

            // Calculate this week's date range (Monday to Friday)
            val today = java.time.LocalDate.now()
            val monday = today.with(java.time.DayOfWeek.MONDAY)
            val friday = today.with(java.time.DayOfWeek.FRIDAY)

            // Get attendance records for this week
            val attendanceResponse = supabase.postgrest["attendances"]
                .select(Columns.list("attendance_date", "status")) {
                    filter {
                        isIn("enrollment_id", enrollmentIds)
                        gte("attendance_date", monday.toString())
                        lte("attendance_date", friday.toString())
                    }
                }

            val attendanceRecords = attendanceResponse.decodeList<WeeklyAttendanceRecord>()

            // Map attendance records by day of week
            val weeklyStatus = mutableMapOf<String, String?>()
            weeklyStatus["monday"] = null
            weeklyStatus["tuesday"] = null
            weeklyStatus["wednesday"] = null
            weeklyStatus["thursday"] = null
            weeklyStatus["friday"] = null

            attendanceRecords.forEach { record ->
                val date = java.time.LocalDate.parse(record.attendance_date)
                val dayOfWeek = date.dayOfWeek

                when (dayOfWeek) {
                    java.time.DayOfWeek.MONDAY -> weeklyStatus["monday"] = record.status
                    java.time.DayOfWeek.TUESDAY -> weeklyStatus["tuesday"] = record.status
                    java.time.DayOfWeek.WEDNESDAY -> weeklyStatus["wednesday"] = record.status
                    java.time.DayOfWeek.THURSDAY -> weeklyStatus["thursday"] = record.status
                    java.time.DayOfWeek.FRIDAY -> weeklyStatus["friday"] = record.status
                    else -> {}
                }
            }

            Log.d("StatisticsApi", "Weekly status fetched: $weeklyStatus")
            Result.success(weeklyStatus)

        } catch (e: Exception) {
            Log.e("StatisticsApi", "Error fetching weekly status", e)
            // Return empty map on error
            Result.success(emptyMap())
        }
    }
}

