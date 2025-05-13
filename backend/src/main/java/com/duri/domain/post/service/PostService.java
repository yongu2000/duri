package com.duri.domain.post.service;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.couple.entity.Couple;
import com.duri.domain.couple.service.CoupleService;
import com.duri.domain.post.dto.PostCreateRequest;
import com.duri.domain.post.entity.Post;
import com.duri.domain.post.repository.PostRepository;
import com.duri.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class PostService {

    private final CoupleService coupleService;
    private final PostRepository postRepository;

    public Void create(CustomUserDetails userDetails, PostCreateRequest request) {
        User user = userDetails.getUser();
        String coupleCode = user.getCoupleCode();
        Couple couple = coupleService.findCoupleWithUsersByCode(coupleCode);
        Post post;
        if (couple.getUserLeft().getId().equals(user.getId())) {
            post = Post.builder()
                .title(request.getTitle())

                .placeName(request.getPlaceName())
                .placeUrl(request.getPlaceUrl())
                .category(request.getCategory())
                .phone(request.getPhone())
                .address(request.getAddress())
                .roadAddress(request.getRoadAddress())
                .x(request.getX())
                .y(request.getY())

                .date(request.getDate())
                .userLeftRate(request.getRate())
                .userLeftComment(request.getComment())
                .couple(couple)
                .scope(request.getScope())
                .build();

        } else {
            post = Post.builder()
                .title(request.getTitle())

                .placeName(request.getPlaceName())
                .placeUrl(request.getPlaceUrl())
                .category(request.getCategory())
                .phone(request.getPhone())
                .address(request.getAddress())
                .roadAddress(request.getRoadAddress())
                .x(request.getX())
                .y(request.getY())

                .date(request.getDate())
                .userRightRate(request.getRate())
                .userRightComment(request.getComment())
                .couple(couple)
                .scope(request.getScope())
                .build();
        }
        postRepository.save(post);
        // 커플에게 별점/한마디 작성 요청 알림 전송
        //

        return null;
    }
}
