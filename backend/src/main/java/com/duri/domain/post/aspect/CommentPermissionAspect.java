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

    @Around("@annotation(CheckCommentPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long commentId = (Long) args[0];
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
