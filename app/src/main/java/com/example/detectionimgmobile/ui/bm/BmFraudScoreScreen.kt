package com.example.detectionimgmobile.ui.bm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.detectionimgmobile.data.api.RetrofitClient
import com.example.detectionimgmobile.data.model.BmFraudResultResponse
import com.example.detectionimgmobile.data.model.BreakdownItem
import com.example.detectionimgmobile.data.model.ExtractedFieldInfo
import com.example.detectionimgmobile.data.model.LoginData
import com.example.detectionimgmobile.data.model.MatchedField
import com.example.detectionimgmobile.data.model.OcrDiagnostics
import com.example.detectionimgmobile.data.model.RawTextBlock
import com.example.detectionimgmobile.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    var visible by remember { mutableStateOf(false) }
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
                delay(100)
                visible = true
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "FRIDAYS Analysis",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Analyzing fraud data...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (fraudResult != null) {
                val data = fraudResult!!
                val score = data.fridays_score
                val breakdownList = score?.breakdown ?: data.breakdown ?: emptyList()

                if (score == null) {
                    // ── No Data State ──
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "No Analysis Available",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = data.message ?: "CMO belum mengupload dokumen untuk customer ini",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val scorePercentage = score.score_percentage ?: 0.0
                    val totalFlagged = score.total_flagged ?: 0
                    val anyFraud = score.any_fraud_detected ?: (totalFlagged > 0)

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // ── Score Circle Card ──
                        item {
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { -30 })
                            ) {
                                ScoreHeaderCard(
                                    customerName = data.customer.name ?: "N/A",
                                    noContract = data.customer.no_contract ?: "",
                                    scorePercentage = scorePercentage,
                                    decision = score.decision ?: "",
                                    riskLevel = score.risk_level,
                                    totalDocs = breakdownList.size,
                                    totalFlagged = totalFlagged,
                                    anyFraud = anyFraud
                                )
                            }
                        }

                        // ── Decision Banner ──
                        item {
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
                            ) {
                                DecisionBanner(
                                    decision = score.decision ?: "",
                                    scorePercentage = scorePercentage
                                )
                            }
                        }

                        // ── Section Header ──
                        item {
                            Text(
                                text = "Document Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // ── Breakdown Cards ──
                        itemsIndexed(breakdownList) { index, item ->
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn() + slideInVertically(
                                    initialOffsetY = { 30 * (index + 1) }
                                )
                            ) {
                                DocumentBreakdownCard(item = item)
                            }
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreHeaderCard(
    customerName: String,
    noContract: String,
    scorePercentage: Double,
    decision: String,
    riskLevel: com.google.gson.JsonElement?,
    totalDocs: Int,
    totalFlagged: Int,
    anyFraud: Boolean
) {
    val scoreColor = when {
        scorePercentage <= 25.0 -> ScoreSafe
        scorePercentage <= 60.0 -> ScoreWarning
        scorePercentage <= 80.0 -> ScoreDanger
        else -> ScoreCritical
    }

    val animatedScore by animateFloatAsState(
        targetValue = (scorePercentage / 100f).toFloat(),
        animationSpec = tween(durationMillis = 1200),
        label = "scoreAnimation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Customer Info ──
            Text(
                text = customerName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = noContract,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Circular Score Gauge ──
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Background arc
                    drawArc(
                        color = scoreColor.copy(alpha = 0.15f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    // Score arc
                    drawArc(
                        color = scoreColor,
                        startAngle = 135f,
                        sweepAngle = 270f * animatedScore,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${scorePercentage.toInt()}%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "Risk Score",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Risk Level Label ──
            val riskLevelText = if (riskLevel == null || riskLevel.isJsonNull) {
                "Unknown"
            } else if (riskLevel.isJsonObject) {
                riskLevel.asJsonObject.get("label")?.asString ?: "Unknown"
            } else {
                riskLevel.asString.replace("_", " ")
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = scoreColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = riskLevelText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Stats Row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MiniStat(
                    value = "$totalDocs",
                    label = "Documents",
                    color = MaterialTheme.colorScheme.primary
                )
                MiniStat(
                    value = "${totalDocs - totalFlagged}",
                    label = "Valid",
                    color = SuccessGreen
                )
                MiniStat(
                    value = "$totalFlagged",
                    label = "Flagged",
                    color = DangerRed
                )
            }
        }
    }
}

@Composable
fun MiniStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun DecisionBanner(decision: String, scorePercentage: Double) {
    val (bannerColor, icon, label) = when {
        scorePercentage <= 25.0 -> Triple(SuccessGreen, Icons.Default.CheckCircle, "APPROVE")
        scorePercentage <= 60.0 -> Triple(WarningOrange, Icons.Default.Info, "APPROVE WITH NOTES")
        scorePercentage <= 80.0 -> Triple(ScoreDanger, Icons.Default.Search, "SURVEY BY OUTSOURCING")
        else -> Triple(ScoreCritical, Icons.Default.Block, "REJECT / INVESTIGATE")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = bannerColor.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = bannerColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Decision",
                    style = MaterialTheme.typography.labelSmall,
                    color = bannerColor.copy(alpha = 0.7f)
                )
                Text(
                    text = decision.ifEmpty { label },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = bannerColor
                )
            }
        }
    }
}

@Composable
fun DocumentBreakdownCard(item: BreakdownItem) {
    val sim = item.similarity_percentage ?: 0.0
    val isRed = sim > 65.0 || item.detection_status == "FRAUD" || item.detection_status == "INSTANT_DUPLICATE"
    val isYellow = !isRed && (sim in 50.0..65.0 || item.detection_status?.contains("DUPLICATE") == true)

    val statusColor = when {
        isRed -> DangerRed
        isYellow -> WarningOrange
        else -> SuccessGreen
    }

    val statusIcon = when {
        isRed -> Icons.Default.Warning
        isYellow -> Icons.Default.Info
        else -> Icons.Default.CheckCircle
    }

    var expanded by remember { mutableStateOf(isRed || isYellow) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Header Row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.component ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.category ?: "N/A",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ── Similarity Badge ──
                if (sim > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${sim.toInt()}%",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // ── Detection Status Badge ──
            if (item.detection_status != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = item.detection_status.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // ── Comparison Summary ──
            if (item.comparison_summary != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.comparison_summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ── Expanded Content ──
            if (expanded) {
                // ── OCR Diagnostics Section ──
                if (item.ocr_diagnostics != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OcrDiagnosticsCard(diagnostics = item.ocr_diagnostics)
                }

                // ── Extracted Values (Side-by-Side Comparison) ──
                if (item.new_doc_extracted?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ExtractedValuesSection(
                        newDoc = item.new_doc_extracted,
                        matchedDoc = item.matched_doc_extracted,
                        isMatch = isRed || isYellow
                    )
                }

                // ── Fraud Patterns ──
                if ((isRed || isYellow) && item.fraud_patterns?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = statusColor.copy(alpha = 0.08f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isRed) "⚠ Fraud Patterns Detected" else "⚡ Caution Items",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            item.fraud_patterns.forEach { patternElement ->
                                val textStr = if (patternElement.isJsonObject) {
                                    val obj = patternElement.asJsonObject
                                    val desc = obj.get("description")?.asString
                                    val pat = obj.get("pattern")?.asString
                                    desc ?: pat ?: ""
                                } else {
                                    patternElement.asString
                                }
                                if (textStr.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "•",
                                            color = statusColor,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(end = 6.dp, top = 1.dp)
                                        )
                                        Text(
                                            text = textStr,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = statusColor.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Matched Fields (Enhanced) ──
                if (item.matched_fields?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(12.dp))
                    MatchedFieldsSection(fields = item.matched_fields)
                }

                // ── Top Similar Matches (Proof) ──
                if (item.top_similar_matches?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Internet Sources Found",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    item.top_similar_matches.forEach { match ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rank
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(DangerRed.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${match.rank}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRed
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = match.source,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = match.similarity_description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "${match.similarity_percentage.toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed
                                )
                            }
                        }
                    }
                }

                // ── Visual Analysis (Proof) ──
                if (item.visual_analysis != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                imageVector = Icons.Default.Insights,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.visual_analysis,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // ── Raw OCR Text Blocks (collapsible) ──
                if (item.raw_text_blocks?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(10.dp))
                    RawTextBlocksSection(blocks = item.raw_text_blocks)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// ── OCR Diagnostics Card ──
// ══════════════════════════════════════════════════════════════
@Composable
fun OcrDiagnosticsCard(diagnostics: OcrDiagnostics) {
    val statusColor = when (diagnostics.status) {
        "OK" -> SuccessGreen
        "WARNING" -> WarningOrange
        "ERROR" -> DangerRed
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val avgConf = diagnostics.avg_confidence ?: 0.0
    val confColor = when {
        avgConf >= 0.85 -> SuccessGreen
        avgConf >= 0.65 -> WarningOrange
        else -> DangerRed
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // ── Title row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Scanner,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "OCR Diagnostics",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                // Status badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = diagnostics.status ?: "N/A",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Confidence Gauge ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Confidence",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    // Background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(confColor.copy(alpha = 0.12f))
                    )
                    // Filled
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = avgConf.toFloat().coerceIn(0f, 1f))
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(confColor)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(avgConf * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = confColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Stats Grid ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DiagnosticChip(
                    label = "Blocks",
                    value = "${diagnostics.total_text_blocks ?: 0}",
                    icon = Icons.Default.ViewModule
                )
                DiagnosticChip(
                    label = "Chars",
                    value = "${diagnostics.total_chars_extracted ?: 0}",
                    icon = Icons.Default.TextFields
                )
                DiagnosticChip(
                    label = "Fields",
                    value = "${diagnostics.identifiers_count ?: diagnostics.identifiers_found?.size ?: 0}",
                    icon = Icons.Default.FindInPage
                )
            }

            // ── Processing time & Engine ──
            if (diagnostics.processing_time_ms != null || diagnostics.ocr_engine != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (diagnostics.ocr_engine != null) {
                        Text(
                            text = diagnostics.ocr_engine,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (diagnostics.processing_time_ms != null) {
                        Text(
                            text = "⏱ ${"%.0f".format(diagnostics.processing_time_ms)}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // ── Identifiers Found ──
            if (diagnostics.identifiers_found?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    diagnostics.identifiers_found.forEach { id ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AccentCyan.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = id.replace("_", " "),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentCyan,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticChip(label: String, value: String, icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// ── Extracted Values — Side-by-Side Comparison ──
// ══════════════════════════════════════════════════════════════
@Composable
fun ExtractedValuesSection(
    newDoc: Map<String, ExtractedFieldInfo>,
    matchedDoc: Map<String, ExtractedFieldInfo>?,
    isMatch: Boolean
) {
    val hasMatchedDoc = matchedDoc?.isNotEmpty() == true

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (hasMatchedDoc && isMatch)
            DangerRed.copy(alpha = 0.04f)
        else
            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // ── Section Title ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (hasMatchedDoc) Icons.AutoMirrored.Filled.CompareArrows else Icons.Default.Description,
                    contentDescription = null,
                    tint = if (hasMatchedDoc) AccentGold else AccentCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasMatchedDoc) "Side-by-Side Comparison" else "Extracted OCR Values",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (hasMatchedDoc) {
                // ── Column Headers ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📄 New Document",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "📋 Matched Document",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DangerRed,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.height(6.dp))

                // ── Compare each field ──
                newDoc.forEach { (key, newField) ->
                    val matchedField = matchedDoc[key]
                    val fieldsIdentical = newField.value == matchedField?.value && matchedField != null

                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        // Field label
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = newField.label ?: key.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (fieldsIdentical) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = DangerRed.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "MATCH",
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRed,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Side-by-side values
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // New doc value
                            Surface(
                                modifier = Modifier.weight(1f).padding(end = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                            ) {
                                Text(
                                    text = newField.value_masked ?: newField.value ?: "N/A",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                )
                            }
                            // Matched doc value
                            if (matchedField != null) {
                                Surface(
                                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (fieldsIdentical)
                                        DangerRed.copy(alpha = 0.08f)
                                    else
                                        SuccessGreen.copy(alpha = 0.06f)
                                ) {
                                    Text(
                                        text = matchedField.value_masked ?: matchedField.value ?: "N/A",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (fieldsIdentical) DangerRed else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.End
                                    )
                                }
                            } else {
                                Text(
                                    text = "—",
                                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            } else {
                // ── Single document extracted values ──
                newDoc.forEach { (key, field) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = field.label ?: key.replace("_", " "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                        ) {
                            Text(
                                text = field.value_masked ?: field.value ?: "N/A",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// ── Enhanced Matched Fields Section ──
// ══════════════════════════════════════════════════════════════
@Composable
fun MatchedFieldsSection(fields: List<MatchedField>) {
    Column {
        Text(
            text = "Matched Fields",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        fields.forEach { field ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (field.is_match)
                    DangerRed.copy(alpha = 0.05f)
                else
                    SuccessGreen.copy(alpha = 0.05f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // ── Field name + match badge ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = field.field,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (field.is_match) DangerRed.copy(alpha = 0.12f) else SuccessGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (field.is_match) "Match ${field.similarity_percentage.toInt()}%" else "Unique",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (field.is_match) DangerRed else SuccessGreen
                            )
                        }
                    }

                    // ── Extracted values preview ──
                    if (field.value_extracted != null || field.value_preview.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Value: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            Text(
                                text = field.value_preview,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // ── Matched value comparison ──
                    if (field.matched_value_preview != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Match: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = DangerRed.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = field.matched_value_preview,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = DangerRed,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // ── Detail ──
                    if (field.detail != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = field.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// ── Raw OCR Text Blocks (Collapsible) ──
// ══════════════════════════════════════════════════════════════
@Composable
fun RawTextBlocksSection(blocks: List<RawTextBlock>) {
    var showBlocks by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showBlocks = !showBlocks },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Raw OCR Blocks (${blocks.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (showBlocks) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }

            if (showBlocks) {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(6.dp))

                blocks.forEachIndexed { idx, block ->
                    val conf = block.confidence ?: 0.0
                    val confColor = when {
                        conf >= 0.85 -> SuccessGreen
                        conf >= 0.65 -> WarningOrange
                        else -> DangerRed
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Confidence indicator dot
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(confColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = block.text ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 10.sp,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${(conf * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = confColor,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}
