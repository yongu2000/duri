package com.duri.domain.api.search.controller;

import com.duri.domain.api.search.dto.SearchApiResponse;
import com.duri.domain.api.search.service.SearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<List<SearchApiResponse>> getSearchResults(
        @RequestParam String keyword
    ) {
        return ResponseEntity.ok(searchService.search(keyword));
    }
}
