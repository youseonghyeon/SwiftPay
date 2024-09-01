package com.swiftpay.dto

import java.math.BigDecimal

data class CreateAccountResponse(
    val id: Long,
    val username: String,
    val name: String,
    val balance: BigDecimal
)
