package com.tuxoftware.ms_security.service.impl;

import com.tuxoftware.ms_security.dto.request.CreateUser;
import com.tuxoftware.ms_security.service.UserManagementService;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementServiceImpl implements UserManagementService {
    private final Keycloak keycloak;

    @Value("${app.keycloak.admin.realm}")
    private String realm;

    @Override
    public String createUser(CreateUser request) {
        RealmResource realmResource = keycloak.realm(realm);

        // MITIGACIÓN RIESGO 1: Roles Inexistentes (Fail Fast)
        // Validamos que los roles existan ANTES de crear al usuario.
        List<RoleRepresentation> rolesToAssign = validateRolesExist(realmResource, request.roles());

        // 1. Preparar UserRepresentation
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);
        user.setEmailVerified(true);

        String createdUserId = null;

        try {
            // 2. Crear Usuario
            try (Response response = realmResource.users().create(user)) {
                if (response.getStatus() == 409) {
                    throw new IllegalArgumentException("El usuario o email ya existe en Keycloak");
                }
                if (response.getStatus() >= 400) {
                    throw new RuntimeException("Error creando usuario. Keycloak Status: " + response.getStatus());
                }
                createdUserId = CreatedResponseUtil.getCreatedId(response);
            }

            log.info("Usuario base creado con ID: {}", createdUserId);
            UserResource userResource = realmResource.users().get(createdUserId);

            // 3. Asignar Password
            try {
                CredentialRepresentation passwordCred = new CredentialRepresentation();
                passwordCred.setTemporary(true);
                passwordCred.setType(CredentialRepresentation.PASSWORD);
                passwordCred.setValue(request.password());
                userResource.resetPassword(passwordCred);
            } catch (BadRequestException e) {
                // Captura error 400 de Keycloak (ej: password muy corto, falta mayúscula)
                throw new IllegalArgumentException("La contraseña no cumple con las políticas de seguridad: " + e.getMessage());
            }

            // 4. Asignar Roles
            if (!rolesToAssign.isEmpty()) {
                userResource.roles().realmLevel().add(rolesToAssign);
            }

            log.info("Usuario {} provisionado exitosamente.", request.username());
            return createdUserId;

        } catch (Exception e) {
            // MITIGACIÓN RIESGO 2: Atomicidad (Compensación / Rollback Manual)
            log.error("Fallo en el proceso de provisionamiento. Iniciando Rollback para usuario ID: {}", createdUserId);
            rollbackUserCreation(realmResource, createdUserId);
            throw e; // Relanzamos para que el Controller se entere
        }
    }

    /**
     * Valida la existencia de roles. Si uno falta, lanza excepción y aborta.
     */
    private List<RoleRepresentation> validateRolesExist(RealmResource realmResource, List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return List.of();
        }
        List<RoleRepresentation> validRoles = new ArrayList<>();
        for (String roleName : roleNames) {
            try {
                // Intenta obtener la representación completa del rol
                validRoles.add(realmResource.roles().get(roleName).toRepresentation());
            } catch (NotFoundException e) {
                throw new IllegalArgumentException("El rol especificado no existe en el sistema: " + roleName);
            }
        }
        return validRoles;
    }

    /**
     * Elimina el usuario si algo falló después de su creación (Password o Roles).
     * Evita usuarios "zombies" sin permisos o contraseña.
     */
    private void rollbackUserCreation(RealmResource realmResource, String userId) {
        if (userId != null) {
            try {
                realmResource.users().get(userId).remove();
                log.info("Rollback exitoso: Usuario {} eliminado.", userId);
            } catch (Exception ex) {
                // Alerta Crítica: Esto requeriría intervención manual de soporte
                log.error("ALERTA CRÍTICA: Falló el rollback del usuario {}. Datos inconsistentes en Keycloak.", userId, ex);
            }
        }
    }
}
