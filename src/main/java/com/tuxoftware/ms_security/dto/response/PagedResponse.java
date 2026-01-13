package com.tuxoftware.ms_security.dto.response;

import java.util.List;

// Wrapper genérico para paginación
public record PagedResponse<T>(
        List<T> content,
        long totalElements,
        int page,
        int size
) {}