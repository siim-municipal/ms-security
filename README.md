# 🛡️ MS Security & User Management

Microservicio satélite encargado de la **gestión extendida de usuarios y auditoría**. Mientras que **Keycloak** maneja la identidad (AuthN) y los tokens, este servicio maneja la información complementaria del perfil del funcionario público y la estructura organizacional.

![Keycloak](https://img.shields.io/badge/Identity-Keycloak-orange)
![Security](https://img.shields.io/badge/Spring-Security-green)

## 🤝 Relación con Keycloak

Este servicio **NO** reemplaza a Keycloak. Funciona en conjunto:

1.  **Keycloak:** Emite tokens, gestiona passwords, 2FA y Realms.
2.  **MS Security:**
    * Almacena perfiles extendidos (Departamento, Puesto, Supervisor).
    * Sincroniza roles específicos del negocio.
    * Gestiona el árbol de jerarquía municipal.
    * Bitácora de auditoría de accesos sensibles.

## ⚙️ Configuración Crítica

Es vital configurar correctamente el convertidor de roles JWT (`JwtAuthenticationConverter`) para leer los roles de `realm_access` del token.

```yaml
security:
  oauth2:
    resourceserver:
      jwt:
        jwk-set-uri: ${JWK_SET_URI_KEYCLOACK}
