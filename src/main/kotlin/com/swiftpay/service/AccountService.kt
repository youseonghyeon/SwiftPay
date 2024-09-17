package com.swiftpay.service

import com.swiftpay.entity.Account
import com.swiftpay.repository.AccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class AccountService(private val accountRepository: AccountRepository) {

    fun createAccount(
        username: String,
        name: String,
        balance: BigDecimal,
        transactionLimit: BigDecimal = BigDecimal(10000),
        dailyLimit: BigDecimal = BigDecimal(50000),
        isAccountLocked : Boolean = false
    ): Account {
        accountRepository.existsByUsername(username).let {
            if (it) {
                throw IllegalArgumentException("Account with username $username already exists")
            }
        }
        val newAccount = Account(
            username = username,
            name = name,
            balance = balance,
            isAccountLocked = isAccountLocked,
            transactionLimit = transactionLimit,
            dailyLimit = dailyLimit
        )
        return accountRepository.save(newAccount)
    }

    fun findById(id: Long): Account {
        return accountRepository.findById(id).orElseThrow { IllegalArgumentException("Account not found") }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveAccountWithNewTransaction(senderAccount: Account) {
        accountRepository.save(senderAccount)
    }

}
