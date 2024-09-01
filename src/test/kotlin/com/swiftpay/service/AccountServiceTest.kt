package com.swiftpay.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.test.Test

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class AccountServiceTest {

    @Autowired
    private lateinit var accountService: AccountService

    @Test
    @DisplayName("유저 생성 테스트")
    fun `create account`() {
        val account = accountService.createAccount("test", "Test User", BigDecimal(1000))
        assertNotNull(account.id)
    }

    @Test
    @DisplayName("중복된 유저 생성 테스트")
    fun `create duplicate account`() {
        // 중복 사용자 생성 시도
        accountService.createAccount("test", "Test User", BigDecimal(1000))
        val exception = assertThrows<IllegalArgumentException> {
            accountService.createAccount("test", "Test User", BigDecimal(1000))
        }
        assertEquals("Account with username test already exists", exception.message)
    }

}
