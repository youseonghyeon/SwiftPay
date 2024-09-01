package com.swiftpay.dto

import java.math.BigDecimal

data class CreateAccountRequest(
    val username: String,
    val name: String,
    val balance: BigDecimal
)

