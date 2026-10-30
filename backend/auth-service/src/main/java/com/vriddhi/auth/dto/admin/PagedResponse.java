package com.vriddhi.auth.dto.admin;

import java.util.List;

public record PagedResponse<T>(List<T> content, long totalElements, int page, int size, int totalPages) {
    public static <T> PagedResponse<T> of(List<T> content, long total, int page, int size) {
        int pages = size <= 0 ? 0 : (int) Math.ceil(total / (double) size);
        return new PagedResponse<>(content, total, page, size, pages);
    }
}
