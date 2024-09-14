package com.swiftpay.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.LocalDateTime

@MappedSuperclass
open class BaseEntity {

    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null
        private set

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
        private set

    @PrePersist
    fun onCreate() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
