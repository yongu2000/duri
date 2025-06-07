package com.duri.global.aspect;

import com.duri.global.log.ApiQueryCounter;
import com.duri.global.log.ConnectionProxyHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class DataSourceAspect {

    private final ApiQueryCounter apiQueryCounter;

    public DataSourceAspect(final ApiQueryCounter apiQueryCounter) {
        this.apiQueryCounter = apiQueryCounter;
    }

    @Around("execution(* javax.sql.DataSource.getConnection())")
    public Object getConnection(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object connection = proceedingJoinPoint.proceed();
        return Proxy.newProxyInstance(
            connection.getClass().getClassLoader(),
            connection.getClass().getInterfaces(),
            new ConnectionProxyHandler(connection, apiQueryCounter)
        );
    }
}
