package com.swiftpay.repository

import com.swiftpay.entity.Account
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository: JpaRepository<Account, Long> {
    fun existsByUsername(username: String): Boolean
}
