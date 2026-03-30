package com.example.detectionimgmobile.ui.bm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.detectionimgmobile.data.model.CmoFraudSummary
import com.example.detectionimgmobile.data.model.LoginData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmDashboardScreen(
    user: LoginData,
    onCmoClick: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var cmoList by remember { mutableStateOf<List<CmoFraudSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val api = RetrofitClient.getAuthService(user.user_id.toString(), user.role)
                val response = withContext(Dispatchers.IO) { api.getBmCmoList() }
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        cmoList = body.data?.cmo_list ?: emptyList()
                    } else {
                        errorMessage = body?.message ?: "Failed to load CMO list"
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
                title = { Text("BM Dashboard - ${user.name ?: ""}") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
            } else if (cmoList.isEmpty()) {
                Text(
                    text = "No CMOs found",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cmoList) { cmo ->
                        CmoCard(cmo = cmo, onClick = { onCmoClick(cmo.cmo_id) })
                    }
                }
            }
        }
    }
}

@Composable
fun CmoCard(cmo: CmoFraudSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cmo.cmo_name ?: "N/A",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "NIP: ${cmo.nip ?: ""}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                if (!cmo.area.isNullOrEmpty()) {
                    Text(
                        text = "Area: ${cmo.area}",
                        fontSize = 14.sp
                    )
                }
            }

            // Right side showing total fraud documents
            val invalidDocs = cmo.total_fraud_documents ?: cmo.flagged_count // Fallback to flagged_count
            Column(horizontalAlignment = Alignment.End) {
                if (invalidDocs > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Invalid Documents",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$invalidDocs",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = "Invalid Docs",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = "0 Invalid",
                        color = Color(0xFF4CAF50), // Green
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View Customers",
                    tint = Color.Gray
                )
            }
        }
    }
}
