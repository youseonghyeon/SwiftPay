package com.swiftpay.repository

import com.swiftpay.entity.PendingTransfer
import org.springframework.data.jpa.repository.JpaRepository

interface PendingTransferRepository : JpaRepository<PendingTransfer, Long> {
}
