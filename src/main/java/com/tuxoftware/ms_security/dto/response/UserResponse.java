package com.tuxoftware.ms_security.dto.response;

import java.util.List;

public record UserResponse(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        long createdTimestamp
) {}