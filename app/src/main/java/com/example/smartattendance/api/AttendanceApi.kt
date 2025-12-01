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
        val from_camera: Boolean = false,
        val recorded_at: String? = null
    )

    @Serializable
    data class EnrollmentResult(val enrollment_id: Int)

    @Serializable
    data class CourseData(
        val course_name: String
    )

    // Submit attendance - check if exists, if not create, if exists update is_verified
    suspend fun submitAttendance(
        userId: String,
        scheduleId: Int,
        courseId: Int
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
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
                return@withContext Result.failure(Exception("User is not enrolled in this course"))
            }
            val enrollmentId = enrollmentResult.first().enrollment_id
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            // Check if attendance record already exists
            val existingAttendance = supabase.postgrest["attendances"]
                .select(columns = Columns.list("attendance_id", "is_verified", "from_camera", "recorded_at", "status")) {
                    filter {
                        eq("enrollment_id", enrollmentId)
                        eq("schedule_id", scheduleId)
                        eq("attendance_date", today)
                    }
                }
                .decodeList<AttendanceResultWithCameraTime>()
            if (existingAttendance.isEmpty()) {
                // Aturan 1: Insert new attendance row, status 'pending'
                supabase.postgrest["attendances"]
                    .insert(AttendanceRecord(
                        enrollment_id = enrollmentId,
                        schedule_id = scheduleId,
                        attendance_date = today,
                        status = "pending"
                    ))
                return@withContext Result.success(Pair("Attendance submitted, waiting for verification", "pending"))
            } else {
                val attendance = existingAttendance.first()
                // Aturan 4: Jika status bukan pending dan is_verified true, langsung redirect
                if (attendance.status != "pending" && attendance.is_verified) {
                    return@withContext Result.success(Pair("Attendance already verified", attendance.status))
                }
                // Aturan 2: Jika is_verified false dan from_camera true
                if (!attendance.is_verified && attendance.from_camera) {
                    // Get schedule start time
                    val scheduleResult = supabase.postgrest["schedules"]
                        .select(columns = Columns.list("start_time")) {
                            filter { eq("schedule_id", scheduleId) }
                        }
                        .decodeList<ScheduleStartTime>()
                    val scheduleStart = scheduleResult.firstOrNull()?.start_time ?: "07:15"
                    // Compare recorded_at time with schedule start time + 15 min
                    val attendanceTime = attendance.recorded_at?.substring(11,16) ?: scheduleStart
                    val startHour = scheduleStart.substring(0,2).toInt()
                    val startMin = scheduleStart.substring(3,5).toInt()
                    val toleranceHour = attendanceTime.substring(0,2).toInt()
                    val toleranceMin = attendanceTime.substring(3,5).toInt()
                    val startTotalMin = startHour * 60 + startMin
                    val attendTotalMin = toleranceHour * 60 + toleranceMin
                    val statusResult = if (attendTotalMin <= startTotalMin + 15) "present" else "late"
                    // Update is_verified and status
                    supabase.postgrest["attendances"]
                        .update({
                            set("is_verified", true)
                            set("status", statusResult)
                        }) {
                            filter { eq("attendance_id", attendance.attendance_id) }
                        }
                    return@withContext Result.success(Pair("Attendance verified successfully", statusResult))
                }
                // Aturan 3: Jika is_verified false dan from_camera false
                // This block is only reached if above conditions are not met
                return@withContext Result.success(Pair("Attendance submitted, waiting for verification", "pending"))
            }
        } catch (_: Exception) {
            return@withContext Result.failure(Exception("Attendance submission failed"))
        }
    }

    @Serializable
    data class AttendanceResultWithCameraTime(
        val attendance_id: Int,
        val is_verified: Boolean,
        val from_camera: Boolean,
        val recorded_at: String?,
        val status: String
    )
    @Serializable
    data class ScheduleStartTime(
        val start_time: String
    )

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

    suspend fun getCourseName(courseId: Int): String? = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["courses"]
                .select(columns = Columns.list("course_name")) {
                    filter { eq("course_id", courseId) }
                }
                .decodeList<CourseData>()
            return@withContext result.firstOrNull()?.course_name
        } catch (e: Exception) {
            return@withContext null
        }
    }

    suspend fun getScheduleInfo(scheduleId: Int): ScheduleInfo? = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["schedules"]
                .select(columns = Columns.list("day", "start_time", "end_time", "room")) {
                    filter { eq("schedule_id", scheduleId) }
                }
                .decodeList<ScheduleInfo>()
            return@withContext result.firstOrNull()
        } catch (e: Exception) {
            return@withContext null
        }
    }

    @Serializable
    data class ScheduleInfo(
        val day: String,
        val start_time: String,
        val end_time: String,
        val room: String?
    )
}
