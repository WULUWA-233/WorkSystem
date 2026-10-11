package com.crm.controller;

import com.crm.annotation.OperationLog;
import com.crm.common.Result;
import com.crm.dto.LoginDTO;
import com.crm.service.AuthService;
import com.crm.utils.LoginUserContext;
import com.crm.vo.CurrentUserVO;
import com.crm.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Authentication Controller
 *
 * @author CRM Team
 */
@Api(tags = "Authentication")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @OperationLog(module = "Authentication", type = "Login", description = "User login")
    @ApiOperation("User Login")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        log.info("Login request: username={}", loginDTO.getUsername());
        String ip = getClientIp(request);
        LoginVO loginVO = authService.login(loginDTO, ip);
        return Result.success(loginVO);
    }

    @ApiOperation("Get Current User Info")
    @GetMapping("/me")
    public Result<CurrentUserVO> me() {
        Long userId = LoginUserContext.getCurrentUserId();
        CurrentUserVO userVO = authService.getCurrentUser(userId);
        return Result.success(userVO);
    }

    @OperationLog(module = "Authentication", type = "Logout", description = "User logout")
    @ApiOperation("User Logout")
    @PostMapping("/logout")
    public Result<Boolean> logout() {
        Long userId = LoginUserContext.getCurrentUserId();
        authService.logout(userId);
        return Result.success("Logged out", true);
    }

    /**
     * Extract client IP from request
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // If multiple IPs in X-Forwarded-For, take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
