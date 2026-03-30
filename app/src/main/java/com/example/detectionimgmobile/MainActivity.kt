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
import com.example.detectionimgmobile.ui.bm.BmDashboardScreen
import com.example.detectionimgmobile.ui.bm.BmCustomerListScreen
import com.example.detectionimgmobile.ui.bm.BmFraudScoreScreen
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
                                if (userData.role == "BM" || userData.role == "AUDIT") {
                                    navController.navigate("bm_dashboard/$userJson") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("dashboard/$userJson") {
                                        popUpTo("login") { inclusive = true }
                                    }
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
                    composable("bm_dashboard/{userJson}") { backStackEntry ->
                        val userJson = backStackEntry.arguments?.getString("userJson")
                        val user = Gson().fromJson(userJson, LoginData::class.java)
                        
                        BmDashboardScreen(
                            user = user,
                            onCmoClick = { cmoId ->
                                navController.navigate("bm_cmo_customers/$userJson/$cmoId")
                            },
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("bm_dashboard/{userJson}") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("bm_cmo_customers/{userJson}/{cmoId}") { backStackEntry ->
                        val userJson = backStackEntry.arguments?.getString("userJson")
                        val cmoIdString = backStackEntry.arguments?.getString("cmoId")
                        val user = Gson().fromJson(userJson, LoginData::class.java)
                        val cmoId = cmoIdString?.toIntOrNull() ?: 0
                        
                        BmCustomerListScreen(
                            user = user,
                            cmoId = cmoId,
                            onBack = { navController.popBackStack() },
                            onCustomerClick = { customerId ->
                                navController.navigate("bm_customer_fraud/$userJson/$customerId")
                            }
                        )
                    }
                    composable("bm_customer_fraud/{userJson}/{customerId}") { backStackEntry ->
                        val userJson = backStackEntry.arguments?.getString("userJson")
                        val customerIdString = backStackEntry.arguments?.getString("customerId")
                        val user = Gson().fromJson(userJson, LoginData::class.java)
                        val customerId = customerIdString?.toIntOrNull() ?: 0
                        
                        BmFraudScoreScreen(
                            user = user,
                            customerId = customerId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
