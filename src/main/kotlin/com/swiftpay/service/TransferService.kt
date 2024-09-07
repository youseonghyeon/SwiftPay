package com.swiftpay.service

import com.swiftpay.entity.Account
import com.swiftpay.entity.TransferHistory
import com.swiftpay.entity.TransferStatus
import com.swiftpay.repository.TransferHistoryRepository
import com.swiftpay.util.TtlMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.concurrent.TimeUnit

open class TransferService(
    private val accountService: AccountService,
    private val transferHistoryRepository: TransferHistoryRepository,
    private val blockAttemptCount: Int,
    private val maxPeriodAmount: BigDecimal
) {

    private val log: Logger = LoggerFactory.getLogger(TransferService::class.java)


    private val ttlMap = TtlMap(1L, TimeUnit.SECONDS)

    @Transactional
    open fun transferMoney(senderId: Long, recipientId: Long, sendAmount: BigDecimal) {
        log.info("Initiating transfer from account $senderId to account $recipientId for amount $sendAmount")

        val senderAccount = accountService.findById(senderId)
        val recipientAccount = accountService.findById(recipientId)

        // 비정상 요청 체크 및 송금 제한 확인
        checkForAbnormalRequests(senderAccount) // 요청 횟수 체크
        validateFrequentTransfers(senderAccount, sendAmount) // 송금 금액 체크

        // 송금 한도 및 계좌 상태 확인
        val dailyTransferAmount = calculateDailyTransferAmount(senderAccount)
        validateAccounts(senderAccount, recipientAccount, sendAmount, dailyTransferAmount)

        // 송금 실행
        executeTransfer(senderAccount, recipientAccount, sendAmount)

        // 송금 내역 저장
        saveTransferHistory(senderId, recipientId, sendAmount)

        log.info("Transfer from account $senderId to account $recipientId completed successfully")
    }

    private fun checkForAbnormalRequests(senderAccount: Account) {
        RequestTracker.logRequest(senderAccount.id!!)
        RequestTracker.clearOldRequests(senderAccount.id!!)

        if (RequestTracker.getRequestTimes(senderAccount.id!!).size > blockAttemptCount) {
            senderAccount.blockAccount()
            throw IllegalStateException("Account ${senderAccount.id} has been blocked due to abnormal activity. (Max request count exceeded)")
        }
    }

    private fun validateFrequentTransfers(senderAccount: Account, sendAmount: BigDecimal) {
        val sendAmountInPeriod = ttlMap.get(senderAccount.id!!)
        if (sendAmountInPeriod + sendAmount > maxPeriodAmount) {
            senderAccount.blockAccount()
            throw IllegalStateException("Account ${senderAccount.id} has been blocked due to abnormal activity. (Max transfer amount exceeded)")
        }
        ttlMap.add(senderAccount.id, sendAmount)
    }

    private fun calculateDailyTransferAmount(senderAccount: Account): BigDecimal {
        return transferHistoryRepository.findSumAmountBySenderIdAndTransferDateBetween(
            senderAccount.id!!,
            LocalDate.now().atStartOfDay(),
            LocalDate.now().atTime(23, 59, 59)
        ) ?: BigDecimal.ZERO
    }

    private fun validateAccounts(
        senderAccount: Account,
        recipientAccount: Account,
        sendAmount: BigDecimal,
        dailyTransferAmount: BigDecimal
    ) {
        senderAccount.validateTransferOrThrow(sendAmount, dailyTransferAmount)
        recipientAccount.validateReceiveStatus()
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
