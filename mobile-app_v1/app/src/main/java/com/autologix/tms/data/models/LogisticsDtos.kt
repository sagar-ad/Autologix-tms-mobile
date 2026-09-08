package com.autologix.tms.data.models

data class GateInRequestDto(
    val trolleyId: String,
    val fromLocation: String,
    val gatePassNumber: String,
    val truckNumber: String,
    val driverName: String,
    val conditionStatus: String, // GOOD, MINOR_DAMAGE, MAJOR_DAMAGE
    val project: String? = null,
    val customer: String? = null,
    val remarks: String? = null,
    val photoUrls: List<String> = emptyList()
)

data class GateOutRequestDto(
    val trolleyId: String,
    val toLocation: String,
    val dispatchNumber: String,
    val truckNumber: String,
    val driverName: String,
    val sealNumber: String? = null,
    val project: String? = null,
    val customer: String? = null,
    val remarks: String? = null
)

data class MovementRecordDto(
    val id: String,
    val trolleyId: String,
    val trolleyNumber: String,
    val movementType: String, // IN, OUT
    val sourceLocation: String,
    val destinationLocation: String,
    val referenceDocNumber: String, // Gate Pass or Dispatch No
    val carrierTruck: String,
    val driverName: String,
    val sealNumber: String? = null,
    val project: String? = null,
    val customer: String? = null,
    val conditionStatus: String = "GOOD",
    val remarks: String? = null,
    val operatorName: String,
    val timestamp: String,
    val photoUrls: List<String> = emptyList()
)

data class LogisticsKpisDto(
    val todayInCount: Int = 18,
    val todayOutCount: Int = 24,
    val netYardBalance: Int = 142,
    val inTransitCount: Int = 38,
    val activeBaysCount: Int = 8
)
