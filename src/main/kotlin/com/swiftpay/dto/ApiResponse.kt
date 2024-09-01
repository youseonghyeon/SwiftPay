package com.swiftpay.dto

data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val data: T? = null,
    val error: String? = null
) {
    companion object {
        fun <T> success(data: T, message: String = "Request successful", status: Int = 201): ApiResponse<T> {
            return ApiResponse(status = status, message = message, data = data)
        }
    }
}
