package com.swiftpay.service

import com.swiftpay.entity.TransferHistory
import com.swiftpay.entity.TransferStatus
import com.swiftpay.repository.TransferHistoryRepository
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.DisplayName
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
    private lateinit var transferHistoryRepository: TransferHistoryRepository


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

        transferMoney(senderId, recipientId, sendAmount)

        em.flush()
        em.clear()

        val senderAccount = accountService.findById(senderId)
        val recipientAccount = accountService.findById(recipientId)

        assertEquals(BigDecimal(9000).compareTo(senderAccount.balance), 0)
        assertEquals(BigDecimal(11000).compareTo(recipientAccount.balance), 0)
    }

    private fun transferMoney(senderId: Long, recipientId: Long, sendAmount: BigDecimal) {
        val senderAccount = accountService.findById(senderId)
        val recipientAccount = accountService.findById(recipientId)

        if (senderAccount.balance < sendAmount) {
            throw IllegalArgumentException("송신자 계좌에 잔액이 부족합니다. 현재 잔액: ${senderAccount.balance} 요청 금액: $sendAmount")
        }

        // 송금 처리
        senderAccount.balance = senderAccount.balance - sendAmount
        recipientAccount.balance = recipientAccount.balance + sendAmount
        val transferHistory = TransferHistory(
            senderId = senderId, recipientId = recipientId, amount = sendAmount, status = TransferStatus.SUCCESS
        )
        transferHistoryRepository.save(transferHistory)
    }

}
