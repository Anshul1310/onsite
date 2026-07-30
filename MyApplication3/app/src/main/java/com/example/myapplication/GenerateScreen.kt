package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.network.GenerateRequest
import com.example.myapplication.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun GenerateScreen() {
    var expirationMinutesText by remember { mutableStateOf("10") }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var qrUrl by remember { mutableStateOf<String?>(null) }
    var expiresAtIso by remember { mutableStateOf<String?>(null) }
    var expiresAtMillis by remember { mutableStateOf<Long?>(null) }
    var remainingSeconds by remember { mutableStateOf<Long?>(null) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Countdown Timer Effect


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Generate QR Code",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = expirationMinutesText,
            onValueChange = { expirationMinutesText = it },
            label = { Text("Expiration Time (minutes)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val mins = expirationMinutesText.toIntOrNull() ?: 10
                loading = true
                errorMessage = ""
                scope.launch {
                    try {
                        val response = RetrofitClient.api.generateQr(GenerateRequest(expirationTime = mins))
                        if (response.isSuccessful && response.body()?.success == true) {
                            val body = response.body()!!
                            qrUrl = body.qrUrl
                            expiresAtIso = body.expiresAt

                            // Parse ISO timestamp
                            body.expiresAt?.let { isoStr ->
                                try {
                                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                                    val date = sdf.parse(isoStr)
                                    expiresAtMillis = date?.time
                                } catch (_: Exception) {
                                    expiresAtMillis = System.currentTimeMillis() + (mins * 60 * 1000)
                                }
                            }
                        } else {
                            errorMessage = response.body()?.message ?: "Failed to generate QR Code"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error: ${e.message}"
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate QR Code", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (loading) {
            CircularProgressIndicator()
        } else if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
        }

        // Display Generated QR Code and Expiration Information
        if (qrUrl != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "QR Code Session",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Resolve localhost host for Android emulator/device testing
                    val resolvedUrl = remember(qrUrl) {
                        qrUrl?.let { url ->
                            if (url.contains("localhost") || url.contains("127.0.0.1")) {
                                val baseHost = try {
                                    java.net.URI(RetrofitClient.BASE_URL).host ?: "10.0.2.2"
                                } catch (_: Exception) {
                                    "10.0.2.2"
                                }
                                url.replace("localhost", baseHost).replace("127.0.0.1", baseHost)
                            } else {
                                url
                            }
                        }
                    }

                    AsyncImage(
                        model = resolvedUrl,
                        contentDescription = "Generated QR Code Image",
                        modifier = Modifier
                            .size(240.dp)
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Formatted Expiration Time
                    val formattedExpiresAt = remember(expiresAtMillis) {
                        expiresAtMillis?.let {
                            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            formatter.format(Date(it))
                        } ?: expiresAtIso ?: "N/A"
                    }

                    Text(
                        text = "Expiration Time: $formattedExpiresAt",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Countdown Timer Display
                    val timerDisplay = remember(remainingSeconds) {
                        val secs = remainingSeconds
                        if (secs == null) "--:--"
                        else if (secs <= 0) "Expired"
                        else {
                            val m = secs / 60
                            val s = secs % 60
                            String.format(Locale.getDefault(), "%02d:%02d", m, s)
                        }
                    }


                }
            }
        }
    }
}
