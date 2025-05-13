package com.duri.domain.api.search.service;

import com.duri.domain.api.search.dto.KakaoSearchResponse;
import com.duri.domain.api.search.dto.SearchApiResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@RequiredArgsConstructor
@Service
@Transactional
public class KakaoSearchService implements SearchService {

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    @Override
    public List<SearchApiResponse> search(String keyword) {
        String url = UriComponentsBuilder
            .fromUriString("https://dapi.kakao.com/v2/local/search/keyword.json")
            .queryParam("query", keyword)
            .queryParam("size", 8)
            .build()
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<KakaoSearchResponse> response = restTemplate.exchange(
            url, HttpMethod.GET, request, KakaoSearchResponse.class
        );

        Set<String> seenNames = new LinkedHashSet<>();

        return Objects.requireNonNull(response.getBody()).getDocuments().stream()
            .filter(doc -> seenNames.add(doc.getPlaceName()))
            .map(doc -> new SearchApiResponse(
                doc.getPlaceName(),
                doc.getPlaceUrl(),
                doc.getCategoryName(),
                doc.getAddressName(),
                doc.getRoadAddressName(),
                doc.getPhone(),
                doc.getX(),
                doc.getY()
            ))
            .collect(Collectors.toList());
    }
}
