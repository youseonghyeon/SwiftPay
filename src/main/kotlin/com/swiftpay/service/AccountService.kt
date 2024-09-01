package com.swiftpay.service

import com.swiftpay.entity.Account
import com.swiftpay.repository.AccountRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class AccountService(private val accountRepository: AccountRepository) {

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
