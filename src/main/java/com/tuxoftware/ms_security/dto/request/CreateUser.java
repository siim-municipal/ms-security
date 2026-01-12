package com.tuxoftware.ms_security.dto.request;

import java.util.List;

public record CreateUser(
        String username,
        String email,
        String firstName,
        String lastName,
        String password,
        List<String> roles
) {}