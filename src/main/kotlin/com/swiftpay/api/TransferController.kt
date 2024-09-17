package com.swiftpay.api

import com.swiftpay.dto.ApiResponse
import com.swiftpay.dto.ScheduleTransferRequest
import com.swiftpay.dto.TransferRequest
import com.swiftpay.service.ScheduledTransferService
import com.swiftpay.service.TransferService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/transfers")
class TransferController(
    private val transferService: TransferService,
    private val scheduledTransferService: ScheduledTransferService
) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/transfer")
    fun realTimeTransfer(@RequestBody request: TransferRequest): ResponseEntity<ApiResponse<Unit>> {
        log.info("Processing real-time transfer request: $request")
        transferService.transferMoney(request.senderId, request.recipientId, request.amount)

        val apiResponse = ApiResponse<Unit>(
            status = HttpStatus.OK.value(),
            message = "Transfer completed successfully"
        )

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse)
    }

    @PostMapping("/schedule-transfer")
    fun scheduleTransfer(@RequestBody request: ScheduleTransferRequest): ResponseEntity<ApiResponse<Unit>> {
        log.info("Processing scheduled transfer request: $request")
        scheduledTransferService.scheduleTransferEnroll(
            request.senderId,
            request.recipientId,
            request.amount,
            request.scheduleTime
        )

        val apiResponse = ApiResponse<Unit>(
            status = HttpStatus.OK.value(),
            message = "Scheduled transfer enroll completed successfully"
        )

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse)
    }

}
