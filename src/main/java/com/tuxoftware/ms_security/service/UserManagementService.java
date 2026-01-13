package com.tuxoftware.ms_security.service;

import com.tuxoftware.ms_security.dto.request.CreateUser;
import com.tuxoftware.ms_security.dto.response.PagedResponse;
import com.tuxoftware.ms_security.dto.response.UserResponse;

public interface UserManagementService {

    String createUser(CreateUser request);

    PagedResponse<UserResponse> listUsers(String search, int page, int size);

    void updateUserStatus(String userId, boolean enabled);

    void resetPassword(String userId, String newPassword);
}
