package com.tuxoftware.ms_security.service;

import com.tuxoftware.ms_security.dto.request.CreateUser;

public interface UserManagementService {

    String createUser(CreateUser request);
}
