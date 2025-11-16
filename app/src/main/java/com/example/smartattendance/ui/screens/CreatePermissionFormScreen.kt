package com.example.smartattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.api.AuthApi
import com.example.smartattendance.api.ScheduleApi
import com.example.smartattendance.api.ScheduleItem
import com.example.smartattendance.api.PermissionApi
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePermissionFormScreen(
    user: AuthApi.User? = null,
    onBackClick: () -> Unit = {},
    onSubmit: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var nim by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("") }
    var startHour by remember { mutableStateOf("") }
    var startMinute by remember { mutableStateOf("") }
    var endHour by remember { mutableStateOf("") }
    var endMinute by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var showClassDropdown by remember { mutableStateOf(false) }
    var showDateDropdown by remember { mutableStateOf(false) }
    var showReasonDropdown by remember { mutableStateOf(false) }

    var schedules by remember { mutableStateOf<List<ScheduleItem>>(emptyList()) }
    var classes by remember { mutableStateOf<List<String>>(emptyList()) }
    var scheduleDataMap by remember { mutableStateOf<Map<String, Pair<ScheduleItem, String>>>(emptyMap()) }
    var availableDates by remember { mutableStateOf<List<String>>(emptyList()) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val reasons = listOf("Sick", "Family Emergency", "Medical Appointment", "Personal Matter", "Other")

    // Function to generate dates for a specific day of the week
    fun generateDatesForDay(dayName: String): List<String> {
        val calendar = java.util.Calendar.getInstance()
        val targetDayOfWeek = when (dayName) {
            "Monday" -> java.util.Calendar.MONDAY
            "Tuesday" -> java.util.Calendar.TUESDAY
            "Wednesday" -> java.util.Calendar.WEDNESDAY
            "Thursday" -> java.util.Calendar.THURSDAY
            "Friday" -> java.util.Calendar.FRIDAY
            else -> java.util.Calendar.MONDAY
        }

        val dates = mutableListOf<String>()
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

        // Find next occurrence of the target day
        var daysToAdd = (targetDayOfWeek - calendar.get(java.util.Calendar.DAY_OF_WEEK) + 7) % 7
        if (daysToAdd == 0) daysToAdd = 7 // If today is the target day, start from next week

        // Generate 4 future dates
        for (i in 0 until 4) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, daysToAdd)
            dates.add(dateFormat.format(calendar.time))
            daysToAdd = 7 // Next occurrence is 1 week later
        }

        return dates
    }

    // Set user data
    LaunchedEffect(user) {
        fullName = user?.full_name ?: ""
        nim = user?.nim ?: ""
    }

    // Fetch schedules
    LaunchedEffect(user?.user_id) {
        user?.user_id?.let { userId ->
            val scheduleApi = ScheduleApi(AuthApi.supabase)
            val result = scheduleApi.getUserSchedules(userId)
            if (result.isSuccess) {
                val daySchedules = result.getOrNull() ?: emptyList()
                // Create a map of class title to (ScheduleItem, day)
                val tempMap = mutableMapOf<String, Pair<ScheduleItem, String>>()
                daySchedules.forEach { daySchedule ->
                    daySchedule.schedules.forEach { scheduleItem ->
                        tempMap[scheduleItem.title] = Pair(scheduleItem, daySchedule.dayName)
                    }
                }
                scheduleDataMap = tempMap
                schedules = daySchedules.flatMap { it.schedules }
                classes = schedules.map { it.title }.distinct()
            }
        }
    }

    // Set times and dates when class is selected
    LaunchedEffect(selectedClass) {
        if (selectedClass.isNotEmpty()) {
            val scheduleData = scheduleDataMap[selectedClass]
            scheduleData?.let { (scheduleItem, dayName) ->
                // Set times
                val timeParts = scheduleItem.time.split(" - ")
                if (timeParts.size == 2) {
                    val startParts = timeParts[0].trim().split(":")
                    startHour = startParts.getOrNull(0) ?: ""
                    startMinute = startParts.getOrNull(1) ?: ""
                    val endParts = timeParts[1].trim().split(":")
                    endHour = endParts.getOrNull(0) ?: ""
                    endMinute = endParts.getOrNull(1) ?: ""
                }

                // Generate dates for the next 4 occurrences of this day
                availableDates = generateDatesForDay(dayName)
            }
        } else {
            availableDates = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        AppHeader(
            title = "Request Permission",
            headerType = HeaderType.BACK,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Request Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Request",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = AppFontFamily,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Name Field
                    Text(
                        text = "Name",
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontFamily = AppFontFamily,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { },
                        placeholder = { Text("Full Name", color = Color.Gray) },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2C2D32),
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    // NIM Field
                    Text(
                        text = "NIM",
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontFamily = AppFontFamily,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = nim,
                        onValueChange = { },
                        placeholder = { Text("NIM", color = Color.Gray) },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2C2D32),
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    // Class Field
                    Text(
                        text = "Class",
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontFamily = AppFontFamily,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box {
                        OutlinedTextField(
                            value = selectedClass,
                            onValueChange = { },
                            placeholder = { Text("Class", color = Color.Gray) },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showClassDropdown = true }
                                .padding(bottom = 16.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    modifier = Modifier.clickable { showClassDropdown = true }
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2C2D32),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        DropdownMenu(
                            expanded = showClassDropdown,
                            onDismissRequest = { showClassDropdown = false }
                        ) {
                            classes.forEach { className ->
                                DropdownMenuItem(
                                    text = { Text(className) },
                                    onClick = {
                                        selectedClass = className
                                        showClassDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Time Fields
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Start Time
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Start time",
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontFamily = AppFontFamily,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row {
                                OutlinedTextField(
                                    value = startHour,
                                    onValueChange = { },
                                    placeholder = { Text("HH", color = Color.Gray) },
                                    readOnly = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2C2D32),
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                                OutlinedTextField(
                                    value = startMinute,
                                    onValueChange = { },
                                    placeholder = { Text("MM", color = Color.Gray) },
                                    readOnly = true,
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2C2D32),
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // End Time
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "End time",
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontFamily = AppFontFamily,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row {
                                OutlinedTextField(
                                    value = endHour,
                                    onValueChange = { },
                                    placeholder = { Text("HH", color = Color.Gray) },
                                    readOnly = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2C2D32),
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                                OutlinedTextField(
                                    value = endMinute,
                                    onValueChange = { },
                                    placeholder = { Text("MM", color = Color.Gray) },
                                    readOnly = true,
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2C2D32),
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                            }
                        }
                    }

                    // Date and Reason Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Date
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Date",
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontFamily = AppFontFamily,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Box {
                                OutlinedTextField(
                                    value = selectedDate,
                                    onValueChange = { },
                                    placeholder = { Text("DD/MM/YYYY", color = Color.Gray) },
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDateDropdown = true }
                                        .padding(end = 8.dp),
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown",
                                            modifier = Modifier.clickable {
                                                showDateDropdown = true
                                            }
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2C2D32),
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                                DropdownMenu(
                                    expanded = showDateDropdown,
                                    onDismissRequest = { showDateDropdown = false }
                                ) {
                                    if (availableDates.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Please select a class first", color = Color.Gray) },
                                            onClick = { }
                                        )
                                    } else {
                                        availableDates.forEach { date ->
                                            DropdownMenuItem(
                                                text = { Text(date) },
                                                onClick = {
                                                    selectedDate = date
                                                    showDateDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        // Reason
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reason for absence",
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontFamily = AppFontFamily,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Box {
                                OutlinedTextField(
                                    value = selectedReason,
                                    onValueChange = { },
                                    placeholder = { Text("Reason", color = Color.Gray) },
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showReasonDropdown = true },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown",
                                            modifier = Modifier.clickable { showReasonDropdown = true }
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2C2D32),
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                                DropdownMenu(
                                    expanded = showReasonDropdown,
                                    onDismissRequest = { showReasonDropdown = false }
                                ) {
                                    reasons.forEach { reason ->
                                        DropdownMenuItem(
                                            text = { Text(reason) },
                                            onClick = {
                                                selectedReason = reason
                                                showReasonDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Description Field
                    Text(
                        text = "Description",
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontFamily = AppFontFamily,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Explain your reason", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(bottom = 16.dp),
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2C2D32),
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    // Evidence Field
                    Text(
                        text = "Evidence",
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontFamily = AppFontFamily,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clickable { /* Handle file upload */ },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attach file",
                                tint = Color.Gray,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Include any supporting files",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error message
                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontFamily = AppFontFamily,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Submit Button
                    Button(
                        onClick = {
                            // Validate fields
                            when {
                                selectedClass.isEmpty() -> {
                                    errorMessage = "Please select a class"
                                }
                                selectedDate.isEmpty() -> {
                                    errorMessage = "Please select a date"
                                }
                                selectedReason.isEmpty() -> {
                                    errorMessage = "Please select a reason"
                                }
                                description.isEmpty() -> {
                                    errorMessage = "Please provide a description"
                                }
                                else -> {
                                    // All fields valid, submit
                                    errorMessage = null
                                    isSubmitting = true

                                    coroutineScope.launch {
                                        try {
                                            val permissionApi = PermissionApi(AuthApi.supabase)
                                            val startTimeFormatted = "$startHour:$startMinute"
                                            val endTimeFormatted = "$endHour:$endMinute"

                                            val result = permissionApi.createPermission(
                                                userId = user?.user_id ?: "",
                                                courseName = selectedClass,
                                                permissionDate = selectedDate,
                                                startTime = startTimeFormatted,
                                                endTime = endTimeFormatted,
                                                reason = selectedReason,
                                                description = description,
                                                schedules = scheduleDataMap
                                            )

                                            isSubmitting = false

                                            if (result.isSuccess) {
                                                // Success - navigate back
                                                onSubmit()
                                            } else {
                                                errorMessage = result.exceptionOrNull()?.message
                                                    ?: "Failed to submit permission request"
                                            }
                                        } catch (e: Exception) {
                                            isSubmitting = false
                                            errorMessage = e.message ?: "An error occurred"
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE57373)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Submit",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontFamily = AppFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePermissionFormScreenPreview() {
    CreatePermissionFormScreen()
}

