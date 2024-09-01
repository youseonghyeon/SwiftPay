package com.swiftpay.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "transfer_history")
data class TransferHistory(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "sender_id", nullable = false)
    val senderId: Long,

    @Column(name = "recipient_id", nullable = false)
    val recipientId: Long,

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    val amount: BigDecimal,

    @Column(name = "transfer_date", nullable = false)
    val transferDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val status: TransferStatus = TransferStatus.PENDING,

    @Column(name = "description")
    val description: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class TransferStatus {
    PENDING,
    SUCCESS,
    FAILED
}
