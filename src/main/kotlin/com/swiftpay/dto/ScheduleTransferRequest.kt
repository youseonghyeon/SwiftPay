package com.swiftpay.dto

import java.time.LocalDateTime

data class ScheduleTransferRequest (
    val senderId: Long,
    val recipientId: Long,
    val amount: Double,
    val scheduleTime: LocalDateTime
)
