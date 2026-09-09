package com.autologix.tms.data.models

import com.google.gson.annotations.SerializedName

data class MonthlyIncidentTrendDto(
    @SerializedName("month")
    val month: String,
    @SerializedName("reported")
    val reported: Int,
    @SerializedName("resolved")
    val resolved: Int
)

data class IncidentCategoryCountDto(
    @SerializedName("category")
    val category: String,
    @SerializedName("count")
    val count: Int,
    @SerializedName("percentage")
    val percentage: Double
)

data class CustomerReportsDto(
    @SerializedName("fleetAvailabilityPercent")
    val fleetAvailabilityPercent: Double = 0.0,
    @SerializedName("averageResolutionTimeHours")
    val averageResolutionTimeHours: Double = 0.0,
    @SerializedName("pmCompliancePercent")
    val pmCompliancePercent: Double = 0.0,
    @SerializedName("totalIncidentsReported")
    val totalIncidentsReported: Int = 0,
    @SerializedName("resolvedIncidentsCount")
    val resolvedIncidentsCount: Int = 0,
    @SerializedName("monthlyTrends")
    val monthlyTrends: List<MonthlyIncidentTrendDto> = emptyList(),
    @SerializedName("incidentsByCategory")
    val incidentsByCategory: List<IncidentCategoryCountDto> = emptyList()
)
