package com.duri.domain.notification.aspect;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.notification.entity.Notification;
import com.duri.domain.notification.exception.UnauthorizedNotificationAccessException;
import com.duri.domain.notification.service.NotificationService;
import com.duri.domain.user.entity.Role;
import com.duri.domain.user.entity.User;
import com.duri.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class NotificationPermissionAspect {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private static Long getNotificationId(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();

        Long commentId = null;

        for (int i = 0; i < parameterNames.length; i++) {
            // 파라미터명이 "commentId"이고 타입이 Long인지 검사
            if ("notificationId".equals(parameterNames[i]) && args[i] instanceof Long) {
                commentId = (Long) args[i];
                break;
            }
        }
        if (commentId == null) {
            throw new IllegalArgumentException("notificationId(Long) 파라미터가 필요합니다.");
        }
        return commentId;
    }

    @Around("@annotation(CheckNotificationPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        Long notificationId = getNotificationId(joinPoint);
        Long userId = extractUserId();

        Notification notification = notificationService.findById(notificationId);

        if (!notification.getTo().getId().equals(userId) && !hasAdminRole(userId)) {
            throw new UnauthorizedNotificationAccessException();
        }

        return joinPoint.proceed();
    }

    private Long extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }

    private boolean hasAdminRole(Long userId) {
        return userRepository.findById(userId)
            .map(User::getRole)
            .filter(role -> role == Role.ADMIN)
            .isPresent();
    }
}
