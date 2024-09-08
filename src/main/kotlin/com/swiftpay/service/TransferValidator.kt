package com.swiftpay.service

import com.swiftpay.entity.Account
import com.swiftpay.repository.TransferHistoryRepository
import com.swiftpay.util.TtlMap
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@Component
class TransferValidator(
    private val accountService: AccountService,
    private val transferHistoryRepository: TransferHistoryRepository
) {

    @Value("\${transfer.block-attempt-count:10}") // 기본값 10
    private val blockAttemptCount: Int = 0

    @Value("\${transfer.max-period-amount:10000}") // 기본값 10000
    private val maxPeriodAmount: BigDecimal = BigDecimal(0)

    private val ttlTime: Long = 1L
    private val ttlUnit: TimeUnit = TimeUnit.SECONDS

    private val log = LoggerFactory.getLogger(TransferValidator::class.java)
    private val ttlMap = TtlMap(ttlTime, ttlUnit)

    fun checkForAbnormalRequests(senderAccount: Account) {
        RequestTracker.logRequest(senderAccount.id!!)
        RequestTracker.clearOldRequests(senderAccount.id!!)

        if (RequestTracker.getRequestTimes(senderAccount.id!!).size > blockAttemptCount) {
            senderAccount.blockAccount()
            accountService.saveAccountWithNewTransaction(senderAccount)
            throw IllegalStateException("Account ${senderAccount.id} has been blocked due to abnormal activity. (Max request count exceeded)")
        }
    }

    fun validateFrequentTransfers(senderAccount: Account, sendAmount: BigDecimal) {
        val sendAmountInPeriod = ttlMap.get(senderAccount.id!!)
        if (sendAmountInPeriod + sendAmount > maxPeriodAmount) {
            senderAccount.blockAccount()
            accountService.saveAccountWithNewTransaction(senderAccount)
            throw IllegalStateException("Account ${senderAccount.id} has been blocked due to abnormal activity. (Max transfer amount exceeded)")
        }
        ttlMap.add(senderAccount.id, sendAmount)
    }

    fun calculateDailyTransferAmount(senderAccount: Account): BigDecimal {
        log.info("transferHistoryRepository $transferHistoryRepository")
        return transferHistoryRepository.findSumAmountBySenderIdAndTransferDateBetween(
            senderAccount.id!!,
            LocalDate.now().atStartOfDay(),
            LocalDate.now().atTime(23, 59, 59)
        ) ?: BigDecimal.ZERO
    }

    fun validateAccounts(
        senderAccount: Account,
        recipientAccount: Account,
        sendAmount: BigDecimal,
        dailyTransferAmount: BigDecimal
    ) {
        senderAccount.validateTransferOrThrow(sendAmount, dailyTransferAmount)
        recipientAccount.validateReceiveStatus()
    }
}
