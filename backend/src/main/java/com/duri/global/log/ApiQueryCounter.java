package com.duri.global.log;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Getter
public class ApiQueryCounter {

    private static final ThreadLocal<String> currentService = new ThreadLocal<>();
    private final Map<String, Integer> serviceQueryCount = new HashMap<>();
    private int totalCount = 0; // 전체 쿼리 카운트

    public void setCurrentService(String serviceName) {
        currentService.set(serviceName);
    }

    public void clearCurrentService() {
        currentService.remove();
    }

    // 전체 + 서비스별 카운트 모두 증가
    public void increaseCount() {
        totalCount++;
        String serviceName = currentService.get();
        if (serviceName == null) {
            serviceName = "unknown";
        }
        serviceQueryCount.put(serviceName, serviceQueryCount.getOrDefault(serviceName, 0) + 1);
    }
    
}
