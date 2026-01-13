package com.tuxoftware.ms_security.service;

import com.tuxoftware.ms_security.dto.UserProfileDTO;
import com.tuxoftware.ms_security.dto.request.CreateUser;
import com.tuxoftware.ms_security.dto.response.PagedResponse;
import com.tuxoftware.ms_security.dto.response.UserResponse;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserManagementService {

    String createUser(CreateUser request);

    UserProfileDTO getUserProfile(String userId, Jwt jwt);

    PagedResponse<UserResponse> listUsers(String search, int page, int size);

    void updateUserStatus(String userId, boolean enabled);

    void resetPassword(String userId, String newPassword);
}
