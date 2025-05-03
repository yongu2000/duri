package com.duri.global.log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:ss.SSS");

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

        // 응답 정보 로깅
        log.info("[{}] {} {} completed - status: {}, time: {}ms",
            LocalDateTime.now().format(formatter),
            request.getMethod(),
            request.getRequestURI(),
            response.getStatus(),
            executionTime
        );

        if (ex != null) {
            log.error("Error occurred while processing request", ex);
        }
    }
}
