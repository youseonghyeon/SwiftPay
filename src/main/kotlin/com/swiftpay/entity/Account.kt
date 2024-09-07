package com.swiftpay.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "account")
class Account(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(name = "username", nullable = false, unique = true)
    val username: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    var balance: BigDecimal,

    @Column(name = "is_account_locked", nullable = false)
    var isAccountLocked: Boolean = false,

    @Column(name = "transaction_limit", nullable = false, precision = 15, scale = 2)
    var transactionLimit: BigDecimal,

    @Column(name = "daily_limit", nullable = false, precision = 15, scale = 2)
    var dailyLimit: BigDecimal,

    ) {

    fun validateTransferOrThrow(amount: BigDecimal, dailyTransferAmount: BigDecimal) {
        if (isAccountLocked) {
            throw IllegalStateException("Account is locked and cannot process transfer (account id: $id, amount: $amount, balance: $balance)")
        }
        if (amount > transactionLimit) {
            throw IllegalStateException("Transfer amount exceeds the transaction limit (account id: $id, amount: $amount, transactionLimit: $transactionLimit)")
        }
        if (dailyTransferAmount + amount > dailyLimit) {
            throw IllegalStateException("Transfer amount exceeds the daily limit (account id: $id, amount: $amount, dailyTransferAmount: $dailyTransferAmount, dailyLimit: $dailyLimit)")
        }
    }

    fun validateReceiveStatus() {
        if (isAccountLocked) {
            throw IllegalStateException("Account is locked and cannot process receive (account id: $id)")
        }
    }

    fun blockAccount() {
        isAccountLocked = true
    }
}
