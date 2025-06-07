package com.duri.global.log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:ss.SSS");

    private static final int QUERY_COUNT_WARNING_STANDARD = 10;
    private static final String QUERY_COUNT_WARNING_LOG_FORMAT = "쿼리가 {}번 이상 실행되었습니다.";
    private final ApiQueryCounter apiQueryCounter;

    public LoggingInterceptor(final ApiQueryCounter apiQueryCounter) {
        this.apiQueryCounter = apiQueryCounter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) {
        // 요청 시작 시간을 request에 저장
        request.setAttribute("startTime", System.currentTimeMillis());

        // 요청 정보 로깅
        log.info("[{}] [{}] {} {} started",
            request.getRemoteAddr(),
            LocalDateTime.now().format(formatter),
            request.getMethod(),
            request.getRequestURI()
        );

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
        ModelAndView modelAndView) {
        // 이 메서드는 컨트롤러 실행 후, 뷰 렌더링 전에 실행됩니다.
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
        Object handler,
        Exception ex) {
        // 요청 처리 완료 시간 계산
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        int queryCount = apiQueryCounter.getTotalCount();

        // 응답 정보 로깅
        log.info("[{}] {} {} completed - status: {}, time: {}ms | query_count: {}",
            LocalDateTime.now().format(formatter),
            request.getMethod(),
            request.getRequestURI(),
            response.getStatus(),
            executionTime,
            queryCount
        );

        Map<String, Integer> serviceQueryMap = apiQueryCounter.getServiceQueryCount();
        if (!serviceQueryMap.isEmpty()) {
            StringBuilder sb = new StringBuilder("\n[Service별 쿼리 카운트]");
            int i = 0;
            int size = serviceQueryMap.size();
            for (Map.Entry<String, Integer> entry : serviceQueryMap.entrySet()) {
                String prefix = (i == size - 1) ? "└─ " : "├─ ";
                sb.append(prefix)
                    .append(entry.getKey())
                    .append(" = ")
                    .append(entry.getValue())
                    .append("\n");
                i++;
            }
            log.info("\n{}", sb.toString());
        }

        if (queryCount >= QUERY_COUNT_WARNING_STANDARD) {
            log.warn(QUERY_COUNT_WARNING_LOG_FORMAT, QUERY_COUNT_WARNING_STANDARD);
        }

        if (ex != null) {
            log.error("Error occurred while processing request", ex);
        }
    }
}
