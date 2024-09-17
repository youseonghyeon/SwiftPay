package com.swiftpay.service

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import kotlin.test.assertEquals

//@Transactional (commented out to avoid transactional issues)
@SpringBootTest
@ActiveProfiles("test")
class ExecuteTransferServiceTest {

    @Autowired
    private lateinit var executeTransferService: ExecuteTransferService

    @Autowired
    private lateinit var accountService: AccountService


    @Test
    @DisplayName("송금 실행 성공 테스트")
    fun `execute transfer success`() {
        val senderAccount = accountService.createAccount("sender", "sender", BigDecimal(10000))
        val recipientAccount = accountService.createAccount("receiver", "receiver", BigDecimal(10000))

        executeTransferService.executeTransfer(senderAccount.id!!, recipientAccount.id!!, BigDecimal(1000))

        accountService.findById(senderAccount.id!!).let {
            assertEquals(BigDecimal(9000).compareTo(it.balance), 0)
        }
        accountService.findById(recipientAccount.id!!).let {
            assertEquals(BigDecimal(11000).compareTo(it.balance), 0)
        }
    }

    @Test
    @DisplayName("송금 실행 실패 테스트 - 송금자 계정 잠김")
    fun `execute transfer fail - sender account locked`() {
        val senderAccount = accountService.createAccount("sender2", "sender", BigDecimal(10000), isAccountLocked = true)
        val recipientAccount = accountService.createAccount("receiver1", "receiver", BigDecimal(10000))

        val exception = assertThrows<IllegalStateException> {
            executeTransferService.executeTransfer(senderAccount.id!!, recipientAccount.id!!, BigDecimal(1000))
        }
        assertEquals("Account is locked", exception.message)
    }

    @Test
    @DisplayName("송금 실행 실패 테스트 - 송금 한도 초과")
    fun `execute transfer fail - transfer limit exceeded`() {
        val senderAccount =
            accountService.createAccount("sender642356", "sender", BigDecimal(100000), transactionLimit = BigDecimal(8000))
        val recipientAccount = accountService.createAccount("receiver21253", "receiver", BigDecimal(100000))

        val exception = assertThrows<IllegalStateException> {
            executeTransferService.executeTransfer(senderAccount.id!!, recipientAccount.id!!, BigDecimal(8100))
        }
        assertEquals("Transfer amount exceeds the transaction limit", exception.message)
    }

    @Test
    @DisplayName("송금 실행 실패 테스트 - 일일 한도 초과")
    fun `execute transfer fail - daily limit exceeded`() {
        val senderAccount =
            accountService.createAccount("sender32315", "sender", BigDecimal(100000), dailyLimit = BigDecimal(8000))
        val recipientAccount = accountService.createAccount("receiver3", "receiver", BigDecimal(100000))

        val exception = assertThrows<IllegalStateException> {
            executeTransferService.executeTransfer(senderAccount.id!!, recipientAccount.id!!, BigDecimal(8100))
        }
        assertEquals("Transfer amount exceeds the daily limit", exception.message)
    }

    @Test
    @DisplayName("송금 실행 실패 테스트 - 수신자 계정 잠김")
    fun `execute transfer fail - recipient account locked`() {
        val senderAccount = accountService.createAccount("sender4347", "sender", BigDecimal(10000))
        val recipientAccount =
            accountService.createAccount("receiver3474", "receiver", BigDecimal(10000), isAccountLocked = true)

        val exception = assertThrows<IllegalStateException> {
            executeTransferService.executeTransfer(senderAccount.id!!, recipientAccount.id!!, BigDecimal(1000))
        }
        assertEquals("Account(receiver) is locked", exception.message)
    }

}
