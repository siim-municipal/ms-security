package com.tuxoftware.ms_security.config;

import io.micrometer.common.lang.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Traduce el token de Keycloak a Roles de Spring Security.
 * Keycloak guarda roles en: "realm_access": { "roles": ["admin", "cajero"] }
 * Spring los necesita como: GrantedAuthority("ROLE_ADMIN"), GrantedAuthority("ROLE_CAJERO")
 */
public class KeycloakJwtConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String resourceId;

    public KeycloakJwtConverter(String resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        // 1. Roles de Realm (Nivel Global)
        // Ejemplo: ROLE_TESORERO
        var realmAccess = (Map<String, Object>) jwt.getClaims().getOrDefault("realm_access", Collections.emptyMap());
        var realmRoles = (Collection<String>) realmAccess.getOrDefault("roles", Collections.emptyList());

        // 2. Roles de Cliente Específico (siim-frontend)
        var resourceAccess = (Map<String, Object>) jwt.getClaims().getOrDefault("resource_access", Collections.emptyMap());
        var clientAccess = (Map<String, Object>) resourceAccess.getOrDefault(resourceId, Collections.emptyMap());
        var clientRoles = (Collection<String>) clientAccess.getOrDefault("roles", Collections.emptyList());

        // 3. Roles de Administración
        var managementAccess = (Map<String, Object>) resourceAccess.getOrDefault("realm-management", Collections.emptyMap());
        var managementRoles = (Collection<String>) managementAccess.getOrDefault("roles", Collections.emptyList());

        // 4. Unificar, Normalizar y Prefijar
        return Stream.concat(
                        Stream.concat(realmRoles.stream(), clientRoles.stream()),
                        managementRoles.stream()
                )
                .map(role -> {
                    // Normalizar: ROLE_realm-admin -> ROLE_REALM-ADMIN
                    String normalizedRole = role.toUpperCase();
                    return normalizedRole.startsWith("ROLE_") ? normalizedRole : "ROLE_" + normalizedRole;
                })
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
