package com.swiftpay.service

import com.swiftpay.entity.Account
import com.swiftpay.util.TtlMap
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class DetectServiceTest {


    private fun createTestAccount(
        balance: BigDecimal,
        isAccountLocked: Boolean = false,
        transactionLimit: BigDecimal,
        dailyLimit: BigDecimal
    ): Account {
        val senderAccount = Account(
            id = 1,
            username = "testUsername",
            name = "testName",
            balance = balance,
            isAccountLocked = isAccountLocked,
            transactionLimit = transactionLimit,
            dailyLimit = dailyLimit
        )
        return senderAccount
    }

    @Test
    @DisplayName("사용자 송금 가능 상태 검증 성공 케이스")
    fun `check user transfer validation success`() {
        //given
        val senderAccount = createTestAccount(
            balance = BigDecimal(10000),
            transactionLimit = BigDecimal(2000),
            dailyLimit = BigDecimal(5000)
        )

        //when then
        // 원금 10000, 이체한도 2000, 일일한도 5000
        // 송금 2000, 금일 송금 금액 2000
        assertDoesNotThrow {
            senderAccount.validateTransferOrThrow(
                amount = BigDecimal(2000),
                dailyTransferAmount = BigDecimal(2000)
            )
        }
    }

    @Test
    @DisplayName("사용자 송금 가능 상태 검증 실패 케이스 - 이체 한도 초과")
    fun `check user transfer validation failure - exceeds transaction limit`() {
        //given
        val senderAccount = createTestAccount(
            balance = BigDecimal(10000),
            transactionLimit = BigDecimal(2000),
            dailyLimit = BigDecimal(5000)
        )

        //when
        // 원금 10000, 이체한도 2000, 일일한도 5000
        // 송금 3000
        val exception = assertThrows<IllegalStateException>() {
            senderAccount.validateTransferOrThrow(amount = BigDecimal(3000), dailyTransferAmount = BigDecimal(0))
        }
        //then
        assertEquals(
            "Transfer amount exceeds the transaction limit (account id: 1, amount: 3000, transactionLimit: 2000)",
            exception.message
        )
    }

    @Test
    @DisplayName("사용자 송금 가능 상태 검증 실패 케이스 - 일일 한도 초과")
    fun `check user transfer validation failure - exceeds daily limit`() {
        //given
        val senderAccount = createTestAccount(
            balance = BigDecimal(10000),
            transactionLimit = BigDecimal(2000),
            dailyLimit = BigDecimal(5000)
        )
        //when
        // 원금 10000, 이체한도 2000, 일일한도 5000
        // 송금 2000, 금일 송금 금액 4000
        val exception2 = assertThrows<IllegalStateException>() {
            senderAccount.validateTransferOrThrow(amount = BigDecimal(2000), dailyTransferAmount = BigDecimal(4000))
        }
        //then
        assertEquals(
            "Transfer amount exceeds the daily limit (account id: 1, amount: 2000, dailyTransferAmount: 4000, dailyLimit: 5000)",
            exception2.message
        )
    }

    @Test
    @DisplayName("빈번한 송금 시도 테스트")
    fun `check frequent transfers`() {
        //given
        val map = TtlMap(1L, TimeUnit.SECONDS)
        map.add(1, BigDecimal(1000))
        map.add(1, BigDecimal(2000))
        Thread.sleep(1500)
        map.add(1, BigDecimal(3000))

        //then
        assertEquals(BigDecimal(3000), map.get(1))

    }

    @Test
    @DisplayName("국가/지역 이체 제한 테스트")
    fun `check country restrictions`() {
        // maxMind GeoIP2 사용 시 작성
    }

    @Test
    @DisplayName("비정상적인 시간대 거래")
    fun `check abnormal time transfers`() {

    }

    @Test
    @DisplayName("비정상적인 IP 주소 거래")
    fun `check abnormal IP transfers`() {

    }

    @Test
    @DisplayName("블랙 리스트 계좌 거래")
    fun `check blacklisted accounts`() {

    }

    @Test
    @DisplayName("의심스러운 기기 사용")
    fun `check suspicious devices`() {

    }

    @Test
    @DisplayName("AI를 사용한 의심스러운 거래 패턴")
    fun `check suspicious transaction patterns using AI`() {

    }


}
