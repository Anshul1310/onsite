package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun ScannerScreen() {

    val context = LocalContext.current

    var scannedToken by remember {
        mutableStateOf("")
    }

    //-----------------------------------------------------
    // QR Scanner
    //-----------------------------------------------------

    val scannerLauncher =
        rememberLauncherForActivityResult(
            contract = ScanContract()
        ) { result ->

            if (result.contents != null) {

                scannedToken = result.contents

                Toast.makeText(
                    context,
                    "QR : ${result.contents}",
                    Toast.LENGTH_SHORT
                ).show()

                // TODO:
                // Send result.contents to backend

            } else {

                Toast.makeText(
                    context,
                    "Scan Cancelled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    //-----------------------------------------------------
    // Camera Permission
    //-----------------------------------------------------

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                val options = ScanOptions().apply {

                    setDesiredBarcodeFormats(
                        ScanOptions.QR_CODE
                    )

                    setPrompt("Scan Attendance QR")

                    setBeepEnabled(true)

                    setOrientationLocked(true)

                    setCaptureActivity(
                        CaptureActivityPortrait::class.java
                    )
                }

                scannerLauncher.launch(options)

            } else {

                Toast.makeText(
                    context,
                    "Camera Permission Denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    //-----------------------------------------------------
    // UI
    //-----------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text =
                if (scannedToken.isEmpty())
                    "No QR scanned"
                else
                    scannedToken
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(

            onClick = {

                if (
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                    val options = ScanOptions().apply {

                        setDesiredBarcodeFormats(
                            ScanOptions.QR_CODE
                        )

                        setPrompt("Scan Attendance QR")

                        setBeepEnabled(true)

                        setOrientationLocked(true)

                        setCaptureActivity(
                            CaptureActivityPortrait::class.java
                        )
                    }

                    scannerLauncher.launch(options)

                } else {

                    permissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }
            }

        ) {

            Text("Scan QR")
        }
    }
}