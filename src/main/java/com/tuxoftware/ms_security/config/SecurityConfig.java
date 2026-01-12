package com.tuxoftware.ms_security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.security.client-id:siim-frontend}")
    private String clientId;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults()) // Importante para permitir CORS si el Gateway no lo maneja
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter(clientId));
        return converter;
    }

    /**
     * Convertidor de roles Keycloak -> Spring Security
     */
    static class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        private final String resourceId;

        public KeycloakRoleConverter(String resourceId) {
            this.resourceId = resourceId;
        }

        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            // 1. Roles de Realm
            var realmAccess = (Map<String, Object>) jwt.getClaims().getOrDefault("realm_access", Collections.emptyMap());
            var realmRoles = (Collection<String>) realmAccess.getOrDefault("roles", Collections.emptyList());

            // 2. Roles de Cliente (Resource)
            var resourceAccess = (Map<String, Object>) jwt.getClaims().getOrDefault("resource_access", Collections.emptyMap());
            var clientAccess = (Map<String, Object>) resourceAccess.getOrDefault(resourceId, Collections.emptyMap());
            var clientRoles = (Collection<String>) clientAccess.getOrDefault("roles", Collections.emptyList());

            // 3. Unificar y Prefijar
            return Stream.concat(realmRoles.stream(), clientRoles.stream())
                    .map(role -> {
                        // Asegurar prefijo ROLE_ para compatibilidad con hasRole()
                        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    })
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
    }
}
