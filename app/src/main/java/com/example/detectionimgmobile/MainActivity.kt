package com.example.detectionimgmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.detectionimgmobile.data.model.LoginData
import com.example.detectionimgmobile.ui.dashboard.DashboardScreen
import com.example.detectionimgmobile.ui.login.LoginScreen
import com.example.detectionimgmobile.ui.theme.DetectionimgmobileTheme
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DetectionimgmobileTheme {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { userData ->
                                val userJson = Gson().toJson(userData)
                                navController.navigate("dashboard/$userJson") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("dashboard/{userJson}") { backStackEntry ->
                        val userJson = backStackEntry.arguments?.getString("userJson")
                        val user = Gson().fromJson(userJson, LoginData::class.java)
                        
                        DashboardScreen(
                            user = user,
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("dashboard/{userJson}") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
