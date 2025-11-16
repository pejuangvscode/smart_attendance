package com.example.smartattendance.api

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class PermissionRequest(
    val enrollment_id: Int,
    val permission_date: String,
    val start_time: String,
    val end_time: String,
    val reason: String,
    val description: String?,
    val evidence: String? = null,
    val status: String = "pending"
)

class PermissionApi(private val supabase: SupabaseClient) {

    suspend fun createPermission(
        userId: String,
        courseName: String,
        permissionDate: String,
        startTime: String,
        endTime: String,
        reason: String,
        description: String,
        schedules: Map<String, Pair<ScheduleItem, String>>
    ): Result<Unit> {
        return try {
            // Get the schedule info for the selected course
            val scheduleData = schedules[courseName] ?: return Result.failure(
                Exception("Schedule not found for course: $courseName")
            )
            val (scheduleItem, _) = scheduleData
            val courseId = scheduleItem.courseId

            // Get enrollment_id for this user and course
            val enrollmentResponse = supabase.postgrest["enrollments"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("course_id", courseId)
                    }
                }

            val enrollments = enrollmentResponse.decodeList<PermissionEnrollmentRecord>()
            if (enrollments.isEmpty()) {
                return Result.failure(Exception("Enrollment not found for this course"))
            }

            val enrollmentId = enrollments[0].enrollment_id

            // Convert date from DD/MM/YYYY to YYYY-MM-DD
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = dateFormat.parse(permissionDate)
            val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dbDateFormat.format(date ?: Date())

            // Format time to HH:MM:SS
            val formattedStartTime = String.format("%02d:%02d:00",
                startTime.split(":")[0].toIntOrNull() ?: 0,
                startTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0
            )
            val formattedEndTime = String.format("%02d:%02d:00",
                endTime.split(":")[0].toIntOrNull() ?: 0,
                endTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0
            )

            // Create permission request
            val permissionRequest = PermissionRequest(
                enrollment_id = enrollmentId,
                permission_date = formattedDate,
                start_time = formattedStartTime,
                end_time = formattedEndTime,
                reason = reason,
                description = description.ifEmpty { null }
            )

            // Insert into database
            supabase.postgrest["permissions"].insert(permissionRequest)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

@Serializable
data class PermissionEnrollmentRecord(
    val enrollment_id: Int,
    val user_id: String,
    val course_id: Int
)

