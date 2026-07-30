package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.network.UserSession

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val startDest = if (UserSession.token.isNotEmpty()) "home" else "login"

            NavHost(
                navController = navController,
                startDestination = startDest
            ) {
                composable("login") {
                    LoginScreen(navController)
                }
                composable("home") {
                    HomeScreen(navController)
                }
                composable("scanner") {
                    ScannerScreen()
                }
                composable("attendance") {
                    AttendanceScreen()
                }
                composable("generate") {
                    GenerateScreen()
                }
            }
        }
    }
}
