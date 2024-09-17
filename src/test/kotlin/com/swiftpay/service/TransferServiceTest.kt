package com.swiftpay.service

import com.swiftpay.entity.TransferStatus
import com.swiftpay.repository.ImmediateTransferResultRepository
import com.swiftpay.repository.PendingTransferRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
class TransferServiceTest {

    @Autowired
    private lateinit var accountService: AccountService

    @Autowired
    private lateinit var transferService: TransferService

    @Autowired
    private lateinit var pendingTransferRepository: PendingTransferRepository

    @Autowired
    private lateinit var scheduledTransferService: ScheduledTransferService

    @Autowired
    private lateinit var immediateTransferResultRepository: ImmediateTransferResultRepository


    @Test
    @DisplayName("송금 테스트")
    fun `transfer money`() {
        val senderId: Long =
            requireNotNull(accountService.createAccount("sender" + createRandomInt(), "Sender", BigDecimal(10000)).id) {
                "Sender account not created"
            }
        val recipientId: Long =
            requireNotNull(
                accountService.createAccount(
                    "recipient" + createRandomInt(),
                    "Recipient",
                    BigDecimal(10000)
                ).id
            ) {
                "Recipient Account not created"
            }
        val sendAmount = BigDecimal(1000)

        transferService.transferMoney(senderId, recipientId, sendAmount)

        val senderAccount = accountService.findById(senderId)
        val recipientAccount = accountService.findById(recipientId)

        assertEquals(BigDecimal(9000).compareTo(senderAccount.balance), 0)
        assertEquals(BigDecimal(11000).compareTo(recipientAccount.balance), 0)
    }

    @Test
    @DisplayName("잔액 부족 송금 테스트")
    fun `transfer money insufficient balance`() {
        val senderId: Long =
            requireNotNull(accountService.createAccount("sender125" + createRandomInt(), "Sender", BigDecimal(1000)).id) {
                "Sender account not created"
            }
        val recipientId: Long =
            requireNotNull(
                accountService.createAccount(
                    "recipient21525" + createRandomInt(),
                    "Recipient",
                    BigDecimal(10000)
                ).id
            ) {
                "Recipient Account not created"
            }
        val sendAmount = BigDecimal(10000)

        transferService.transferMoney(senderId, recipientId, sendAmount)

        // 금액이 일정한지
        accountService.findById(senderId).let {
            assertEquals(BigDecimal(1000).compareTo(it.balance), 0)
        }
        accountService.findById(recipientId).let {
            assertEquals(BigDecimal(10000).compareTo(it.balance), 0)
        }

//        // 실패 히스토리가 저장되어 있는지
//        val history = immediateTransferResultRepository.findAll()
//            .filter { it.senderId == senderId }
//            .filter { it.recipientId == recipientId }
//            .filter { it.amount == sendAmount }
//
//        assertEquals(1, history.size)
//        assertEquals(TransferStatus.FAILED, history[0].status)
    }

    @Test
    @DisplayName("계좌 없는 송금 테스트")
    fun `transfer money account not found`() {
        val senderId: Long =
            requireNotNull(accountService.createAccount("sender" + createRandomInt(), "Sender", BigDecimal(1000)).id) {
                "Sender account not created"
            }
        val recipientId: Long = 1000
        val sendAmount = BigDecimal(1000)

        val exception = assertThrows<IllegalArgumentException> {
            transferService.transferMoney(senderId, recipientId, sendAmount)
        }
        assertEquals("Account not found", exception.message)
    }

    @Test
    @DisplayName("빈번한 송금 테스트 - 시간단위 송금 횟수 초과")
    fun `transfer money frequent transfers`() {
        val senderId: Long = requireNotNull(
            accountService.createAccount(
                "sender" + createRandomInt(),
                "Sender",
                BigDecimal(100000)
            ).id
        ) {
            "Sender account not created"
        }
        val recipientId: Long =
            requireNotNull(
                accountService.createAccount(
                    "recipient" + createRandomInt(),
                    "Recipient",
                    BigDecimal(10000)
                ).id
            ) {
                "Recipient Account not created"
            }
        val sendAmount = BigDecimal(1000)

        repeat(10) {
            transferService.transferMoney(senderId, recipientId, sendAmount)
        }

        val exception = assertThrows<IllegalStateException> {
            transferService.transferMoney(senderId, recipientId, sendAmount)
        }
        assertEquals(
            "Account $senderId has been blocked due to abnormal activity. (Max request count exceeded)",
            exception.message
        )

        assertTrue { accountService.findById(senderId).isAccountLocked }
    }

    @Test
    @DisplayName("빈번한 송금 테스트 - 시간단위 송금 금액 초과")
    fun `transfer money frequent transfers - exceeds max transfer amount`() {
        val senderId: Long = requireNotNull(
            accountService.createAccount(
                username = "sender3462",
                name = "Sender",
                balance = BigDecimal(9999999999),
                transactionLimit = BigDecimal(9999999999),
                dailyLimit = BigDecimal(9999999999)
            ).id
        ) {
            "Sender account not created"
        }
        val recipientId: Long =
            requireNotNull(
                accountService.createAccount(
                    "recipient2346",
                    "Recipient",
                    balance = BigDecimal(9999999999)
                ).id
            ) {
                "Recipient Account not created"
            }
        val sendAmount = BigDecimal(20000)

        repeat(5) {
            transferService.transferMoney(senderId, recipientId, sendAmount)
        }

        val exception = assertThrows<IllegalStateException> {
            transferService.transferMoney(senderId, recipientId, sendAmount)
        }
        assertEquals(
            "Account $senderId has been blocked due to abnormal activity. (Max transfer amount exceeded)",
            exception.message
        )

        assertTrue { accountService.findById(senderId).isAccountLocked }
    }

    @Test
    @DisplayName("예약 송금 테스트")
    fun `schedule transfer`() {
        val senderId: Long =
            requireNotNull(accountService.createAccount("sender" + createRandomInt(), "Sender", BigDecimal(10000)).id) {
                "Sender account not created"
            }
        val recipientId: Long =
            requireNotNull(
                accountService.createAccount(
                    "recipient" + createRandomInt(),
                    "Recipient",
                    BigDecimal(10000)
                ).id
            ) {
                "Recipient Account not created"
            }
        val sendAmount = BigDecimal(1000)

        scheduledTransferService.scheduleTransferEnroll(
            senderId,
            recipientId,
            sendAmount.toDouble(),
            LocalDateTime.now().plusMinutes(5)
        )


        val findAll = pendingTransferRepository.findAll()
        assertEquals(1, findAll.size)
        assertEquals(senderId, findAll[0].senderId)
        assertEquals(recipientId, findAll[0].recipientId)
        assertEquals(0, findAll[0].amount.compareTo(sendAmount))

    }

    private fun createRandomInt() = (0..100).random()
}
