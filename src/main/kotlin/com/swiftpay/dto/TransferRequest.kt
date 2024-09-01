package com.swiftpay.dto

data class TransferRequest (
    val senderId: Long,
    val recipientId: String,
    val amount: Double
)
