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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer Module Permission Interceptor
 * Intercepts business APIs and checks for ADMIN, SALES, MANAGER, or AUDITOR role.
 *
 * @author CRM Team
 */
@Component
public class CustomerPermissionInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CustomerPermissionInterceptor.class);

    /** Roles allowed to access business modules. */
    private static final List<String> ALLOWED_ROLES = Arrays.asList("ADMIN", "SALES", "MANAGER", "AUDITOR");

    private final SysRoleMapper sysRoleMapper;

    public CustomerPermissionInterceptor(SysRoleMapper sysRoleMapper) {
        this.sysRoleMapper = sysRoleMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long userId = LoginUserContext.getCurrentUserId();
        String username = LoginUserContext.getCurrentUsername();

        // Query user roles (or use cached roles if already set by AdminInterceptor)
        List<String> roleCodes = LoginUserContext.getCurrentUserRoles();
        if (roleCodes == null || roleCodes.isEmpty()) {
            List<SysRole> roles = sysRoleMapper.selectByUserId(userId);
            roleCodes = roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());
            LoginUserContext.setCurrentUser(userId, username, roleCodes);
        }

        // Check if user has an allowed role
        boolean allowed = roleCodes.stream().anyMatch(ALLOWED_ROLES::contains);
        if (!allowed) {
            log.warn("Customer access denied: user={}, roles={}, uri={}", username, roleCodes, request.getRequestURI());
            throw new BusinessException(ResultCode.FORBIDDEN, "No permission to access");
        }

        log.debug("Customer access granted: user={}, uri={}", username, request.getRequestURI());
        return true;
    }
}
