package com.swiftpay.service

import io.micrometer.core.instrument.MeterRegistry
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Handles the actual transfer process between accounts.
 * This service is responsible for executing the transfer of funds
 * from the sender's account to the recipient's account.
 */
@Service
class ExecuteTransferService(
    private val accountService: AccountService,
    private val transferValidator: TransferValidator,
    private val meterRegistry: MeterRegistry
) {

    private val log: Logger = LoggerFactory.getLogger(ExecuteTransferService::class.java)

    @PostConstruct
    fun metricInit() {
        meterRegistry.counter("swiftpay_transfer_request_total", "status", "success")
    }

    /**
     * Executes a money transfer between two accounts.
     * Validates account status and transfer limits before adjusting balances.
     *
     * @param senderAccountId the ID of the sender's account
     * @param recipientAccountId the ID of the recipient's account
     * @param sendAmount the amount of money to transfer
     * @throws IllegalStateException if the sender's account is locked
     * @throws IllegalStateException if the sender's balance is insufficient
     * @throws IllegalStateException if the transfer amount exceeds the transaction limit
     * @throws IllegalStateException if the transfer amount exceeds the daily limit
     * @throws IllegalStateException if the recipient's account is locked
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    internal fun executeTransfer(senderAccountId: Long, recipientAccountId: Long, sendAmount: BigDecimal) {

        val senderAccount = accountService.findById(senderAccountId)
        val recipientAccount = accountService.findById(recipientAccountId)

        // 송금 한도 및 계좌 상태 확인
        transferValidator.calculateDailyTransferAmount(senderAccount, LocalDate.now())
            .let { transferValidator.validateAccounts(senderAccount, recipientAccount, sendAmount, it) }

        senderAccount.balance = senderAccount.balance - sendAmount
        recipientAccount.balance = recipientAccount.balance + sendAmount

        incrementMetricForTest()

        log.info("Transfer of amount $sendAmount from account ${senderAccount.id} to account ${recipientAccount.id} executed successfully")
    }

    private fun incrementMetricForTest() {
        try {
            meterRegistry.counter("swiftpay_transfer_request_total", "status", "success").increment()
        } catch (e: Exception) {
            log.error("Error incrementing metric: ${e.message}")
        }
    }
}

