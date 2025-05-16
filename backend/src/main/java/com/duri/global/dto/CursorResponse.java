package com.duri.global.dto;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CursorResponse<T, C> {

    private final List<T> items;
    private final C nextCursor;
    private final boolean hasNext;

    public static <T, C> CursorResponse<T, C> of(List<T> items, C nextCursor, boolean hasNext) {
        return new CursorResponse<>(items, nextCursor, hasNext);
    }
}
