package com.swiftpay.dto

import java.math.BigDecimal

data class TransferRequest (
    val senderId: Long,
    val recipientId: Long,
    val amount: BigDecimal
)
