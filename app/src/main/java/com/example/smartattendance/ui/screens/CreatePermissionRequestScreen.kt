package com.example.smartattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.ui.components.AppBottomNavigation
import com.example.smartattendance.ui.theme.SmartAttendanceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePermissionRequestScreen(
    onBackClick: () -> Unit = {},
    onSubmit: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val darkGray = Color(0xFF3A3A3A)

    // Form state
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

    // Dropdown states
    var classDropdownExpanded by remember { mutableStateOf(false) }
    var dateDropdownExpanded by remember { mutableStateOf(false) }
    var reasonDropdownExpanded by remember { mutableStateOf(false) }

    val classList = listOf("Class A", "Class B", "Class C", "Class D")
    val reasonList = listOf("Sick", "Family Emergency", "Personal", "Other")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top bar with dark gray background
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = darkGray
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Request Permission",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        // Form content with white background
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Main form card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(4.dp, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Request title
                    Text(
                        text = "Request",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Name field
                    Text(
                        text = "Name",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = { Text("Full Name", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = darkGray,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    // NIM field
                    Text(
                        text = "NIM",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = nim,
                        onValueChange = { nim = it },
                        placeholder = { Text("NIM", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = darkGray,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    // Class dropdown
                    Text(
                        text = "Class",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = classDropdownExpanded,
                        onExpandedChange = { classDropdownExpanded = !classDropdownExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedClass,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Class", color = Color.Gray) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = darkGray,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = classDropdownExpanded,
                            onDismissRequest = { classDropdownExpanded = false }
                        ) {
                            classList.forEach { className ->
                                DropdownMenuItem(
                                    text = { Text(className) },
                                    onClick = {
                                        selectedClass = className
                                        classDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Time fields row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Start time
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Start time",
                                fontSize = 14.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = startHour,
                                    onValueChange = { startHour = it },
                                    placeholder = { Text("Hour", fontSize = 12.sp, color = Color.Gray) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = darkGray,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                                OutlinedTextField(
                                    value = startMinute,
                                    onValueChange = { startMinute = it },
                                    placeholder = { Text("Minute", fontSize = 12.sp, color = Color.Gray) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = darkGray,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // End time
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "End time",
                                fontSize = 14.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = endHour,
                                    onValueChange = { endHour = it },
                                    placeholder = { Text("Hour", fontSize = 12.sp, color = Color.Gray) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = darkGray,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                                OutlinedTextField(
                                    value = endMinute,
                                    onValueChange = { endMinute = it },
                                    placeholder = { Text("Minute", fontSize = 12.sp, color = Color.Gray) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = darkGray,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                            }
                        }
                    }

                    // Date and Reason row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Date dropdown
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Date",
                                fontSize = 14.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ExposedDropdownMenuBox(
                                expanded = dateDropdownExpanded,
                                onExpandedChange = { dateDropdownExpanded = !dateDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedDate,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("DD/MM/YYYY", fontSize = 12.sp, color = Color.Gray) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown"
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = darkGray,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = dateDropdownExpanded,
                                    onDismissRequest = { dateDropdownExpanded = false }
                                ) {
                                    listOf("29/09/2025", "30/09/2025", "01/10/2025").forEach { date ->
                                        DropdownMenuItem(
                                            text = { Text(date) },
                                            onClick = {
                                                selectedDate = date
                                                dateDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Reason dropdown
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reason for absence",
                                fontSize = 14.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ExposedDropdownMenuBox(
                                expanded = reasonDropdownExpanded,
                                onExpandedChange = { reasonDropdownExpanded = !reasonDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedReason,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("Reason", fontSize = 12.sp, color = Color.Gray) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown"
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = darkGray,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = reasonDropdownExpanded,
                                    onDismissRequest = { reasonDropdownExpanded = false }
                                ) {
                                    reasonList.forEach { reason ->
                                        DropdownMenuItem(
                                            text = { Text(reason) },
                                            onClick = {
                                                selectedReason = reason
                                                reasonDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Description field
                    Text(
                        text = "Description",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Explain your reason", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(bottom = 16.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = darkGray,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    // Evidence section
                    Text(
                        text = "Evidence",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .border(
                                1.dp,
                                Color.LightGray,
                                RoundedCornerShape(4.dp)
                            )
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
                            .clickable { /* Handle file upload */ },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attach file",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Include any supporting files",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit button
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE57373)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Submit",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom navigation
        }

        // Bottom Navigation
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            AppBottomNavigation(
                currentRoute = "schedule",
                onNavigate = onNavigate
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePermissionRequestScreenPreview() {
    SmartAttendanceTheme {
        CreatePermissionRequestScreen()
    }
}
