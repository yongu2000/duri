package com.duri.domain.post.dto;

import com.duri.domain.post.constant.search.PostSortBy;
import com.duri.domain.post.constant.search.SortDirection;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostSearchOptions {

    private String searchKeyword;     // 검색어

    // 검색 기간
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;      // 시작일
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;        // 종료일

    // 정렬
    private PostSortBy sortBy = PostSortBy.DATE;   // 정렬 기준 (date, likes, comments)
    private SortDirection sortDirection = SortDirection.DESC; // 정렬 방향 (asc, desc)

}
