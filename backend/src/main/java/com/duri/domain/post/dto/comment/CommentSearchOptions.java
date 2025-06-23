package com.duri.domain.post.dto.comment;

import com.duri.domain.post.constant.search.CommentSortBy;
import com.duri.domain.post.constant.search.SortDirection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentSearchOptions {

    private CommentSortBy sortBy = CommentSortBy.CREATED_AT;
    private SortDirection sortDirection = SortDirection.DESC;

}
