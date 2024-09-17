package com.swiftpay.service

import com.swiftpay.entity.ImmediateTransferResult
import com.swiftpay.entity.TransferStatus
import com.swiftpay.repository.ImmediateTransferResultRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class TransferHistoryService(
    private val immediateTransferResultRepository: ImmediateTransferResultRepository,
) {

    private val log: Logger = LoggerFactory.getLogger(TransferHistoryService::class.java)

    fun saveTransferHistorySuccess(senderId: Long, recipientId: Long, amount: BigDecimal) {
        val immediateTransferResult = ImmediateTransferResult(
            senderId = senderId,
            recipientId = recipientId,
            amount = amount,
            status = TransferStatus.SUCCESS
        )
        immediateTransferResultRepository.save(immediateTransferResult)
        log.info("Transfer history for transfer from account $senderId to account $recipientId saved successfully")
    }

    fun saveTransferHistoryFail(senderId: Long, recipientId: Long, amount: BigDecimal) {
        val immediateTransferResult = ImmediateTransferResult(
            senderId = senderId,
            recipientId = recipientId,
            amount = amount,
            status = TransferStatus.FAILED
        )
        immediateTransferResultRepository.save(immediateTransferResult)
        log.info("Transfer history for failed transfer from account $senderId to account $recipientId saved successfully")
    }
}
