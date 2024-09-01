package com.swiftpay.repository

import com.swiftpay.entity.TransferHistory
import org.springframework.data.jpa.repository.JpaRepository

interface TransferHistoryRepository: JpaRepository<TransferHistory, Long> {


}
