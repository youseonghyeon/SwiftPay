# 이상거래 감지 서비스

이상거래 감지 서비스는 계좌 송금 중 발생할 수 있는 비정상적인 거래를 실시간으로 분석하여 차단하는 시스템입니다. 이 서비스는 송금 요청이 들어올 때마다 거래 내역과 패턴을 분석하고, 설정된 규칙에 따라 비정상 거래 여부를 판단합니다. 송금자는 송금 금액이나 요청 횟수를 초과할 경우 거래가 제한됩니다.

## 서비스 개요

- **거래 검증**: 과거 거래 패턴을 분석하여 비정상적인 패턴을 감지합니다.
- **비정상 요청 차단**: 송금 횟수나 금액을 초과할 경우 거래를 차단하고 영구 제한 조치를 합니다.
- **실시간 모니터링**: Prometheus와 Grafana를 사용하여 시스템 상태와 거래 패턴을 실시간 모니터링합니다. 이상 징후가 발생하면 즉각적으로 대응할 수 있습니다.

## 서비스 아키텍처

EC2 인스턴스에서 Docker compose 를 사용해 애플리케이션과 MySQL을 구동하고 있으며, GitHub Actions를 통해 CI/CD를 자동화했습니다. 또한, 로컬 환경에서 Prometheus와 Grafana를 사용해 시스템 상태를 모니터링하고 있습니다.

<img width="1243" alt="스크린샷 2024-09-08 오후 10 06 40" src="https://github.com/user-attachments/assets/107136cf-f82d-4b4c-a252-9dc77c5f4662">



# 1.성능 테스트 결과 - 단순 API 요청
**2024년 9월 1일**

120명의 가상 유저(VUs)가 30초 동안 API 엔드포인트에 요청을 보낸 성능 테스트 결과입니다.

## 테스트 환경

- **테스트 도구**: k6
- **서버 인스턴스**: AWS EC2 t2.small (1 vCPU, 2GB RAM)
- **테스트 대상**: `/api/transfers/transfer` 엔드포인트
- **테스트 설정**:
  - 가상 유저 수(VUs): 120명
  - 테스트 지속 시간(Duration): 30초
- **서비스 설명**:
  - 트랜잭션 1
    1. 송금/수신 가능 사용자 상태 조회
    2. 송금 (송금자, 수신자 금액 변경)
    3. 송금 히스토리 저장

## 테스트 결과

| 메트릭                         | 값                            |
| ----------------------------- | ----------------------------- |
| **총 요청 수**                 | 3906 요청                     |
| **성공률**                     | 100% (3906/3906)              |
| **평균 응답 시간**             | 934.4ms                       |
| **최소 응답 시간**             | 125.92ms                      |
| **최대 응답 시간**             | 2.16s                         |
| **90번째 백분위 응답 시간**    | 1.04s                         |
| **95번째 백분위 응답 시간**    | 1.11s                         |
| **데이터 전송량**              | 수신: 812kB, 전송: 789kB      |
| **초당 요청 수**               | 126.64 요청/초                |

<img width="2298" alt="image" src="https://github.com/user-attachments/assets/4e263367-01b5-4676-9a46-7feaf105697a">


## 상세 분석

- **성공률**: 모든 요청이 정상적으로 처리되었습니다.
- **응답 시간**: 평균 응답 시간은 934.4ms로 나타났습니다. 응답 시간이 1초를 넘는 경우도 있었지만, 대다수의 요청은 1.11초 이내에 처리되었습니다.
- **최대 응답 시간**: 2.16초로 가장 오래 걸린 요청의 응답 시간은 상대적으로 길었으나, 이는 극히 일부의 케이스였습니다.
- **초당 요청 수**: 초당 약 126건의 요청이 처리되었습니다. 120명의 가상 유저가 지속적으로 요청을 보내는 상황에서도 서버는 안정적인 성능을 유지했습니다.

## 결론

안정적인 성공률과 응답속도를 보였습니다.

---
# 2. 성능 테스트 결과 - 700명 30초 동안의 단순 API 요청
**2024년 9월 1일**

