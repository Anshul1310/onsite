package com.example.myapplication

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.UserSession

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current

    LaunchedEffect(UserSession.token) {
        if (UserSession.token.isNotEmpty()) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Attendance App", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Login with your DAuth account", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                val loginUrl = "${RetrofitClient.BASE_URL}/auth/login"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login with DAuth", fontSize = 18.sp)
        }
    }
}
