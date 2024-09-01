package com.swiftpay.dto

import java.time.LocalDateTime

data class ScheduleTransferRequest (
    val senderId: Long,
    val recipientId: String,
    val amount: Double,
    val scheduledDate: LocalDateTime
)
