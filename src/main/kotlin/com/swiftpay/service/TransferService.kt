package com.swiftpay.service

import com.swiftpay.entity.Account
import com.swiftpay.entity.TransferHistory
import com.swiftpay.entity.TransferStatus
import com.swiftpay.repository.TransferHistoryRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
open class TransferService(
    private val accountService: AccountService,
    private val transferHistoryRepository: TransferHistoryRepository,
    private val transferValidator: TransferValidator
) {

    private val log: Logger = LoggerFactory.getLogger(TransferService::class.java)




    @Transactional
    open fun transferMoney(senderId: Long, recipientId: Long, sendAmount: BigDecimal) {
        log.info("Initiating transfer from account $senderId to account $recipientId for amount $sendAmount")

        val senderAccount = accountService.findById(senderId)
        val recipientAccount = accountService.findById(recipientId)

        // 송금 한도 및 계좌 상태 확인
        val dailyTransferAmount = transferValidator.calculateDailyTransferAmount(senderAccount)
        transferValidator.validateAccounts(senderAccount, recipientAccount, sendAmount, dailyTransferAmount)

        // 비정상 요청 체크 및 송금 제한 확인
        transferValidator.checkForAbnormalRequests(senderAccount) // 요청 횟수 체크 ## account 강제 커밋 존재
        transferValidator.validateFrequentTransfers(senderAccount, sendAmount) // 송금 금액 체크 ## account 강제 커밋 존재

        // 송금 실행
        executeTransfer(senderAccount, recipientAccount, sendAmount)

        // 송금 내역 저장
        saveTransferHistory(senderId, recipientId, sendAmount)

        log.info("Transfer from account $senderId to account $recipientId completed successfully")
    }

    private fun executeTransfer(senderAccount: Account, recipientAccount: Account, sendAmount: BigDecimal) {
        senderAccount.balance = senderAccount.balance - sendAmount
        recipientAccount.balance = recipientAccount.balance + sendAmount
        log.info("Transfer of amount $sendAmount from account ${senderAccount.id} to account ${recipientAccount.id} executed successfully")
    }

    private fun saveTransferHistory(senderId: Long, recipientId: Long, amount: BigDecimal) {
        val transferHistory = TransferHistory(
            senderId = senderId,
            recipientId = recipientId,
            amount = amount,
            status = TransferStatus.SUCCESS
        )
        transferHistoryRepository.save(transferHistory)
        log.info("Transfer history for transfer from account $senderId to account $recipientId saved successfully")
    }
}
