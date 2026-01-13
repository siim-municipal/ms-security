package com.tuxoftware.ms_security.controller;

import com.tuxoftware.ms_security.dto.UserProfileDTO;
import com.tuxoftware.ms_security.dto.request.CreateUser;
import com.tuxoftware.ms_security.dto.request.ResetPasswordRequest;
import com.tuxoftware.ms_security.dto.request.UpdateStatusRequest;
import com.tuxoftware.ms_security.dto.response.PagedResponse;
import com.tuxoftware.ms_security.dto.response.UserResponse;
import com.tuxoftware.ms_security.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserManagementService userService;

    @PostMapping
    @PreAuthorize("hasRole('REALM-ADMIN')")
    public ResponseEntity<Map<String, String>> createUser(@RequestBody CreateUser request) {
        String userId = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("userId", userId, "message", "Usuario creado exitosamente"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        UserProfileDTO profile = userService.getUserProfile(userId, jwt);
        return ResponseEntity.ok(profile);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<UserResponse>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // Validación defensiva de paginación
        if (page < 0) page = 0;
        if (size < 1) size = 10;

        return ResponseEntity.ok(userService.listUsers(search, page, size));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable String id,
            @RequestBody UpdateStatusRequest request
    ) {
        userService.updateUserStatus(id, request.enabled());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable String id,
            @RequestBody ResetPasswordRequest request
    ) {
        userService.resetPassword(id, request.newPassword());
        return ResponseEntity.noContent().build();
    }


}