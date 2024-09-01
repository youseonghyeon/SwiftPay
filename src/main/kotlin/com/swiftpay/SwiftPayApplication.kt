package com.swiftpay

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SwiftPayApplication

fun main(args: Array<String>) {
    runApplication<SwiftPayApplication>(*args)
}
