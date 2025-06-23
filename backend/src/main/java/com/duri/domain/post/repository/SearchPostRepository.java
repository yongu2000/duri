package com.duri.domain.post.repository;

import com.duri.domain.post.dto.PostCursor;
import com.duri.domain.post.dto.PostSearchOptions;
import com.duri.domain.post.entity.Post;
import java.util.List;

public interface SearchPostRepository {

    List<Post> findCoupleCompletePostsBySearchOptions(PostCursor cursor, int size,
        PostSearchOptions postSearchOptions);

    List<Post> findCoupleCompletePostsBySearchOptions(PostCursor cursor, int size,
        PostSearchOptions postSearchOptions, Long coupleId);

    List<Post> findCouplePendingPostsBySearchOptions(PostCursor cursor, int size,
        PostSearchOptions postSearchOptions, Long coupleId);


}
