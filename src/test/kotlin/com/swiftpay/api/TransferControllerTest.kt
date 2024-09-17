package com.swiftpay.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.swiftpay.dto.TransferRequest
import com.swiftpay.service.ScheduledTransferService
import com.swiftpay.service.TransferService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

@WebMvcTest(TransferController::class)
class TransferControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var transferService: TransferService

    @MockBean
    private lateinit var scheduledTransferService: ScheduledTransferService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should process real-time transfer successfully`() {
        val request = TransferRequest(
            senderId = 1L,
            recipientId = 2L,
            amount = BigDecimal(1000)
        )

        Mockito.doNothing().`when`(transferService).transferMoney(request.senderId, request.recipientId, request.amount)

        mockMvc.perform(
            post("/api/transfers/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
    }

}
