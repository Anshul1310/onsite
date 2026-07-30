package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.myapplication.network.MarkRequest
import com.example.myapplication.network.RetrofitClient
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

@Composable
fun ScannerScreen() {
    val context = LocalContext.current
    var scannedToken by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("No QR scanned") }
    val scope = rememberCoroutineScope()

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents != null) {
            scannedToken = result.contents
            statusText = "Token: ${result.contents}"

            if (studentId.isNotEmpty()) {
                scope.launch {
                    try {
                        val response = RetrofitClient.api.markAttendance(
                            MarkRequest(studentId, result.contents)
                        )
                        if (response.isSuccessful) {
                            statusText = response.body()?.message ?: "Attendance Marked"
                            Toast.makeText(context, statusText, Toast.LENGTH_SHORT).show()
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Failed"
                            statusText = errorBody
                            Toast.makeText(context, errorBody, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        statusText = "Error: ${e.message}"
                        Toast.makeText(context, statusText, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Enter Student ID first", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Scan Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Scan Attendance QR")
                setBeepEnabled(true)
                setOrientationLocked(true)
                setCaptureActivity(CaptureActivityPortrait::class.java)
            }
            scannerLauncher.launch(options)
        } else {
            Toast.makeText(context, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = statusText)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = studentId,
            onValueChange = { studentId = it },
            label = { Text("Student ID") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (studentId.isEmpty()) {
                    Toast.makeText(context, "Enter Student ID first", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val options = ScanOptions().apply {
                        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        setPrompt("Scan Attendance QR")
                        setBeepEnabled(true)
                        setOrientationLocked(true)
                        setCaptureActivity(CaptureActivityPortrait::class.java)
                    }
                    scannerLauncher.launch(options)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        ) {
            Text("Scan QR")
        }
    }
}