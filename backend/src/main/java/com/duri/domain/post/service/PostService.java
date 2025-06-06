package com.duri.domain.post.service;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.couple.entity.Couple;
import com.duri.domain.couple.service.CoupleService;
import com.duri.domain.image.entity.Image;
import com.duri.domain.image.service.ImageService;
import com.duri.domain.notification.constant.NotificationType;
import com.duri.domain.notification.entity.Notification;
import com.duri.domain.notification.service.NotificationService;
import com.duri.domain.post.constant.PostStatus;
import com.duri.domain.post.dto.PendingPostCountResponse;
import com.duri.domain.post.dto.PostCreateRequest;
import com.duri.domain.post.dto.PostCursor;
import com.duri.domain.post.dto.PostEditRequest;
import com.duri.domain.post.dto.PostIdToken;
import com.duri.domain.post.dto.PostImageUrlResponse;
import com.duri.domain.post.dto.PostResponse;
import com.duri.domain.post.dto.PostSearchOptions;
import com.duri.domain.post.entity.Post;
import com.duri.domain.post.entity.Post.PostBuilder;
import com.duri.domain.post.exception.PostNotFoundException;
import com.duri.domain.post.repository.PostRepository;
import com.duri.domain.user.entity.Position;
import com.duri.domain.user.entity.User;
import com.duri.global.dto.CursorResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class PostService {

    private final CoupleService coupleService;
    private final PostStatService postStatService;
    private final PostRepository postRepository;
    private final ImageService imageService;
    private final NotificationService notificationService;

    public void create(CustomUserDetails userDetails, PostCreateRequest request) {
        User user = userDetails.getUser();
        Couple couple = coupleService.findCoupleWithUsersByCode(user.getCoupleCode());
        boolean isLeftUser = user.getPosition() == Position.LEFT;
        PostBuilder postBuilder = Post.builder()
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

            .couple(couple)
            .scope(request.getScope())
            .status(PostStatus.PENDING);

        if (isLeftUser) {
            postBuilder
                .userLeftRate(request.getRate())
                .userLeftComment(request.getComment());
        } else {
            postBuilder
                .userRightRate(request.getRate())
                .userRightComment(request.getComment());
        }
        Post post = postBuilder.build();
        
        postRepository.save(post);
        postStatService.create(post);

        setPostImages(request.getImageUrls(), post);

        // 커플에게 별점/한마디 작성 요청 알림 전송
        Notification notification = Notification.builder()
            .type(NotificationType.POST)
            .confirmed(false)
            .to(isLeftUser ? couple.getUserRight() : couple.getUserLeft())
            .from(isLeftUser ? couple.getUserLeft() : couple.getUserRight())
            .content(post.getTitle() + "게시글 작성을 완료해주세요")
            .build();

        notificationService.send(notification);
        //

    }


    public void edit(CustomUserDetails userDetails, PostEditRequest request) {
        Post post = postRepository.findById(request.getPostId())
            .orElseThrow(PostNotFoundException::new);

        updateBasicFields(post, request);
        updateUserSideFields(post, request, userDetails.getUser().getPosition());

        if (post.getUserLeftRate() != null &&
            post.getUserLeftComment() != null &&
            post.getUserRightRate() != null &&
            post.getUserRightComment() != null) {

            double avgRate = (post.getUserLeftRate() + post.getUserRightRate()) / 2.0;
            double roundedRate = Math.round(avgRate * 10) / 10.0;
            post.changeRate(roundedRate);
            post.changeStatus(PostStatus.COMPLETE);
        }

        setPostImages(request.getImageUrls(), post);

    }

    @Transactional(readOnly = true)
    public CursorResponse<PostResponse, PostCursor> getAllPostsWithSearchOptionsToCursor(
        PostCursor cursor,
        int size,
        PostSearchOptions searchOptions) {
        // 커서 기반 조회
        List<Post> posts = postRepository.findCompletePostsBySearchOptions(cursor, size + 1,
            searchOptions);

        // hasNext 확인을 위해 size + 1개를 조회했으므로, 실제 응답에는 size개만 포함
        boolean hasNext = posts.size() > size;
        if (hasNext) {
            posts = posts.subList(0, size);
        }

        PostCursor nextCursor = hasNext && !posts.isEmpty()
            ? PostCursor.from(posts.getLast())
            : null;

        // 다음 커서는 마지막 게시글의 ID
        return new CursorResponse<>(posts.stream().map(PostResponse::from).toList(),
            nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public CursorResponse<PostResponse, PostCursor> getAllPostsWithSearchOptionsToCursor(
        PostCursor cursor,
        int size,
        PostSearchOptions searchOptions,
        String coupleCode) {
        // 커서 기반 조회
        List<Post> posts = postRepository.findCompletePostsBySearchOptions(cursor, size + 1,
            searchOptions, coupleCode);

        // hasNext 확인을 위해 size + 1개를 조회했으므로, 실제 응답에는 size개만 포함
        boolean hasNext = posts.size() > size;
        if (hasNext) {
            posts = posts.subList(0, size);
        }

        PostCursor nextCursor = hasNext && !posts.isEmpty()
            ? PostCursor.from(posts.getLast())
            : null;

        // 다음 커서는 마지막 게시글의 ID
        return new CursorResponse<>(posts.stream().map(PostResponse::from).toList(),
            nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public CursorResponse<PostResponse, PostCursor> getPendingPostsWithSearchOptionsToCursor(
        PostCursor cursor,
        int size,
        PostSearchOptions searchOptions,
        String coupleCode) {
        // 커서 기반 조회
        List<Post> posts = postRepository.findPendingPostsBySearchOptions(cursor, size + 1,
            searchOptions, coupleCode);

        // hasNext 확인을 위해 size + 1개를 조회했으므로, 실제 응답에는 size개만 포함
        boolean hasNext = posts.size() > size;
        if (hasNext) {
            posts = posts.subList(0, size);
        }

        PostCursor nextCursor = hasNext && !posts.isEmpty()
            ? PostCursor.from(posts.getLast())
            : null;

        // 다음 커서는 마지막 게시글의 ID
        return new CursorResponse<>(posts.stream().map(PostResponse::from).toList(),
            nextCursor, hasNext);
    }


    @Transactional(readOnly = true)
    public List<PostImageUrlResponse> getPostImages(PostIdToken token) {
        return imageService.findByPostId(token.getPostId()).stream()
            .map(PostImageUrlResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PendingPostCountResponse getPendingPostCount(String coupleCode) {
        return PendingPostCountResponse.from(
            postRepository.countByCodeAndStatus(coupleCode, PostStatus.PENDING));
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(PostIdToken token) {
        return PostResponse.from(postRepository.findById(token.getPostId())
            .orElseThrow(PostNotFoundException::new));
    }

    private void setPostImages(List<String> imageUrls, Post post) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        imageUrls.forEach(imageUrl -> {
            Image image = imageService.findByUrl(imageUrl);
            image.setPost(post);
        });
    }

    private void updateBasicFields(Post post, PostEditRequest request) {
        if (request.getTitle() != null) {
            post.changeTitle(request.getTitle());
        }
        if (request.getPlaceName() != null) {
            post.changePlaceName(request.getPlaceName());
        }
        if (request.getPlaceUrl() != null) {
            post.changePlaceUrl(request.getPlaceUrl());
        }
        if (request.getCategory() != null) {
            post.changeCategory(request.getCategory());
        }
        if (request.getPhone() != null) {
            post.changePhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            post.changeAddress(request.getAddress());
        }
        if (request.getRoadAddress() != null) {
            post.changeRoadAddress(request.getRoadAddress());
        }
        if (request.getX() != null && request.getY() != null) {
            post.changeCoordinate(request.getX(), request.getY());
        }
        if (request.getDate() != null) {
            post.changeDate(request.getDate());
        }
        if (request.getScope() != null) {
            post.changeScope(request.getScope());
        }
    }

    private void updateUserSideFields(Post post, PostEditRequest request, Position position) {
        boolean isLeftUser = position == Position.LEFT;
        if (request.getRate() != null) {
            if (isLeftUser) {
                post.changeUserLeftRate(request.getRate());
            } else {
                post.changeUserRightRate(request.getRate());
            }
        }

        if (request.getComment() != null) {
            if (isLeftUser) {
                post.changeUserLeftComment(request.getComment());
            } else {
                post.changeUserRightComment(request.getComment());
            }
        }
    }

}
