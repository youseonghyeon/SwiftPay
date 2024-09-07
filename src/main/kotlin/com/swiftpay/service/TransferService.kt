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
import java.time.LocalDate

@Service
class TransferService(
    private val accountService: AccountService,
    private val transferHistoryRepository: TransferHistoryRepository
) {

    private val log: Logger = LoggerFactory.getLogger(TransferService::class.java)

    private val BLOCK_ATTEMPT_COUNT = 5

    /**
     * 지정된 송신자 계좌에서 수신자 계좌로 금액을 이체하는 기능을 수행합니다.
     * 이 과정에서 송신자 계좌의 비정상 접근 여부를 확인하고,
     * 송신 및 수신 계좌의 상태를 검증한 후, 이체를 수행합니다.
     * 이체 후에는 이체 내역이 기록됩니다.
     *
     * @param senderId 송신자 계좌의 ID
     * @param recipientId 수신자 계좌의 ID
     * @param sendAmount 이체할 금액
     *
     * @throws IllegalArgumentException 송신자 계좌의 잔액이 부족한 경우 발생
     * @throws IllegalStateException 비정상 접근이 감지되어 송신자 계좌가 차단된 경우 발생
     */
    @Transactional
    fun transferMoney(senderId: Long, recipientId: Long, sendAmount: BigDecimal) {
        log.info("Initiating transfer from account $senderId to account $recipientId for amount $sendAmount")

        val senderAccount = accountService.findById(senderId)
        val recipientAccount = accountService.findById(recipientId)

        checkForAbnormalRequests(senderAccount)

        val dailyTransferAmount =
            transferHistoryRepository.findSumAmountBySenderIdAndTransferDateBetween(
                senderAccount.id!!,
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(23, 59, 59)
            ) ?: BigDecimal.ZERO

        validateAccounts(senderAccount, recipientAccount, sendAmount, dailyTransferAmount)
        validateTransferAmount(senderAccount.balance, sendAmount)

        executeTransfer(senderAccount, recipientAccount, sendAmount)
        saveTransferHistory(senderId, recipientId, sendAmount)

        log.info("Transfer from account $senderId to account $recipientId completed successfully")
    }

    private fun checkForAbnormalRequests(senderAccount: Account) {
        senderAccount.id ?: throw IllegalArgumentException("Account not found")
        RequestTracker.logRequest(senderAccount.id)
        RequestTracker.clearOldRequests(senderAccount.id)


        if (RequestTracker.getRequestTimes(senderAccount.id).size > BLOCK_ATTEMPT_COUNT) {
            senderAccount.blockAccount()
            throw IllegalStateException("Account ${senderAccount.id} has been blocked due to abnormal activity.")
        }
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

    private fun validateTransferAmount(senderBalance: BigDecimal, sendAmount: BigDecimal) {
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
