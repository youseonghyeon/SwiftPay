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
class TransferService(
    private val accountService: AccountService,
    private val transferHistoryRepository: TransferHistoryRepository
) {

    private val log: Logger = LoggerFactory.getLogger(TransferService::class.java)

    @Transactional
    fun transferMoney(senderId: Long, recipientId: Long, sendAmount: BigDecimal) {
        log.info("Initiating transfer from account $senderId to account $recipientId for amount $sendAmount")

        val senderAccount = accountService.findById(senderId)
        val recipientAccount = accountService.findById(recipientId)

        validateAccount(senderAccount, recipientAccount)

        validateTransfer(senderAccount.balance, sendAmount)

        executeTransfer(senderAccount, recipientAccount, sendAmount)

        saveTransferHistory(senderId, recipientId, sendAmount)

        log.info("Transfer from account $senderId to account $recipientId completed successfully")
    }

    private fun validateAccount(senderAccount: Account, recipientAccount: Account) {
        senderAccount.canProcessTransferStatusCheck()
        recipientAccount.canProcessReceiveStatusCheck()
    }

    private fun validateTransfer(senderBalance: BigDecimal, sendAmount: BigDecimal) {
        if (senderBalance < sendAmount) {
            throw IllegalArgumentException("Insufficient balance in sender's account. Current balance: $senderBalance, Requested amount: $sendAmount")
        }
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
