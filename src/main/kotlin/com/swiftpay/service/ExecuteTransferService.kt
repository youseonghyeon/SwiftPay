package com.swiftpay.service

import com.swiftpay.repository.AccountRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
class ExecuteTransferService(
    private val accountService: AccountService,
    private val transferValidator: TransferValidator,
    private val accountRepository: AccountRepository
) {

    private val log: Logger = LoggerFactory.getLogger(ExecuteTransferService::class.java)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun executeTransfer(senderAccountId: Long, recipientAccountId: Long, sendAmount: BigDecimal) {

        val senderAccount = accountService.findById(senderAccountId)
        val recipientAccount = accountService.findById(recipientAccountId)

        // 송금 한도 및 계좌 상태 확인
        transferValidator.calculateDailyTransferAmount(senderAccount, LocalDate.now())
            .let { transferValidator.validateAccounts(senderAccount, recipientAccount, sendAmount, it) }


        senderAccount.balance = senderAccount.balance - sendAmount
        recipientAccount.balance = recipientAccount.balance + sendAmount

        log.info("Transfer of amount $sendAmount from account ${senderAccount.id} to account ${recipientAccount.id} executed successfully")
    }
}

