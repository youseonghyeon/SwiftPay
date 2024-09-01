package com.swiftpay.api

import com.swiftpay.dto.ApiResponse
import com.swiftpay.dto.CreateAccountRequest
import com.swiftpay.dto.CreateAccountResponse
import com.swiftpay.entity.Account
import com.swiftpay.service.AccountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/accounts")
class AccountController(private val accountService: AccountService) {

    @PostMapping("/create")
    fun createAccount(@RequestBody request: CreateAccountRequest): ResponseEntity<ApiResponse<CreateAccountResponse>> {
        val account = accountService.createAccount(request.username, request.name, request.balance)

        val response = account.toCreateAccountResponse()
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    private fun Account.toCreateAccountResponse() = CreateAccountResponse(
        id = requireNotNull(this.id) { "Account ID must not be null after creation" },
        username = this.username,
        name = this.name,
        balance = this.balance
    )

}
