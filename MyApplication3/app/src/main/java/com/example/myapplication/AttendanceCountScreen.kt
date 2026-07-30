package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun AttendanceCountScreen() {
    var rollNo by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Attendance Count", fontSize = 22.sp)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = rollNo,
            onValueChange = { rollNo = it },
            label = { Text("Roll Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (rollNo.isNotEmpty()) {
                    loading = true
                    resultText = ""
                    scope.launch {
                        try {
                            val response = RetrofitClient.api.getAttendanceCount(rollNo)
                            resultText = "${response.student.name} (${response.student.rollNo})\nDepartment: ${response.student.department}\nYear: ${response.student.year}\nTotal Attendance: ${response.count}"
                        } catch (e: Exception) {
                            resultText = "Error: ${e.message}"
                        }
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Check Count")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (loading) {
            CircularProgressIndicator()
        } else if (resultText.isNotEmpty()) {
            Text(text = resultText, fontWeight = FontWeight.Medium, fontSize = 16.sp)
        }
    }
}
