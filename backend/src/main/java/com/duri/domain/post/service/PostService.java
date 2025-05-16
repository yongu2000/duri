package com.duri.domain.post.service;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.couple.entity.Couple;
import com.duri.domain.couple.service.CoupleService;
import com.duri.domain.image.entity.Image;
import com.duri.domain.image.service.ImageService;
import com.duri.domain.post.constant.PostStatus;
import com.duri.domain.post.dto.CompletePostResponse;
import com.duri.domain.post.dto.PostCreateRequest;
import com.duri.domain.post.dto.PostCursor;
import com.duri.domain.post.dto.PostImageRequest;
import com.duri.domain.post.dto.PostImageUrlResponse;
import com.duri.domain.post.dto.PostSearchOptions;
import com.duri.domain.post.entity.Post;
import com.duri.domain.post.entity.Post.PostBuilder;
import com.duri.domain.post.repository.PostRepository;
import com.duri.domain.user.entity.User;
import com.duri.global.dto.CursorResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class PostService {

    private final CoupleService coupleService;
    private final PostRepository postRepository;
    private final ImageService imageService;

    public Void create(CustomUserDetails userDetails, PostCreateRequest request) {
        User user = userDetails.getUser();
        Couple couple = coupleService.findCoupleWithUsersByCode(user.getCoupleCode());

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

        if (couple.getUserLeft().getId().equals(user.getId())) {
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

        request.getImageUrls().forEach(imageUrl -> {
            Image image = imageService.findByUrl(imageUrl);
            image.setPost(post);
        });

        // 커플에게 별점/한마디 작성 요청 알림 전송
        //

        return null;
    }

    @Transactional(readOnly = true)
    public CursorResponse<CompletePostResponse, PostCursor> getAllPostsWithSearchOptionsToCursor(
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
        return new CursorResponse<>(posts.stream().map(CompletePostResponse::from).toList(),
            nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public CursorResponse<CompletePostResponse, PostCursor> getAllPostsWithSearchOptionsToCursor(
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
        return new CursorResponse<>(posts.stream().map(CompletePostResponse::from).toList(),
            nextCursor, hasNext);
    }

    public List<PostImageUrlResponse> getPostImages(PostImageRequest request) {
        return imageService.findByPostId(request.getPostId()).stream()
            .map(PostImageUrlResponse::from).toList();
    }
}
