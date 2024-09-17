package com.swiftpay.scheduler

import com.swiftpay.entity.TransferStatus
import com.swiftpay.repository.PendingTransferRepository
import com.swiftpay.service.ScheduledTransferService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class PendingTransferScheduler(
    private val pendingTransferRepository: PendingTransferRepository,
    private val scheduledTransferService: ScheduledTransferService
) {

    private val log: Logger = LoggerFactory.getLogger(PendingTransferScheduler::class.java)

    @Scheduled(cron = "0 * * * * *")
    fun processPendingTransfers() {
        log.info("Starting to process pending transfers, scheduling time:: ${LocalDateTime.now()}")
        pendingTransferRepository.findByScheduleTimeBeforeAndStatus(LocalDateTime.now(), TransferStatus.PENDING)
            .forEach { scheduledTransferService.scheduledTransferProcess(it.id!!) }
        log.info("Pending transfers processed successfully")
    }
}
