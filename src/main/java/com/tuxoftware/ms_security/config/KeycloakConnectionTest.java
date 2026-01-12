package com.tuxoftware.ms_security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakConnectionTest implements CommandLineRunner {

    private final Keycloak keycloak;

    @Value("${app.keycloak.admin.realm}")
    private String targetRealm;

    @Override
    public void run(String... args) {
        try {
            // Intenta obtener información del Realm para validar acceso
            var realmRep = keycloak.realm(targetRealm).toRepresentation();
            log.info("✅ CONEXIÓN EXITOSA A KEYCLOAK. Realm: {}, Activo: {}",
                    realmRep.getRealm(), realmRep.isEnabled());
        } catch (Exception e) {
            log.error("❌ ERROR CONECTANDO A KEYCLOAK: {}", e.getMessage());
            // No fallar el startup en desarrollo, pero sí en prod
        }
    }
}