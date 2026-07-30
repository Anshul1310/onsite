package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.network.UserSession
import com.google.gson.Gson
import com.example.myapplication.network.Student

class MainActivity : ComponentActivity() {
    private val deepLinkHandled = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleDeepLink(intent)
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
                composable("count") {
                    AttendanceCountScreen()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "onsite" && uri.host == "callback") {
            val token = uri.getQueryParameter("token")
            val studentJson = uri.getQueryParameter("student")
            if (token != null) {
                UserSession.token = token
                if (studentJson != null) {
                    try {
                        UserSession.student = Gson().fromJson(studentJson, Student::class.java)
                    } catch (_: Exception) {}
                }
                deepLinkHandled.value = true
            }
        }
    }
}
