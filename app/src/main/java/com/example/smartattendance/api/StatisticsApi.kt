package com.example.smartattendance.api

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
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

class StatisticsApi(private val supabase: SupabaseClient) {

    suspend fun getUserStatistics(userId: String): Result<OverallStatistic> {
        return try {
            Log.d("StatisticsApi", "Fetching statistics for user: $userId")

            // Get all enrollments for the user
            val enrollmentsResponse = supabase.postgrest["enrollments"]
                .select(Columns.list("enrollment_id"))
                .eq("user_id", userId)

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

            val enrollmentIds = enrollments.map { it.enrollment_id }

            // Get all attendance records for user's enrollments
            val attendanceResponse = supabase.postgrest["attendances"]
                .select(Columns.list("status"))
                .`in`("enrollment_id", enrollmentIds)

            val attendanceRecords = attendanceResponse.decodeList<AttendanceRecord>()

            // Calculate statistics
            val totalPresent = attendanceRecords.count { it.status == "present" }
            val totalLate = attendanceRecords.count { it.status == "late" }
            val totalAbsent = attendanceRecords.count { it.status == "absent" }
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
}

