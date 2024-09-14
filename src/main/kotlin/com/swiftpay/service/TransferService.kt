package com.swiftpay.service

import com.swiftpay.entity.*
import com.swiftpay.repository.ImmediateTransferResultRepository
import com.swiftpay.repository.PendingTransferRepository
import com.swiftpay.repository.ScheduledTransferResultRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
open class TransferService(
    private val accountService: AccountService,
    private val immediateTransferResultRepository: ImmediateTransferResultRepository,
    private val transferValidator: TransferValidator,
    private val pendingTransferRepository: PendingTransferRepository,
    private val scheduledTransferResultRepository: ScheduledTransferResultRepository
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
        val immediateTransferResult = ImmediateTransferResult(
            senderId = senderId,
            recipientId = recipientId,
            amount = amount,
            status = TransferStatus.SUCCESS
        )
        immediateTransferResultRepository.save(immediateTransferResult)
        log.info("Transfer history for transfer from account $senderId to account $recipientId saved successfully")
    }

    @Transactional
    fun scheduleTransfer(senderId: Long, recipientId: Long, amount: Double, scheduleTime: LocalDateTime) {
        log.info("Starting to schedule transfer. Sender: $senderId, Recipient: $recipientId, Amount: $amount, Schedule Time: $scheduleTime")

        val transferDetails = createTransferDetails(senderId, recipientId, amount, scheduleTime)

        // 송금 대기 테이블 저장
        pendingTransferRepository.save(transferDetails.first)
        log.info("Pending transfer saved. Transfer ID: ${transferDetails.first.id}, Status: ${transferDetails.first.status}")

        // 송금 결과 테이블 저장
        scheduledTransferResultRepository.save(transferDetails.second)
        log.info("Scheduled transfer result saved. Transfer ID: ${transferDetails.second.id}, Status: ${transferDetails.second.status}")

        log.info("Transfer scheduling completed successfully.")
    }

    private fun createTransferDetails(
        senderId: Long,
        recipientId: Long,
        amount: Double,
        scheduleTime: LocalDateTime
    ): Pair<PendingTransfer, ScheduledTransferResult> {
        val amountBigDecimal = BigDecimal.valueOf(amount)
        val randomTransactionId = UUID.randomUUID().toString()
        val pendingTransfer = PendingTransfer(
            senderId = senderId,
            recipientId = recipientId,
            amount = amountBigDecimal,
            schedule_time = scheduleTime,
            status = TransferStatus.PENDING,
            transactionId = randomTransactionId
        )
        val scheduledTransferResult = ScheduledTransferResult(
            senderId = senderId,
            recipientId = recipientId,
            amount = amountBigDecimal,
            scheduleTime = scheduleTime,
            status = TransferStatus.PENDING,
            transactionId = randomTransactionId
        )

        return Pair(pendingTransfer, scheduledTransferResult)
    }
}
