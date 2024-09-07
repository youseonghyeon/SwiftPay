package com.swiftpay.conf

import com.swiftpay.repository.TransferHistoryRepository
import com.swiftpay.service.AccountService
import com.swiftpay.service.TransferService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

@Configuration
class AppConfig {

    @Value("\${transfer.block-attempt-count:10}") // 기본값 10
    private val blockAttemptCount: Int = 0

    @Value("\${transfer.max-period-amount:10000}") // 기본값 10000
    private val maxPeriodAmount: BigDecimal = BigDecimal(0)

    @Bean
    fun transferService(
        accountService: AccountService,
        transferHistoryRepository: TransferHistoryRepository
    ): TransferService {
        return TransferService(
            accountService,
            transferHistoryRepository,
            blockAttemptCount,
            maxPeriodAmount
        )
    }

}
