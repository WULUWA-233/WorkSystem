package com.crm.service.impl;

import com.crm.common.BusinessException;
import com.crm.common.ResultCode;
import com.crm.dto.LoginDTO;
import com.crm.entity.SysRole;
import com.crm.entity.SysUser;
import com.crm.mapper.SysRoleMapper;
import com.crm.mapper.SysUserMapper;
import com.crm.service.AuthService;
import com.crm.utils.JwtUtils;
import com.crm.utils.PasswordUtils;
import com.crm.vo.CurrentUserVO;
import com.crm.vo.LoginVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Authentication Service Implementation
 *
 * @author CRM Team
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;

    public AuthServiceImpl(SysUserMapper sysUserMapper, SysRoleMapper sysRoleMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
    }

    @Override
    public LoginVO login(LoginDTO loginDTO, String ip) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        // 1. Query user by username
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            log.warn("Login failed: user not found — username={}", username);
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Incorrect username or password");
        }

        // 2. Check if user is deleted (double-check, MyBatis-Plus already filters)
        if (!Integer.valueOf(0).equals(user.getDeleted())) {
            log.warn("Login failed: user deleted — username={}", username);
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Incorrect username or password");
        }

        // 3. Check if user is disabled
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            log.warn("Login failed: user disabled — username={}", username);
            throw new BusinessException(ResultCode.UNAUTHORIZED, "User account has been disabled");
        }

        // 4. Verify password
        if (!PasswordUtils.matches(password, user.getPassword())) {
            log.warn("Login failed: wrong password — username={}", username);
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Incorrect username or password");
        }

        // 5. Query user roles
        List<SysRole> roles = sysRoleMapper.selectByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());

        // 6. Generate JWT token
        String token = JwtUtils.generateToken(user.getId(), user.getUsername(),
                Collections.singletonMap("credentialVersion", JwtUtils.credentialVersion(user.getPassword())));

        // 7. Update last login time and IP
        // Update only login metadata, so concurrent password/status changes are not overwritten.
        SysUser loginUpdate = new SysUser();
        loginUpdate.setId(user.getId());
        loginUpdate.setLastLoginTime(LocalDateTime.now());
        loginUpdate.setLastLoginIp(ip);
        sysUserMapper.updateById(loginUpdate);

        log.info("User logged in: username={}, id={}, ip={}", username, user.getId(), ip);

        // 8. Build response
        LoginVO vo = new LoginVO();
        vo.setAccessToken(token);
        vo.setTokenType("Bearer");
        vo.setExpiresIn(JwtUtils.EXPIRATION_SECONDS);

        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setRoles(roleCodes);
        vo.setUser(userInfo);

        return vo;
    }

    @Override
    public CurrentUserVO getCurrentUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(0).equals(user.getDeleted())
                || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "User account is unavailable");
        }

        List<SysRole> roles = sysRoleMapper.selectByUserId(userId);
        List<String> roleCodes = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());

        CurrentUserVO vo = new CurrentUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setRoles(roleCodes);

        return vo;
    }

    @Override
    public void logout(Long userId) {
        // MVP: no token blacklist — client is responsible for discarding the token
        log.info("User logged out: userId={}", userId);
    }
}
