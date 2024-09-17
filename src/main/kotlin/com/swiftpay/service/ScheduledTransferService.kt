package com.swiftpay.service

import com.swiftpay.entity.PendingTransfer
import com.swiftpay.entity.ScheduledTransferResult
import com.swiftpay.entity.TransferStatus
import com.swiftpay.repository.PendingTransferRepository
import com.swiftpay.repository.ScheduledTransferResultRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Handles scheduled transfers.
 * This service is responsible for enrolling and processing transfers that are scheduled for future dates.
 */
@Service
class ScheduledTransferService(
    private val accountService: AccountService,
    private val pendingTransferRepository: PendingTransferRepository,
    private val scheduledTransferResultRepository: ScheduledTransferResultRepository,
    private val executeTransferService: ExecuteTransferService,
    private val transferResultService: TransferResultService
) {

    private val log: Logger = LoggerFactory.getLogger(ScheduledTransferService::class.java)

    /**
     * Enrolls a new scheduled transfer.
     * Saves the transfer details and schedules it for future processing.
     *
     * @param senderId the ID of the sender's account
     * @param recipientId the ID of the recipient's account
     * @param amount the amount of money to transfer
     * @param scheduleTime the time when the transfer should be executed
     */
    @Transactional
    fun scheduleTransferEnroll(senderId: Long, recipientId: Long, amount: Double, scheduleTime: LocalDateTime) {
        log.info("Starting to schedule transfer. Sender: $senderId, Recipient: $recipientId, Amount: $amount, Schedule Time: $scheduleTime")

        val transferDetails = createTransferDetails(senderId, recipientId, amount, scheduleTime)

        // 송금 대기 테이블 저장
        pendingTransferRepository.save(transferDetails.first)
        log.info("Pending transfer saved. Transfer ID: ${transferDetails.first.id}, Status: ${transferDetails.first.status}")

        // 송금 결과 테이블 저장
//        transferResultService.saveScheduledTransferResult(transferDetails.second)
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
            scheduleTime = scheduleTime,
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

    /**
     * Processes scheduled transfers by executing them based on the pending transfer details.
     * This method handles the entire process of validating accounts, executing the transfer,
     * and updating the transfer status in the repository.
     *
     * @param pendingTransferId the ID of the pending transfer to process. This ID is used to
     *        retrieve the specific pending transfer record from the repository.
     */
    @Transactional
    fun scheduledTransferProcess(pendingTransferId: Long) {
        val pendingTransfer = pendingTransferRepository.findById(pendingTransferId).get()
        log.info("Starting scheduled transfer processing for transfer ID: $pendingTransferId")
        val senderAccount = accountService.findById(pendingTransfer.senderId)
        val recipientAccount = accountService.findById(pendingTransfer.recipientId)
        val sendAmount = pendingTransfer.amount

        // 송금 실행
        try {
            executeTransferService.executeTransfer(senderAccount.id!!, recipientAccount.id!!, sendAmount)
            val transactionId: String = pendingTransfer.transactionId
            // 송금 테이블에서 삭제
            pendingTransferRepository.delete(pendingTransfer)
            // 송금 결과 테이블 업데이트
            scheduledTransferResultRepository.findByTransactionId(transactionId)!!
                .let { it.status = TransferStatus.SUCCESS }
            log.info("Scheduled transfer completed successfully for transfer ID: $pendingTransferId")
        } catch (e: RuntimeException) {
            log.error("Scheduled transfer failed transferId:  $pendingTransferId", e)
            val transactionId: String = pendingTransfer.transactionId
            // 송금 테이블 실패 업데이트
            pendingTransfer.status = TransferStatus.FAILED
            // 송금 결과 테이블 업데이트
            scheduledTransferResultRepository.findByTransactionId(transactionId)!!.status = TransferStatus.FAILED
        }
    }

}
