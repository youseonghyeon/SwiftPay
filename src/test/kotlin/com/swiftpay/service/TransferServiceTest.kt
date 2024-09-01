package com.swiftpay.service

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class TransferServiceTest {

    @Autowired
    private lateinit var accountService: AccountService

    @Autowired
    private lateinit var transferService: TransferService


    @Autowired
    private lateinit var em: EntityManager

    @Test
    @DisplayName("송금 테스트")
    fun `transfer money`() {
        val senderId: Long = requireNotNull(accountService.createAccount("sender", "Sender", BigDecimal(10000)).id) {
            "Sender account not created"
        }
        val recipientId: Long =
            requireNotNull(accountService.createAccount("recipient", "Recipient", BigDecimal(10000)).id) {
                "Recipient Account not created"
            }
        val sendAmount = BigDecimal(1000)

        transferService.transferMoney(senderId, recipientId, sendAmount)

        em.flush()
        em.clear()

        val senderAccount = accountService.findById(senderId)
        val recipientAccount = accountService.findById(recipientId)

        assertEquals(BigDecimal(9000).compareTo(senderAccount.balance), 0)
        assertEquals(BigDecimal(11000).compareTo(recipientAccount.balance), 0)
    }

    @Test
    @DisplayName("잔액 부족 송금 테스트")
    fun `transfer money insufficient balance`() {
        val senderId: Long = requireNotNull(accountService.createAccount("sender", "Sender", BigDecimal(1000)).id) {
            "Sender account not created"
        }
        val recipientId: Long =
            requireNotNull(accountService.createAccount("recipient", "Recipient", BigDecimal(10000)).id) {
                "Recipient Account not created"
            }
        val sendAmount = BigDecimal(10000)

        val exception = assertThrows<IllegalArgumentException> {
            transferService.transferMoney(senderId, recipientId, sendAmount)
        }
        assertEquals("Insufficient balance in sender's account. Current balance: 1000, Requested amount: 10000", exception.message)
    }

    @Test
    @DisplayName("계좌 없는 송금 테스트")
    fun `transfer money account not found`() {
        val senderId: Long = requireNotNull(accountService.createAccount("sender", "Sender", BigDecimal(1000)).id) {
            "Sender account not created"
        }
        val recipientId: Long = 1000
        val sendAmount = BigDecimal(1000)

        val exception = assertThrows<IllegalArgumentException> {
            transferService.transferMoney(senderId, recipientId, sendAmount)
        }
        assertEquals("Account not found", exception.message)
    }


}
