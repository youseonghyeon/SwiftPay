package com.swiftpay.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "pending_transfer")
class PendingTransfer(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val transactionId: String,

    @Column(name = "sender_id", nullable = false)
    var senderId: Long,

    @Column(name = "recipient_id", nullable = false)
    var recipientId: Long,

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    var amount: BigDecimal,

    @Column(name = "transfer_time", nullable = false)
    var schedule_time: LocalDateTime,

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: TransferStatus = TransferStatus.PENDING,

    @Column(name = "description")
    var description: String? = null

) : BaseEntity()