700명의 가상 유저(VUs)가 30초 동안 API 엔드포인트에 요청을 보낸 성능 테스트 결과입니다.<br>
1번 테스트보다 높은 부하를 가정하여 수행되었습니다.

## 테스트 환경

- **테스트 도구**: k6
- **서버 인스턴스**: AWS EC2 t2.small (1 vCPU, 2GB RAM)
- **테스트 대상**: `/api/transfers/transfer` 엔드포인트
- **테스트 설정**:
  - 가상 유저 수(VUs): 700명
  - 테스트 지속 시간(Duration): 30초
- **서비스 설명**:
  - 트랜잭션 1
    1. 송금/수신 가능 사용자 상태 조회
    2. 송금 (송금자, 수신자 금액 변경)
    3. 송금 히스토리 저장

## 테스트 결과

| 메트릭                         | 값                            |
| ----------------------------- | ----------------------------- |
| **총 요청 수**                 | 6009 요청                     |
| **성공률**                     | 100% (6009/6009)              |
| **평균 응답 시간**             | 3.73초                        |
| **최소 응답 시간**             | 85.75ms                       |
| **최대 응답 시간**             | 7.91초                        |
| **90번째 백분위 응답 시간**    | 4.19초                        |
| **95번째 백분위 응답 시간**    | 4.29초                        |
| **데이터 전송량**              | 수신: 1.2MB, 전송: 1.2MB      |
| **초당 요청 수**               | 176.40 요청/초                |

## 상세 분석

- **성공률**: 700명의 가상 유저가 동시에 요청을 보냈음에도 불구하고, 모든 요청이 성공적으로 처리되어 100%의 성공률을 기록했습니다.
- **응답 시간**: 평균 응답 시간이 3.73초로 나타났습니다. 응답 시간이 4초를 넘는 경우가 많았으며, 최대 응답 시간은 7.91초로 나타났습니다. 이는 서버가 높은 부하 상황에서 응답 속도가 저하될 수 있음을 시사합니다.
  - 만약 사용자의 요청이 현재 상태로 유지된다면 서버의 리소스를 추가해야 합니다.
  
---

# 3. 검증 기능 추가
**2024년 9월 7일**

## 추가된 기능
- **일시적 거래 제한**
  - 계정별 설정된 일일 송금 한도를 초과하는 경우 거래가 제한되며, 익일이 되어야 다시 거래가 가능합니다.
- **이상거래 감지**:
  - 서비스에 설정된 시간단위 송금 요청 건수를 초과하는 경우, 이상거래로 간주되어 거래가 영구적으로 제한됩니다.
  - 서비스에 설정된 시간단위 송금 금액을 초과하는 경우, 이상거래로 간주되어 거래가 영구적으로 제한됩니다.
- **구현 방법**
  - DB를 이용해 구현 시 빈번한 I/O에 대한 성능감소를 우려해 TTL이 적용된 Map을 만들어 기능을 구현하였습니다.
```kotlin
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
}
```


## 테스트 결과
<img width="753" alt="스크린샷 2024-09-07 오후 8 55 58" src="https://github.com/user-attachments/assets/68d151bf-ea67-435d-b49e-a730197778ca">

## 상세 분석

- **성공률**: 10건(2%) 성공 448건(98%) 실패, 1초단 요청량 10회 초과로 계정 영구 제한
- **로그**: 2024-09-07T12:07:29.368Z ERROR 1 --- [nio-8080-exec-8] c.swiftpay.api.GlobalExceptionHandler    : An unexpected error occurred: Account 1 has been blocked due to abnormal activity. (Max request count exceeded)

---
# 4. 검증 기능을 포함한 성능 테스트 결과
**2024년 9월 7일**

