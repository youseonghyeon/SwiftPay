package com.swiftpay.service

import com.swiftpay.entity.TransferStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

/**
 * Manages the entire process of immediate transfers.
 * This service coordinates validation, execution, and logging of real-time transfers.
 */
@Service
class ImmediateTransferService(
    private val accountService: AccountService,
    private val transferValidator: TransferValidator,
    private val executeTransferService: ExecuteTransferService,
    private val historyService: TransferResultService
) {

    private val log: Logger = LoggerFactory.getLogger(ImmediateTransferService::class.java)

    /**
     * Initiates an immediate transfer process.
     * Validates the transfer request and executes the transaction.
     *
     * @param senderId the ID of the sender's account
     * @param recipientId the ID of the recipient's account
     * @param amount the amount of money to transfer
     */
    @Transactional
    fun immediateTransferProcess(senderId: Long, recipientId: Long, amount: BigDecimal) {
        log.info("Initiating transfer from account $senderId to account $recipientId for amount $amount")

        val senderAccount = accountService.findById(senderId)
        val recipientAccount = accountService.findById(recipientId)

        // 비정상 요청 체크 및 송금 제한 확인
        transferValidator.checkForAbnormalRequests(senderAccount) // 요청 횟수 체크 ## account 강제 커밋 존재
        transferValidator.validateFrequentTransfers(senderAccount, amount) // 송금 금액 체크 ## account 강제 커밋 존재

        // 송금 ID
        val transactionId = UUID.randomUUID().toString()

        // 송금 실행
        try {
            executeTransferService.executeTransfer(senderAccount.id!!, recipientAccount.id!!, amount)
            historyService.saveImmediateTransferResult(senderId, recipientId, amount, TransferStatus.SUCCESS, transactionId)
        } catch (e: RuntimeException) {
            log.error("Transfer from account $senderId to account $recipientId failed", e)
            historyService.saveImmediateTransferResult(senderId, recipientId, amount, TransferStatus.FAILED, transactionId)
        }
    }
}
