package com.swiftpay.repository

import com.swiftpay.entity.ImmediateTransferResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Transactional(readOnly = true)
interface ImmediateTransferResultRepository : JpaRepository<ImmediateTransferResult, Long> {

    @Query("SELECT SUM(th.amount) FROM ImmediateTransferResult th WHERE th.senderId = :senderId AND th.transferTime BETWEEN :transferDate AND :transferDate2")
    fun findSumAmountBySenderIdAndTransferDateBetween(
        senderId: Long,
        transferDate: LocalDateTime,
        transferDate2: LocalDateTime
    ): BigDecimal?

}
