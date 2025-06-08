package com.duri.domain.post.aspect;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.couple.entity.Couple;
import com.duri.domain.couple.service.CoupleService;
import com.duri.domain.post.entity.Comment;
import com.duri.domain.post.exception.UnauthorizedCommentAccessException;
import com.duri.domain.post.service.CommentService;
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
public class CommentPermissionAspect {

    private final CommentService commentService;
    private final CoupleService coupleService;
    private final UserRepository userRepository;

    private static Long getCommentId(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();

        Long commentId = null;

        for (int i = 0; i < parameterNames.length; i++) {
            // 파라미터명이 "commentId"이고 타입이 Long인지 검사
            if ("commentId".equals(parameterNames[i]) && args[i] instanceof Long) {
                commentId = (Long) args[i];
                break;
            }
        }
        if (commentId == null) {
            throw new IllegalArgumentException("commentId(Long) 파라미터가 필요합니다.");
        }
        return commentId;
    }

    @Around("@annotation(CheckCommentPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        Long commentId = getCommentId(joinPoint);
        Long userId = extractUserId();
        String coupleCode = extractCoupleCode(); // SecurityContext에서 꺼내기

        Comment comment = commentService.findById(commentId);
        Couple couple = coupleService.findByCode(coupleCode);

        if (!comment.getCouple().getId().equals(couple.getId()) && !hasAdminRole(userId)) {
            throw new UnauthorizedCommentAccessException();
        }

        return joinPoint.proceed();
    }

    private Long extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }

    private String extractCoupleCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getCoupleCode();
    }

    private boolean hasAdminRole(Long userId) {
        return userRepository.findById(userId)
            .map(User::getRole)
            .filter(role -> role == Role.ADMIN)
            .isPresent();
    }
}
