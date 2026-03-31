package com.example.detectionimgmobile.ui.bm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.detectionimgmobile.data.model.CustomerFraudSummary
import com.example.detectionimgmobile.data.model.LoginData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmCustomerListScreen(
    user: LoginData,
    cmoId: Int,
    onBack: () -> Unit,
    onCustomerClick: (Int) -> Unit
) {
    var customerList by remember { mutableStateOf<List<CustomerFraudSummary>>(emptyList()) }
    var cmoName by remember { mutableStateOf("Customers") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(cmoId) {
        coroutineScope.launch {
            try {
                val api = RetrofitClient.getAuthService(user.user_id.toString(), user.role)
                val response = withContext(Dispatchers.IO) { api.getBmCmoCustomers(cmoId) }
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        customerList = body.data?.customers ?: emptyList()
                        body.data?.cmo?.name?.let { cmoName = it }
                    } else {
                        errorMessage = body?.message ?: "Failed to load customers"
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
                title = { Text(cmoName ?: "Customers") },
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
            } else if (customerList.isEmpty()) {
                Text(
                    text = "No customers found for this CMO",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(customerList) { customer ->
                        CustomerCard(
                            customer = customer,
                            user = user,
                            onClick = { onCustomerClick(customer.customer_id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCard(customer: CustomerFraudSummary, user: LoginData, onClick: () -> Unit) {
    var invalidDocs by remember { mutableStateOf(customer.total_flagged ?: 0) }
    var scoreFetched by remember { mutableStateOf(false) }

    LaunchedEffect(customer.customer_id) {
        if (!scoreFetched && (customer.fraud_status == "FLAGGED" || customer.fraud_status == "CONFIRMED_FRAUD")) {
            try {
                val api = RetrofitClient.getAuthService(user.user_id.toString(), user.role)
                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    api.getBmCustomerFraudResult(customer.customer_id)
                }
                if (response.isSuccessful) {
                    val breakdown = response.body()?.data?.breakdown
                    if (breakdown != null) {
                        invalidDocs = breakdown.count { item ->
                            val sim = item.similarity_percentage ?: 0.0
                            sim > 65.0 || item.detection_status == "FRAUD" || item.detection_status == "INSTANT_DUPLICATE"
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore, fallback to default value
            } finally {
                scoreFetched = true
            }
        }
    }

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
                    text = customer.customer_name ?: "Unknown",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Contract: ${customer.no_contract ?: ""}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = "Status: ${customer.fraud_status ?: ""}",
                    fontSize = 14.sp
                )
            }

            // Right side showing total flagged documents
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
                    Text(
                        text = "Clean",
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
