package com.tuxoftware.ms_security.config;


import com.tuxoftware.ms_security.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Captura IllegalArgumentException.
     * Casos: Password débil, Roles inválidos, Usuario ya existente (según tu lógica actual).
     * Retorna: 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Error de validación: {}", e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    /**
     * Captura Excepciones de Keycloak (NotFound).
     * Caso: Intentar asignar un rol que no existe (si Keycloak lo reporta directo).
     * Retorna: 404 Not Found
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e, HttpServletRequest request) {
        log.warn("Recurso no encontrado: {}", e.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado en el proveedor de identidad", request);
    }

    /**
     * Captura Excepciones de Keycloak (BadRequest).
     * Caso: Errores nativos de Keycloak.
     * Retorna: 400 Bad Request
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleKcBadRequest(BadRequestException e, HttpServletRequest request) {
        log.error("Error en petición a Keycloak: {}", e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Error procesando la solicitud en Keycloak", request);
    }

    /**
     * Fallback General (Cualquier otra cosa).
     * Caso: NullPointer, Fallo de Red, Keycloak caído.
     * Retorna: 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e, HttpServletRequest request) {
        log.error("Error no controlado: ", e); // Importante loguear el stacktrace aquí
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno inesperado. Contacte a soporte.",
                request
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}
