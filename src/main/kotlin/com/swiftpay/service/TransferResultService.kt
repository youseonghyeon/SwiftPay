package com.swiftpay.service

import com.swiftpay.entity.ImmediateTransferResult
import com.swiftpay.entity.ScheduledTransferResult
import com.swiftpay.entity.TransferStatus
import com.swiftpay.repository.ImmediateTransferResultRepository
import com.swiftpay.repository.ScheduledTransferResultRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Handles both immediate and scheduled transfer results.
 * Responsible for saving and updating transfer results in the database.
 */
@Service
class TransferResultService(
    private val immediateTransferResultRepository: ImmediateTransferResultRepository,
    private val scheduledTransferResultRepository: ScheduledTransferResultRepository
) {

    private val log: Logger = LoggerFactory.getLogger(TransferResultService::class.java)

    fun saveImmediateTransferResult(
        senderId: Long,
        recipientId: Long,
        amount: BigDecimal,
        status: TransferStatus,
        transactionId: String
    ) {
        val immediateTransferResult = ImmediateTransferResult(
            senderId = senderId,
            recipientId = recipientId,
            amount = amount,
            status = status
        )
        immediateTransferResultRepository.save(immediateTransferResult)
        log.info("Transfer history for transfer from account $senderId to account $recipientId saved successfully")
    }

    fun saveScheduledTransferResult(
        senderId: Long,
        recipientId: Long,
        amount: BigDecimal,
        status: TransferStatus,
        transactionId: String,
        scheduleTime: LocalDateTime
    ) {
        val scheduledTransferResult = ScheduledTransferResult(
            senderId = senderId,
            recipientId = recipientId,
            amount = amount,
            status = status,
            transactionId = transactionId,
            scheduleTime = scheduleTime
        )
        scheduledTransferResultRepository.save(scheduledTransferResult)
        log.info("Scheduled transfer history for transfer from account $senderId to account $recipientId saved successfully")
    }


}
