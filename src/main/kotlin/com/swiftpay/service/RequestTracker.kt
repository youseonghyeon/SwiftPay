package com.swiftpay.service

import java.util.*
import java.util.concurrent.ConcurrentHashMap


object RequestTracker {

    private val requestMap = ConcurrentHashMap<Long, MutableList<Long>>()

    fun logRequest(accountId: Long) {
        val currentTime = System.currentTimeMillis()
        requestMap.computeIfAbsent(accountId) { k: Long? -> Collections.synchronizedList(ArrayList()) }.add(currentTime)
    }

    fun getRequestTimes(accountId: Long?): List<Long> {
        return requestMap.getOrDefault(accountId, emptyList())
    }

    fun clearOldRequests(accountId: Long) {
        val oneSecondAgo = System.currentTimeMillis() - 1000
        val requests = requestMap[accountId]
        requests?.removeIf { timestamp: Long -> timestamp < oneSecondAgo }
    }
}