| 메트릭                         | 값                            |
| ----------------------------- | ----------------------------- |
| **총 요청 수**                 | 1153 요청                     |
| **성공률**                     | 97.91% (1129/1153)            |
| **평균 응답 시간**             | 3.26s                         |
| **최소 응답 시간**             | 165.89ms                      |
| **최대 응답 시간**             | 6.64s                         |
| **90번째 백분위 응답 시간**    | 3.82s                         |
| **95번째 백분위 응답 시간**    | 3.91s                         |
| **데이터 전송량**              | 수신: 240kB, 전송: 235kB      |
| **초당 요청 수**               | 35.03 요청/초                 |

## 테스트 환경
- **테스트 도구**: k6
- **서버 인스턴스**: AWS EC2 t2.small (1 vCPU, 2GB RAM)
- **테스트 대상**: `/api/transfers/transfer` 엔드포인트
- **테스트 설정**:
  - 가상 유저 수(VUs): 120명
  - 서비스 계정 수: 986명
  - 테스트 지속 시간(Duration): 30초

## 상세 분석
- **성공률**: 98%, 24건이 실패하였으며 user를 못찾는경우, 거래가 너무 빈번하여 영구 제한된 경우가 여기 포함되었습니다.
- **응답 시간**: 첫번째 테스트였던 단순 송금 테스트 평균 응답시간 934ms에서 3.26s로 큰폭으로 증가하였습니다.
  - 이는 이상거래 감지 및 일시적 거래 제한 기능이 추가되어, 서버에서 추가적인 로직을 수행하고 있기 때문이며, 3초 이상의 응답시간은 사용자에게 불편을 줄 수 도 있습니다.

---
# 5. 트러블 슈팅
**2024년 9월 7일**
## `@Transactional` 유지로 인한 repository.save() 호출 후 롤백 문제

### 문제 설명
`@Transactional`로 정의된 `save` 메서드에서 엔티티를 저장한 후 예외를 발생시킬 때, 저장된 데이터가 커밋되지 않는 문제가 발생했습니다. 이는 예외가 트랜잭션 내부에서 발생하기 때문에 트랜잭션이 롤백되고, 데이터베이스에 저장된 내용이 유지되지 않았습니다.

### 문제 원인
기본적으로, `@Transactional` 어노테이션은 **런타임 예외**나 **체크되지 않은 예외(Unchecked Exception)**가 발생하면 **자동으로 롤백**을 수행합니다. 즉, `save` 메서드에서 엔티티를 저장한 후 예외가 발생하면 트랜잭션이 롤백되며, 저장된 데이터가 커밋되지 않습니다.

### 해결 방법
강제로 중간 커밋을 하기 위해 `@Transactional(propagation = Propagation.REQUIRES_NEW)`를 사용하여 **새로운 트랜잭션**에서 데이터를 저장하고, 저장 후 바로 **강제 커밋**되도록 하였습니다. 이를 통해 예외가 발생하더라도 트랜잭션 내에서 커밋이 정상적으로 이루어집니다.

### 해결 코드 예시

### 수정 전 (에러로 인한 롤백 발생)
```kotlin
    @Transactional
    open fun transferMoney(senderId: Long, recipientId: Long, sendAmount: BigDecimal) {
        ...
         if (RequestTracker.getRequestTimes(senderAccount.id!!).size > blockAttemptCount) {
            ...
            accountService.save(senderAccount)
            throw IllegalStateException("Account ${senderAccount.id} has been blocked due to abnormal activity. (Max request count exceeded)")
        }
    }

    fun save(senderAccount: Account) {
        accountRepository.save(senderAccount)
    }
```
### 수정 후 (정상 커밋)
```kotlin
    @Transactional
    open fun transferMoney(senderId: Long, recipientId: Long, sendAmount: BigDecimal) {
        ...
        if (RequestTracker.getRequestTimes(senderAccount.id!!).size > blockAttemptCount) {
            ...
            // 강제 커밋이므로 주의 필요
            accountService.saveAccountWithNewTransaction(senderAccount)
        throw IllegalStateException("Account ${senderAccount.id} has been blocked due to abnormal activity. (Max request count exceeded)")
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveAccountWithNewTransaction(senderAccount: Account) { 
        accountRepository.save(senderAccount)
    }
```

