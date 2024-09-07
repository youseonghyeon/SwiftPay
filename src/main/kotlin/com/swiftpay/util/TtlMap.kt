package com.swiftpay.util

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class TtlMap(private val ttl: Long, private val unit: TimeUnit) {

    // 키와 함께, (추가된 시간, 금액) 쌍을 리스트로 저장하는 Map
    private val map = mutableMapOf<Long, MutableList<Pair<LocalDateTime, BigDecimal>>>()

    // 현재 시간을 기준으로 만료 시간을 계산
    private fun calculateExpiration(): LocalDateTime {
        return LocalDateTime.now().plusNanos(unit.toNanos(ttl))
    }

    // 새로운 값 추가 (현재 시간과 함께 저장)
    fun add(key: Long, value: BigDecimal) {
        val expirationTime = calculateExpiration()
        val pair = Pair(expirationTime, value)
        map.computeIfAbsent(key) { mutableListOf() }.add(pair)
    }

    // 만료된 데이터를 자동으로 제거
    private fun removeExpiredEntries(key: Long) {
        val now = LocalDateTime.now()
        map[key]?.removeIf { it.first.isBefore(now) }
    }

    // 해당 키의 합계 값을 가져옴 (만료되지 않은 데이터만)
    fun get(key: Long): BigDecimal {
        removeExpiredEntries(key)
        val values = map[key] ?: return BigDecimal.ZERO
        return values.map { it.second }.fold(BigDecimal.ZERO, BigDecimal::add)
    }

    // 만료된 데이터를 전체적으로 정리하는 메서드 (옵션)
    fun cleanUp() {
        val now = LocalDateTime.now()
        map.forEach { (key, list) ->
            list.removeIf { it.first.isBefore(now) }
        }
    }
}
