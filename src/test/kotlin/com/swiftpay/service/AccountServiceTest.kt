package com.swiftpay.service

import com.swiftpay.entity.Account
import com.swiftpay.repository.AccountRepository
import org.junit.jupiter.api.Assertions.*
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
    lateinit var accountRepository: AccountRepository

    @Test
    @DisplayName("유저 생성 테스트")
    fun `create account`() {
        val account = createAccount("test", "Test User", BigDecimal(1000))
        accountRepository.save(account)
        assertNotNull(account.id)
    }

    @Test
    @DisplayName("중복된 유저 생성 테스트")
    fun `create duplicate account`() {
        // 중복 사용자 생성 시도
        createAccount("test", "Test User", BigDecimal(1000))
        val exception = assertThrows<IllegalArgumentException> {
            createAccount("test", "Test User", BigDecimal(1000))
        }
        assertEquals("Account with username test already exists", exception.message)
    }

    fun createAccount(username: String, name: String, balance: BigDecimal): Account {
        accountRepository.existsByUsername(username).let {
            if (it) {
                throw IllegalArgumentException("Account with username $username already exists")
            }
        }
        val newAccount = Account(username = username, name = name, balance = balance, isAccountLocked = false)
        return accountRepository.save(newAccount)
    }
}
