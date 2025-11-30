package com.example.smartattendance.api

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.serialization.Serializable

class AttendanceApi(private val supabase: SupabaseClient) {

    // Data class for attendance record
    @Serializable
    data class AttendanceRecord(
        val attendance_id: Int? = null,
        val enrollment_id: Int,
        val schedule_id: Int,
        val attendance_date: String,
        val status: String,
        val is_verified: Boolean = false,
        val recorded_at: String? = null
    )

    @Serializable
    data class EnrollmentResult(val enrollment_id: Int)

    @Serializable
    data class AttendanceResult(val attendance_id: Int, val is_verified: Boolean)

    // Submit attendance - check if exists, if not create, if exists update is_verified
    suspend fun submitAttendance(
        userId: String,
        scheduleId: Int,
        courseId: Int,
        status: String = "present"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // First, get enrollment_id for this user and course
            val enrollmentResult = supabase.postgrest["enrollments"]
                .select(columns = Columns.list("enrollment_id")) {
                    filter {
                        eq("user_id", userId)
                        eq("course_id", courseId)
                    }
                }
                .decodeList<EnrollmentResult>()

            if (enrollmentResult.isEmpty()) {
                return@withContext Result.failure(Exception("User is not enrolled in this course"))
            }

            val enrollmentId = enrollmentResult.first().enrollment_id
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            // Check if attendance record already exists
            val existingAttendance = supabase.postgrest["attendances"]
                .select(columns = Columns.list("attendance_id", "is_verified")) {
                    filter {
                        eq("enrollment_id", enrollmentId)
                        eq("schedule_id", scheduleId)
                        eq("attendance_date", today)
                    }
                }
                .decodeList<AttendanceResult>()

            if (existingAttendance.isNotEmpty()) {
                // Update existing record to verified
                val attendanceId = existingAttendance.first().attendance_id
                supabase.postgrest["attendances"]
                    .update({
                        set("is_verified", true)
                    }) {
                        filter {
                            eq("attendance_id", attendanceId)
                        }
                    }
                return@withContext Result.success("Attendance verified successfully")
            } else {
                // Create new attendance record
                supabase.postgrest["attendances"]
                    .insert(AttendanceRecord(
                        enrollment_id = enrollmentId,
                        schedule_id = scheduleId,
                        attendance_date = today,
                        status = status,
                        is_verified = true
                    ))
                return@withContext Result.success("Attendance submitted successfully")
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    // Get attendance status for today
    suspend fun getTodayAttendance(
        userId: String,
        scheduleId: Int,
        courseId: Int
    ): Result<AttendanceRecord?> = withContext(Dispatchers.IO) {
        try {
            // Get enrollment_id
            val enrollmentResult = supabase.postgrest["enrollments"]
                .select(columns = Columns.list("enrollment_id")) {
                    filter {
                        eq("user_id", userId)
                        eq("course_id", courseId)
                    }
                }
                .decodeList<EnrollmentResult>()

            if (enrollmentResult.isEmpty()) {
                return@withContext Result.success(null)
            }

            val enrollmentId = enrollmentResult.first().enrollment_id
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            val attendance = supabase.postgrest["attendances"]
                .select() {
                    filter {
                        eq("enrollment_id", enrollmentId)
                        eq("schedule_id", scheduleId)
                        eq("attendance_date", today)
                    }
                }
                .decodeList<AttendanceRecord>()

            return@withContext Result.success(attendance.firstOrNull())
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
}
