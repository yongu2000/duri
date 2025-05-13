package com.duri.domain.api.search.service;

import com.duri.domain.api.search.dto.SearchApiResponse;
import java.util.List;

public interface SearchService {

    List<SearchApiResponse> search(String keyword);
}
