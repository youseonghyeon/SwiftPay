package com.swiftpay.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class TimeTraceImpl {

    private val log = LoggerFactory.getLogger(TimeTraceImpl::class.java)

    @Pointcut("execution(* com.swiftpay.service..TransferValidator.*(..))")
    fun serviceMethods() {
    }

    @Around("serviceMethods()")
    fun calculateTime(joinPoint: ProceedingJoinPoint): Any? {
        return try {
            val start = System.currentTimeMillis()
            val result = joinPoint.proceed()
            val end = System.currentTimeMillis()
            val methodName = joinPoint.signature.name
            log.debug("$methodName execution time: ${end - start} ms")
            result
        } catch (e: Exception) {
            log.error("Error in TimeTraceImpl", e)
            throw e
        }
    }
}