Transactional에 대한 추가적인 내용은 [유성현 개인블로그 - @Transactional 방식과 트러블 슈팅](https://youseong.tistory.com/71) 에서 확인할 수 있습니다.

---
# 6. 서비스 성능 개선
**2024년 9월 8일**

## Method Execution Statistics

| method_name                 | count | average_time(ms) | 비고                         |
|-----------------------------|-------|------------------|------------------------------|
| calculateDailyTransferAmount | 100   | 42.5             | 사용자 일일 송금 금액 조회     |
| validateAccounts             | 100   | 0.25             | 사용자 일일 송금 금액 검증     |
| checkForAbnormalRequests     | 100   | 2.5              | 시간단위 요청 횟수 초과 검증   |
| validateFrequentTransfers    | 100   | 0.75             | 시간단위 송금 금액 초과 검증   |

## 테스트 환경
- **db 데이터 적재량**: 45000건
- **조회 쿼리**: JPQL(SELECT SUM(th.amount) FROM TransferHistory th WHERE th.senderId = :senderId AND th.transferDate BETWEEN :transferDate AND :transferDate2)

## 개선 목표
- db select 쿼리 최적화가 필요합니다.

## 개선 방안
- **index 설정**: 조건문 transferDate 에 대한 index 를 설정하여 조회 성능을 향상시켰습니다.
- 추가로 이미 원하는 성능이 나왔기 때문에 senderId 는 index로 설정하지 않았으며, 옵티마이저를 고려하여 쿼리의 where 순서를 변경하지 않았습니다.

## 개선 결과

| method_name                 | count | average_time(ms) | 비고                         |
|-----------------------------|-------|------------------|------------------------------|
| calculateDailyTransferAmount | 100   | 5.8              | 사용자 일일 송금 금액 조회     |
| validateAccounts             | 100   | 0.25             | 사용자 일일 송금 금액 검증     |
| checkForAbnormalRequests     | 100   | 2.6              | 시간단위 요청 횟수 초과 검증   |
| validateFrequentTransfers    | 100   | 0.65             | 시간단위 송금 금액 초과 검증   |

---
# 7. 쿼리 최적화 후 성능 테스트 결과
**2024년 9월 8일** (tag v2.3)

| 메트릭                         | 값                    |
| ----------------------------- |----------------------|
| **총 요청 수**                 | 1153 -> 2009 요청      |
| **성공률**                     | 97.01% (1949/2009)   |
| **평균 응답 시간**             | 3.26 -> 1.84s        |
| **최소 응답 시간**             | 165.89 -> 29.44ms    |
| **최대 응답 시간**             | 6.64 -> 21.57s       |
| **90번째 백분위 응답 시간**    | 3.82 -> 1.92s        |
| **95번째 백분위 응답 시간**    | 3.91 -> 1.98s        |
| **데이터 전송량**              | 수신: 414kB, 전송: 411kB |
| **초당 요청 수**               | 35.03 -> 63.21 요청/초  |

## 테스트 환경
- **테스트 도구**: k6
- **서버 인스턴스**: AWS EC2 t2.small (1 vCPU, 2GB RAM)
- **테스트 대상**: `/api/transfers/transfer` 엔드포인트
- **테스트 설정**:
  - 가상 유저 수(VUs): 120명
  - 서비스 계정 수: 986명
  - 테스트 지속 시간(Duration): 30초

## 상세 분석

- **성공률**: 97%, 실패 60건에 대해서는 존재하지 않는 유저에 대한 송금으로 성능 또는 리소스에 대한 문제는 없었습니다.
- **응답 시간**: 평균 응답시간이 3.26s에서 1.84s로 대폭 개선되었습니다.
  - 이는 쿼리 최적화로 인해 성능이 개선되었으며, 사용자에게 더 나은 서비스를 제공할 수 있게 되었습니다.

CPU 사용량은 100%를 유지하였습니다.
<img width="1531" alt="스크린샷 2024-09-08 오후 9 30 35" src="https://github.com/user-attachments/assets/4f3b30c3-99bf-4929-8834-761889f6ae89">

---
