package com.crm.service;

import com.crm.dto.LoginDTO;
import com.crm.vo.CurrentUserVO;
import com.crm.vo.LoginVO;

/**
 * Authentication Service Interface
 *
 * @author CRM Team
 */
public interface AuthService {

    /**
     * User login
     *
     * @param loginDTO login credentials
     * @param ip       client IP address
     * @return login result with token
     */
    LoginVO login(LoginDTO loginDTO, String ip);

    /**
     * Get current logged-in user info
     *
     * @param userId current user ID
     * @return current user info
     */
    CurrentUserVO getCurrentUser(Long userId);

    /**
     * User logout
     *
     * @param userId current user ID
     */
    void logout(Long userId);
}
