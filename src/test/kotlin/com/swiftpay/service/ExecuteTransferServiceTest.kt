package com.swiftpay.service

import com.swiftpay.repository.AccountRepository
import com.swiftpay.testutil.MockAccount
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class ExecuteTransferServiceTest {

    @Autowired
    private lateinit var executeTransferService: ExecuteTransferService

    @Autowired
    private lateinit var accountService: AccountService


    /**
     * 1. 송금자 계정 잠겼는지
     * 2. 송금액이 잔액보다 큰지
     * 3. 송금액이 거래 한도를 초과하는지
     * 4. 송금액이 일일 한도를 초과하는지
     * 5. 받는사람 계정 잠김
     */


    @Test
    @DisplayName("송금 실행 성공 테스트")
    fun `execute transfer success`() {
        val senderAccount = accountService.createAccount("sender", "sender", BigDecimal(10000))
        val recipientAccount = accountService.createAccount("receiver", "receiver", BigDecimal(10000))


        executeTransferService.executeTransfer(senderAccount.id!!, recipientAccount.id!!, BigDecimal(1000))

        accountService.findById(senderAccount.id!!).let {
            assert(it.balance == BigDecimal(9000))
        }
        accountService.findById(recipientAccount.id!!).let {
            assert(it.balance == BigDecimal(11000))
        }
    }


}
