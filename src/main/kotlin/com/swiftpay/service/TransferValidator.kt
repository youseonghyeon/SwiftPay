package com.swiftpay.service

import com.swiftpay.entity.Account
import com.swiftpay.repository.ImmediateTransferResultRepository
import com.swiftpay.util.TtlMap
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Validates transfer requests before execution.
 * This service checks account statuses, transfer limits, and any abnormal transfer attempts.
 * It also blocks accounts when necessary based on abnormal activities or exceeded transfer limits.
 */
@Component
class TransferValidator(
    private val accountService: AccountService,
    private val immediateTransferResultRepository: ImmediateTransferResultRepository
) {

    @Value("\${transfer.block-attempt-count:10}") // 기본값 10
    private val blockAttemptCount: Int = 0

    @Value("\${transfer.max-period-amount:10000}") // 기본값 10000
    private val maxPeriodAmount: BigDecimal = BigDecimal(0)

    private val ttlTime: Long = 1L
    private val ttlUnit: TimeUnit = TimeUnit.SECONDS

    private val log = LoggerFactory.getLogger(TransferValidator::class.java)
    private val ttlMap = TtlMap(ttlTime, ttlUnit)

    /**
     * Checks for abnormal requests from the sender's account. If the number of requests exceeds
     * the defined block attempt count, the sender's account will be blocked and an exception will be thrown.
     *
     * @param senderAccount the account of the sender making the request.
     * @throws IllegalStateException if the account is blocked due to abnormal activity.
     */
    fun checkForAbnormalRequests(senderAccount: Account) {
        RequestTracker.logRequest(senderAccount.id!!)
        RequestTracker.clearOldRequests(senderAccount.id)

        if (RequestTracker.getRequestTimes(senderAccount.id).size > blockAttemptCount) {
            senderAccount.blockAccount()
            accountService.saveAccountWithNewTransaction(senderAccount)
            throw IllegalStateException("Account ${senderAccount.id} has been blocked due to abnormal activity. (Max request count exceeded)")
        }
    }

    /**
     * Validates the frequency and amount of transfers made from a sender's account.
     * If the total amount transferred in a predefined period exceeds the maximum allowed,
     * the account will be blocked and an exception will be thrown.
     *
     * @param senderAccount the account of the sender making the request
     * @param sendAmount the amount of money to be transferred
     * @throws IllegalStateException if the account is blocked due to exceeding the allowed transfer amount
     */
    fun validateFrequentTransfers(senderAccount: Account, sendAmount: BigDecimal) {
        val sendAmountInPeriod = ttlMap.get(senderAccount.id!!)
        if (sendAmountInPeriod + sendAmount > maxPeriodAmount) {
            senderAccount.blockAccount()
            accountService.saveAccountWithNewTransaction(senderAccount)
            throw IllegalStateException("Account ${senderAccount.id} has been blocked due to abnormal activity. (Max transfer amount exceeded)")
        }
        ttlMap.add(senderAccount.id, sendAmount)
    }

    /**
     * Calculates the total transfer amount of the sender's account for a given day.
     *
     * @param senderAccount the account of the sender making the transfer
     * @param date the specific date for which the daily transfer amount is to be calculated
     * @return the total transfer amount for the given date, or BigDecimal.ZERO if no transfers occurred
     */
    fun calculateDailyTransferAmount(senderAccount: Account, date: LocalDate): BigDecimal {
        log.info("Calculating daily transfer amount for account ID ${senderAccount.id} on ${date}")
        return immediateTransferResultRepository.findSumAmountBySenderIdAndTransferDateBetween(
            senderAccount.id!!,
            date.atStartOfDay(),
            date.atTime(23, 59, 59)
        ) ?: BigDecimal.ZERO
    }

    /**
     * Validates the sender and recipient accounts for a transfer.
     * Ensures that the sender has sufficient balance, the sender's account is not locked,
     * the transfer amount is within the transaction limit, and the daily limit has not been exceeded.
     * It also checks that the recipient's account is not locked.
     *
     * @param senderAccount the account of the sender making the transfer
     * @param recipientAccount the account of the recipient receiving the transfer
     * @param sendAmount the amount of money to be transferred
     * @param dailyTransferAmount the total amount of money transferred in the current day, excluding the current transfer amount
     * @throws IllegalStateException if the sender's account is locked
     * @throws IllegalStateException if the sender's balance is insufficient
     * @throws IllegalStateException if the transfer amount exceeds the transaction limit
     * @throws IllegalStateException if the sum of the daily transfer amount and the current transfer amount exceeds the daily limit
     * @throws IllegalStateException if the recipient's account is locked
     */
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
