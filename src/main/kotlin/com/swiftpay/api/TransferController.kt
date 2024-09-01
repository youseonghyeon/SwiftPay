package com.swiftpay.api

import com.swiftpay.dto.ScheduleTransferRequest
import com.swiftpay.dto.TransferRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/transfers")
class TransferController {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/transfer")
    fun realTimeTransfer(@RequestBody request: TransferRequest) {
        log.info("Processing real-time transfer request: $request")
        throw NotImplementedError("Real-time transfers are not yet supported")
    }

    @PostMapping("/schedule-transfer")
    fun scheduleTransfer(@RequestBody request: ScheduleTransferRequest) {
        log.info("Processing scheduled transfer request: $request")
        throw NotImplementedError("Scheduled transfers are not yet supported")
    }


}
