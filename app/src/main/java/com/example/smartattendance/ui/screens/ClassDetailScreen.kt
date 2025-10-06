package com.example.smartattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.ui.components.AppHeader
import com.example.smartattendance.ui.components.HeaderType
import com.example.smartattendance.ui.theme.AppFontFamily
import java.text.SimpleDateFormat
import java.util.*

data class DetailedScheduleItem(
    val courseCode: String,
    val courseName: String,
    val time: String,
    val room: String,
    val instructor: String,
    val sks: Int,
    val numberOfPresence: String,
    val attendanceHistory: List<AttendanceRecord>
)

data class AttendanceRecord(
    val date: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    dayName: String,
    courseCode: String,
    onBackClick: () -> Unit = {}
) {
    // Get class details based on courseCode
    val classDetails = getClassDetails(courseCode)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        AppHeader(
            title = "Schedule",
            headerType = HeaderType.BACK,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Warning Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Warning!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        fontFamily = AppFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    Text(
                        text = "The tolerance limit has been reached. If you miss class or are late again, you will not be able to take the final exam.",
                        fontSize = 12.sp,
                        color = Color.Black,
                        fontFamily = AppFontFamily,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }

            // Detail Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Detail",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = AppFontFamily,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Course
                    DetailRow("Course", classDetails.courseName)

                    // Dosen
                    DetailRow("Dosen", classDetails.instructor)

                    // SKS
                    DetailRow("SKS", classDetails.sks.toString())

                    // Number of attendees (presence)
                    DetailRow("Number of attendees", classDetails.numberOfPresence)

                    // Room
                    DetailRow("Room", classDetails.room, isLast = true)
                }
            }

            // Attendance Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Attendance status",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = AppFontFamily,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Attendance records
                    classDetails.attendanceHistory.forEach { record ->
                        AttendanceStatusRow(
                            date = record.date,
                            status = record.status
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black,
            fontFamily = AppFontFamily,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black,
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun AttendanceStatusRow(
    date: String,
    status: String
) {
    val statusColor = when (status.uppercase()) {
        "PRESENT" -> Color(0xFF4CAF50)
        "ABSENT" -> Color(0xFFE53E3E)
        "LATE" -> Color(0xFFFF9800)
        else -> Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            fontSize = 12.sp,
            color = Color.Black,
            fontFamily = AppFontFamily
        )
        Text(
            text = status.uppercase(),
            fontSize = 12.sp,
            color = statusColor,
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}

// Function to get class details based on course code
private fun getClassDetails(courseCode: String): DetailedScheduleItem {
    return when (courseCode) {
        "INF 20052 - KKKR" -> DetailedScheduleItem(
            courseCode = "INF 20052 - KKKR",
            courseName = "KECERDASAN KOMPUTASIONAL",
            time = "8:45AM - 11:25AM",
            room = "B339",
            instructor = "ADITYA RAMA MITRA, SAMUEL LUKAS",
            sks = 4,
            numberOfPresence = "5/16",
            attendanceHistory = listOf(
                AttendanceRecord("06/10/2025", "PRESENT"),
                AttendanceRecord("30/09/2025", "ABSENT"),
                AttendanceRecord("23/09/2025", "PRESENT"),
                AttendanceRecord("16/09/2025", "PRESENT"),
                AttendanceRecord("09/09/2025", "LATE"),
                AttendanceRecord("02/09/2025", "PRESENT"),
                AttendanceRecord("26/08/2025", "ABSENT"),
                AttendanceRecord("19/08/2025", "PRESENT")
            )
        )
        "INF 20052 - KKLR" -> DetailedScheduleItem(
            courseCode = "INF 20052 - KKLR",
            courseName = "KECERDASAN KOMPUTASIONAL Laboratory",
            time = "1:15PM - 2:55PM",
            room = "B340",
            instructor = "KELVIN WIRIYATAMA",
            sks = 1,
            numberOfPresence = "6/16",
            attendanceHistory = listOf(
                AttendanceRecord("06/10/2025", "PRESENT"),
                AttendanceRecord("30/09/2025", "PRESENT"),
                AttendanceRecord("23/09/2025", "ABSENT"),
                AttendanceRecord("16/09/2025", "PRESENT"),
                AttendanceRecord("09/09/2025", "PRESENT"),
                AttendanceRecord("02/09/2025", "LATE"),
                AttendanceRecord("26/08/2025", "PRESENT"),
                AttendanceRecord("19/08/2025", "PRESENT")
            )
        )
        "INF 20151 - PMLR" -> DetailedScheduleItem(
            courseCode = "INF 20151 - PMLR",
            courseName = "PEMBELAJARAN MESIN LANJUT",
            time = "7:15AM - 9:45AM",
            room = "B338",
            instructor = "FELIKS VICTOR PARNINGOTAN S.",
            sks = 3,
            numberOfPresence = "7/16",
            attendanceHistory = listOf(
                AttendanceRecord("01/10/2025", "PRESENT"),
                AttendanceRecord("24/09/2025", "PRESENT"),
                AttendanceRecord("17/09/2025", "PRESENT"),
                AttendanceRecord("10/09/2025", "ABSENT"),
                AttendanceRecord("03/09/2025", "PRESENT"),
                AttendanceRecord("27/08/2025", "PRESENT"),
                AttendanceRecord("20/08/2025", "LATE"),
                AttendanceRecord("13/08/2025", "PRESENT")
            )
        )
        "INF 20054 - PAPR" -> DetailedScheduleItem(
            courseCode = "INF 20054 - PAPR",
            courseName = "PENGEMBANGAN APLIKASI PLATFORM MOBILE",
            time = "10:15AM - 11:55AM",
            room = "B339",
            instructor = "DAVID HABSARA HAREVA",
            sks = 2,
            numberOfPresence = "8/16",
            attendanceHistory = listOf(
                AttendanceRecord("01/10/2025", "PRESENT"),
                AttendanceRecord("24/09/2025", "PRESENT"),
                AttendanceRecord("17/09/2025", "PRESENT"),
                AttendanceRecord("10/09/2025", "PRESENT"),
                AttendanceRecord("03/09/2025", "ABSENT"),
                AttendanceRecord("27/08/2025", "PRESENT"),
                AttendanceRecord("20/08/2025", "PRESENT"),
                AttendanceRecord("13/08/2025", "PRESENT")
            )
        )
        "INF 20054 - PALR" -> DetailedScheduleItem(
            courseCode = "INF 20054 - PALR",
            courseName = "PENGEMBANGAN APLIKASI PLATFORM MOBILE Laboratory",
            time = "1:15PM - 2:55PM",
            room = "B339",
            instructor = "DAVID HABSARA HAREVA",
            sks = 1,
            numberOfPresence = "9/16",
            attendanceHistory = listOf(
                AttendanceRecord("01/10/2025", "PRESENT"),
                AttendanceRecord("24/09/2025", "PRESENT"),
                AttendanceRecord("17/09/2025", "PRESENT"),
                AttendanceRecord("10/09/2025", "PRESENT"),
                AttendanceRecord("03/09/2025", "PRESENT"),
                AttendanceRecord("27/08/2025", "ABSENT"),
                AttendanceRecord("20/08/2025", "PRESENT"),
                AttendanceRecord("13/08/2025", "PRESENT")
            )
        )
        "INF 20262 - IKSR" -> DetailedScheduleItem(
            courseCode = "INF 20262 - IKSR",
            courseName = "INFORMATIKA DALAM KOM SELULER",
            time = "1:15PM - 3:45PM",
            room = "B338",
            instructor = "BENNY HARDIONO, LOUIS KHRISNA PUTERA SURYAPRANATA",
            sks = 3,
            numberOfPresence = "6/16",
            attendanceHistory = listOf(
                AttendanceRecord("02/10/2025", "ABSENT"),
                AttendanceRecord("25/09/2025", "LATE"),
                AttendanceRecord("18/09/2025", "PRESENT"),
                AttendanceRecord("11/09/2025", "PRESENT"),
                AttendanceRecord("04/09/2025", "PRESENT"),
                AttendanceRecord("28/08/2025", "PRESENT"),
                AttendanceRecord("21/08/2025", "ABSENT"),
                AttendanceRecord("14/08/2025", "PRESENT")
            )
        )
        "INF 20053 - PWR" -> DetailedScheduleItem(
            courseCode = "INF 20053 - PWR",
            courseName = "PERANCANGAN & PEMROGRAMAN WEB",
            time = "7:15AM - 9:45AM",
            room = "B342",
            instructor = "MARTA DIANA",
            sks = 2,
            numberOfPresence = "7/16",
            attendanceHistory = listOf(
                AttendanceRecord("03/10/2025", "PRESENT"),
                AttendanceRecord("26/09/2025", "PRESENT"),
                AttendanceRecord("19/09/2025", "ABSENT"),
                AttendanceRecord("12/09/2025", "PRESENT"),
                AttendanceRecord("05/09/2025", "PRESENT"),
                AttendanceRecord("29/08/2025", "LATE"),
                AttendanceRecord("22/08/2025", "PRESENT"),
                AttendanceRecord("15/08/2025", "PRESENT")
            )
        )
        "INF 20053 - PPLR" -> DetailedScheduleItem(
            courseCode = "INF 20053 - PPLR",
            courseName = "PERANCANGAN & PEMROGRAMAN WEB Laboratory",
            time = "10:00AM - 11:40AM",
            room = "B341",
            instructor = "KELVIN WIRIYATAMA",
            sks = 1,
            numberOfPresence = "8/16",
            attendanceHistory = listOf(
                AttendanceRecord("03/10/2025", "PRESENT"),
                AttendanceRecord("26/09/2025", "PRESENT"),
                AttendanceRecord("19/09/2025", "PRESENT"),
                AttendanceRecord("12/09/2025", "ABSENT"),
                AttendanceRecord("05/09/2025", "PRESENT"),
                AttendanceRecord("29/08/2025", "PRESENT"),
                AttendanceRecord("22/08/2025", "PRESENT"),
                AttendanceRecord("15/08/2025", "PRESENT")
            )
        )
        "INF 20051 - KKJR" -> DetailedScheduleItem(
            courseCode = "INF 20051 - KKJR",
            courseName = "KEAMANAN KOMPUTER & JARINGAN",
            time = "7:15AM - 9:45AM",
            room = "B342",
            instructor = "PUJIANTO YUGOPUSPITO",
            sks = 3,
            numberOfPresence = "5/16",
            attendanceHistory = listOf(
                AttendanceRecord("04/10/2025", "ABSENT"),
                AttendanceRecord("27/09/2025", "ABSENT"),
                AttendanceRecord("20/09/2025", "PRESENT"),
                AttendanceRecord("13/09/2025", "PRESENT"),
                AttendanceRecord("06/09/2025", "LATE"),
                AttendanceRecord("30/08/2025", "PRESENT"),
                AttendanceRecord("23/08/2025", "ABSENT"),
                AttendanceRecord("16/08/2025", "PRESENT")
            )
        )
        "INF 20051 - KKLR" -> DetailedScheduleItem(
            courseCode = "INF 20051 - KKLR",
            courseName = "KEAMANAN KOMPUTER & JARINGAN Laboratory",
            time = "10:00AM - 11:40AM",
            room = "B342",
            instructor = "PUJIANTO YUGOPUSPITO",
            sks = 1,
            numberOfPresence = "6/16",
            attendanceHistory = listOf(
                AttendanceRecord("04/10/2025", "PRESENT"),
                AttendanceRecord("27/09/2025", "ABSENT"),
                AttendanceRecord("20/09/2025", "PRESENT"),
                AttendanceRecord("13/09/2025", "PRESENT"),
                AttendanceRecord("06/09/2025", "PRESENT"),
                AttendanceRecord("30/08/2025", "LATE"),
                AttendanceRecord("23/08/2025", "PRESENT"),
                AttendanceRecord("16/08/2025", "ABSENT")
            )
        )
        else -> DetailedScheduleItem(
            courseCode = courseCode,
            courseName = "Unknown Course",
            time = "Unknown",
            room = "Unknown",
            instructor = "Unknown",
            sks = 0,
            numberOfPresence = "0/16",
            attendanceHistory = emptyList()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ClassDetailScreenPreview() {
    ClassDetailScreen(
        dayName = "Monday",
        courseCode = "INF 20052 - KKKR"
    )
}
