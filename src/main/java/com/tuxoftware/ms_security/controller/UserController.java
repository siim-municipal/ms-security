package com.tuxoftware.ms_security.controller;

import com.tuxoftware.ms_security.dto.request.CreateUser;
import com.tuxoftware.ms_security.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userService;

    @PostMapping
    public ResponseEntity<Map<String, String>> createUser(@RequestBody CreateUser request) {
        String userId = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("userId", userId, "message", "Usuario creado exitosamente"));
    }

    // Manejador de excepciones básico local (idealmente usar @ControllerAdvice global)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    }
}