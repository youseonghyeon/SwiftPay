package com.swiftpay.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class TransferService(
    private val accountService: AccountService,
    private val transferValidator: TransferValidator,
    private val executeTransferService: ExecuteTransferService,
    private val historyService: TransferHistoryService
) {

    private val log: Logger = LoggerFactory.getLogger(TransferService::class.java)

    @Transactional
    fun transferMoney(senderId: Long, recipientId: Long, sendAmount: BigDecimal) {
        log.info("Initiating transfer from account $senderId to account $recipientId for amount $sendAmount")

        val senderAccount = accountService.findById(senderId)
        val recipientAccount = accountService.findById(recipientId)

        // 비정상 요청 체크 및 송금 제한 확인
        transferValidator.checkForAbnormalRequests(senderAccount) // 요청 횟수 체크 ## account 강제 커밋 존재
        transferValidator.validateFrequentTransfers(senderAccount, sendAmount) // 송금 금액 체크 ## account 강제 커밋 존재

        // 송금 실행
        try {
            executeTransferService.executeTransfer(senderAccount.id!!, recipientAccount.id!!, sendAmount)
            historyService.saveTransferHistorySuccess(senderId, recipientId, sendAmount)
        } catch (e: RuntimeException) {
            log.error("Transfer from account $senderId to account $recipientId failed", e)
            historyService.saveTransferHistoryFail(senderId, recipientId, sendAmount)
        }
    }
}
