package com.swiftpay.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Column
import java.math.BigDecimal

@Entity
@Table(name = "account")
data class Account(

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
) {


    fun canProcessTransferStatusCheck() {
        if (isAccountLocked) {
            throw IllegalStateException("Account is locked and cannot process transfer (account id: $id)")
        }
    }

    fun canProcessReceiveStatusCheck() {
        if (isAccountLocked) {
            throw IllegalStateException("Account is locked and cannot process receive (account id: $id)")
        }
    }

    fun blockAccount() {
        isAccountLocked = true
    }
}
