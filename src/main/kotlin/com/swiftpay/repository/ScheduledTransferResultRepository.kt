package com.swiftpay.repository

import com.swiftpay.entity.ScheduledTransferResult
import org.springframework.data.jpa.repository.JpaRepository

interface ScheduledTransferResultRepository : JpaRepository<ScheduledTransferResult, Long> {
    fun findByTransactionId(transactionId: String): ScheduledTransferResult?
}
