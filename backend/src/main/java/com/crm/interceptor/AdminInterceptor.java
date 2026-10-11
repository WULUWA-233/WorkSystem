package com.crm.interceptor;

import com.crm.common.BusinessException;
import com.crm.common.ResultCode;
import com.crm.entity.SysRole;
import com.crm.mapper.SysRoleMapper;
import com.crm.utils.LoginUserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ADMIN Role Interceptor
 * Intercepts /api/v1/system/** and checks if current user has ADMIN role.
 *
 * @author CRM Team
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AdminInterceptor.class);

    private final SysRoleMapper sysRoleMapper;

    public AdminInterceptor(SysRoleMapper sysRoleMapper) {
        this.sysRoleMapper = sysRoleMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long userId = LoginUserContext.getCurrentUserId();
        String username = LoginUserContext.getCurrentUsername();

        List<String> roleCodes = LoginUserContext.getCurrentUserRoles();
        if (roleCodes == null || roleCodes.isEmpty()) {
            List<SysRole> roles = sysRoleMapper.selectByUserId(userId);
            roleCodes = roles.stream()
                    .map(SysRole::getRoleCode)
                    .collect(Collectors.toList());
            LoginUserContext.setCurrentUser(userId, username, roleCodes);
        }

        // Check ADMIN role
        if (!roleCodes.contains("ADMIN")) {
            log.warn("Access denied: user={}, roles={}, uri={}", username, roleCodes, request.getRequestURI());
            throw new BusinessException(ResultCode.FORBIDDEN, "No permission to access");
        }

        log.debug("ADMIN access granted: user={}, uri={}", username, request.getRequestURI());
        return true;
    }
}
