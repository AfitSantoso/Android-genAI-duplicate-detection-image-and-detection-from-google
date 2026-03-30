package com.example.detectionimgmobile.ui.bm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.detectionimgmobile.data.api.RetrofitClient
import com.example.detectionimgmobile.data.model.BmFraudResultResponse
import com.example.detectionimgmobile.data.model.BreakdownItem
import com.example.detectionimgmobile.data.model.LoginData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmFraudScoreScreen(
    user: LoginData,
    customerId: Int,
    onBack: () -> Unit
) {
    var fraudResult by remember { mutableStateOf<BmFraudResultResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(customerId) {
        coroutineScope.launch {
            try {
                val api = RetrofitClient.getAuthService(user.user_id.toString(), user.role)
                val response = withContext(Dispatchers.IO) { api.getBmCustomerFraudResult(customerId) }
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        fraudResult = body.data
                    } else {
                        errorMessage = body?.message ?: "Failed to load fraud score"
                    }
                } else {
                    errorMessage = "Error: ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Network error occurred"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FRAUD SCORE") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (fraudResult != null) {
                val data = fraudResult!!
                val score = data.fridays_score
                val breakdownList = data.breakdown ?: emptyList()
                
                if (score == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "No Data",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = data.message ?: "Belum ada dokumen yang diupload oleh CMO",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (score.any_fraud_detected) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "CUSTOMER: ${data.customer.name ?: "N/A"}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${score.score_percentage}%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 48.sp,
                                        color = if (score.any_fraud_detected) Color.Red else Color(0xFF4CAF50)
                                    )
                                    Text(
                                        text = "Risk Level: ${(score.risk_level ?: "UNKNOWN").replace("_", " ")}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val validCount = breakdownList.size - score.total_flagged
                                    Text(
                                        text = "Summary: $validCount / ${breakdownList.size} Valid Documents",
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
    
                        item {
                            Text(
                                text = "Document Breakdown",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
    
                        items(breakdownList) { item ->
                            DocumentBreakdownCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentBreakdownCard(item: BreakdownItem) {
    val sim = item.similarity_percentage ?: 0.0
    val isRed = sim > 65.0 || item.detection_status == "FRAUD" || item.detection_status == "INSTANT_DUPLICATE"
    val isYellow = !isRed && (sim in 50.0..65.0 || item.detection_status?.contains("DUPLICATE") == true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.component ?: "Unknown Component",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Category: ${item.category ?: "N/A"}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    if (item.comparison_summary != null) {
                        Text(
                            text = item.comparison_summary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                if (isRed) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Fraud",
                        tint = Color.Red,
                        modifier = Modifier.size(32.dp)
                    )
                } else if (isYellow) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Caution",
                        tint = Color(0xFFFFA000), // Yellow/Orange
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Safe",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            if ((isRed || isYellow) && item.fraud_patterns?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                val bgColor = if (isRed) Color(0xFFFFEBEE) else Color(0xFFFFF8E1)
                val textColor = if (isRed) Color.Red else Color(0xFFF57C00)
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor)
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (isRed) "Fraud Patterns:" else "Cek Kembali (Caution):",
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 12.sp
                    )
                    item.fraud_patterns.forEach { pattern ->
                        Text(
                            text = "• ${pattern ?: ""}",
                            color = textColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
