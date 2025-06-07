package com.duri.global.aspect;

import com.duri.global.log.ApiQueryCounter;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

@Aspect
@Component
@RequiredArgsConstructor
public class ServiceMonitorAspect {

    private final ApiQueryCounter apiQueryCounter;


    @Around("execution(* com.duri.domain..service..*(..))")
    public Object aroundService(ProceedingJoinPoint joinPoint) throws Throwable {
        // RequestScope가 활성화되어 있을 때만 로직 실행
        try {
            RequestContextHolder.currentRequestAttributes();
            String methodSignature = joinPoint.getSignature().toShortString();
            apiQueryCounter.setCurrentService(methodSignature);
            try {
                return joinPoint.proceed();
            } finally {
                apiQueryCounter.clearCurrentService();
            }
        } catch (IllegalStateException e) {
            // RequestScope가 아니면 그냥 패스
            return joinPoint.proceed();
        }
    }
}
