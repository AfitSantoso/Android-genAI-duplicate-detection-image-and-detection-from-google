package com.example.detectionimgmobile.data.model

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonElement

data class BmCmoListResponse(
    val total: Int?,
    val cmo_list: List<CmoFraudSummary>?
)

data class CmoFraudSummary(
    val cmo_id: Int,
    val cmo_name: String,
    val nip: String,
    val area: String?,
    val total_customers: Int,
    val flagged_count: Int,
    val confirmed_fraud_count: Int,
    val total_fraud_documents: Int? // Number of invalid documents
)

data class BmCustomerListResponse(
    val cmo: CmoBasicInfo?,
    val total_customers: Int,
    val customers: List<CustomerFraudSummary>
)

data class CmoBasicInfo(
    val id: Int,
    val name: String,
    val nip: String,
    val area: String?
)

data class CustomerFraudSummary(
    @SerializedName("id", alternate = ["customer_id"]) val customer_id: Int,
    val no_contract: String?,
    @SerializedName("name", alternate = ["customer_name"]) val customer_name: String?,
    val fraud_status: String?,
    val score_percentage: Double?,
    val decision: String?,
    val risk_level: String?,
    val total_flagged: Int?, // Number of invalid documents per customer
    val any_fraud_detected: Boolean?,
    val fridays_calculated_at: String?
)

data class BmFraudResultResponse(
    val customer: CustomerFraudDetail,
    @SerializedName("fridays_score", alternate = ["fridays_result"]) val fridays_score: FridaysScoreDetail?,
    val breakdown: List<BreakdownItem>?,
    val message: String?
)

data class CustomerFraudDetail(
    val id: Int,
    val no_contract: String?,
    val name: String?,
    val fraud_status: String?,
    val cmo_name: String?,
    val cmo_nip: String?
)

data class FridaysScoreDetail(
    val score: Double?,
    val score_percentage: Double?,
    val decision: String?,
    val risk_level: JsonElement?,
    val summary: ResultSummary?,
    val breakdown: List<BreakdownItem>?
) {
    val total_flagged: Int
        get() = (summary?.flagged_documents?.size ?: 0) + (summary?.google_flagged_proofs?.size ?: 0)

    val any_fraud_detected: Boolean
        get() = total_flagged > 0
}

data class RiskLevel(
    val level: String?,
    val color: String?,
    val label: String?,
    val description: String?,
    val action_required: String?
)

data class ResultSummary(
    val total_components: Int?,
    val uploaded_components: Int?,
    val skipped_components: Int?,
    val flagged_documents: List<String>?,
    val google_flagged_proofs: List<String>?,
    val calculated_at: String?
)

data class BreakdownItem(
    val component: String?,
    val category: String?,
    val similarity_percentage: Double?,
    val matched_fields: List<MatchedField>?,
    val comparison_summary: String?,
    val fraud_patterns: List<JsonElement>?,
    val top_similar_matches: List<SimilarMatch>?,
    val visual_analysis: String?,
    val detection_status: String?,
    val note: String?
)

data class FraudPattern(
    val pattern: String?,
    val severity: String?,
    val description: String?,
    val detail: String?
)

data class MatchedField(
    val field: String,
    val field_key: String,
    val value_preview: String,
    val is_match: Boolean,
    val similarity_percentage: Double,
    val detail: String?
)

data class SimilarMatch(
    val rank: Int,
    val source: String,
    val title: String,
    val link: String?,
    val similarity_percentage: Double,
    val similarity_description: String
)
