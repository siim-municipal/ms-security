package com.tuxoftware.ms_security.dto;

import java.util.List;
import java.util.Map;

public record UserProfileDTO(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        Map<String, List<String>> attributes // Para datos extra (ej. numero_empleado)
) {}