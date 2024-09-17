package com.swiftpay.repository

import com.swiftpay.entity.PendingTransfer
import com.swiftpay.entity.TransferStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface PendingTransferRepository : JpaRepository<PendingTransfer, Long> {
    fun findByScheduleTimeBeforeAndStatus(beforeDateTime: LocalDateTime, status: TransferStatus ): List<PendingTransfer>
}
