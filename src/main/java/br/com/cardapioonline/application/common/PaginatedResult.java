package br.com.cardapioonline.application.common;

import java.util.List;

public record PaginatedResult<T>(
        List<T> items,
        int page,
        int pageSize,
        long total,
        int totalPages,
        boolean hasPreviousPage,
        boolean hasNextPage
) {
    public static <T> PaginatedResult<T> of(List<T> items, int page, int pageSize, long total) {
        int pages = pageSize <= 0 ? 0 : (int) Math.ceil(total / (double) pageSize);
        return new PaginatedResult<>(items, page, pageSize, total, pages, page > 1, page < pages);
    }
}
