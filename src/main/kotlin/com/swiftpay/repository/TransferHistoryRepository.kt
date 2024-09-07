package com.swiftpay.repository

import com.swiftpay.entity.TransferHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Transactional(readOnly = true)
interface TransferHistoryRepository : JpaRepository<TransferHistory, Long> {

    @Query("SELECT SUM(th.amount) FROM TransferHistory th WHERE th.senderId = :senderId AND th.transferDate BETWEEN :transferDate AND :transferDate2")
    fun findSumAmountBySenderIdAndTransferDateBetween(
        senderId: Long,
        transferDate: LocalDateTime,
        transferDate2: LocalDateTime
    ): BigDecimal?

}
