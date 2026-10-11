package com.crm.interceptor;

import com.crm.common.BusinessException;
import com.crm.common.ResultCode;
import com.crm.entity.SysRole;
import com.crm.mapper.SysRoleMapper;
import com.crm.utils.LoginUserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuditorReadOnlyInterceptor implements HandlerInterceptor {

    private final SysRoleMapper sysRoleMapper;

    public AuditorReadOnlyInterceptor(SysRoleMapper sysRoleMapper) {
        this.sysRoleMapper = sysRoleMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        List<String> roleCodes = LoginUserContext.getCurrentUserRoles();
        if (roleCodes == null || roleCodes.isEmpty()) {
            Long userId = LoginUserContext.getCurrentUserId();
            roleCodes = sysRoleMapper.selectByUserId(userId).stream()
                    .map(SysRole::getRoleCode)
                    .collect(Collectors.toList());
            LoginUserContext.setCurrentUser(userId, LoginUserContext.getCurrentUsername(), roleCodes);
        }

        if (roleCodes.contains("AUDITOR") && !isReadMethod(request.getMethod())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "审计员只允许查看数据");
        }
        return true;
    }

    private boolean isReadMethod(String method) {
        return "GET".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method);
    }
}
