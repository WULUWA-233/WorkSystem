package com.crm.config;

import com.crm.common.Result;
import com.crm.common.ResultCode;
import com.crm.entity.SysRole;
import com.crm.entity.SysUser;
import com.crm.mapper.SysRoleMapper;
import com.crm.mapper.SysUserMapper;
import com.crm.utils.JwtUtils;
import com.crm.utils.LoginUserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter
 * Intercepts every request, validates JWT token (except whitelisted paths),
 * and sets the current user context.
 *
 * @author CRM Team
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private static final List<String> WHITELIST_PATTERNS = Arrays.asList(
            "/api/v1/common/health",
            "/api/v1/auth/login",
            "/doc.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v2/api-docs/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/favicon.ico",
            "/error"
    );

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final SysRoleMapper sysRoleMapper;
    private final SysUserMapper sysUserMapper;

    public JwtAuthenticationFilter(SysRoleMapper sysRoleMapper, SysUserMapper sysUserMapper) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        LoginUserContext.clear();
        SecurityContextHolder.clearContext();
        try {
            String requestUri = request.getServletPath();
            if (requestUri == null || requestUri.isEmpty()) {
                requestUri = request.getRequestURI().substring(request.getContextPath().length());
            }
            if (isWhitelisted(requestUri)) {
                filterChain.doFilter(request, response);
                return;
            }

            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
                writeUnauthorizedResponse(response, "Not logged in or missing token");
                return;
            }

            Claims claims;
            Long userId;
            String credentialVersion;
            try {
                claims = JwtUtils.parseToken(authHeader.substring(TOKEN_PREFIX.length()).trim());
                userId = Long.valueOf(claims.getId());
                if (claims.getExpiration() == null || userId <= 0) {
                    throw new IllegalArgumentException("Invalid token claims");
                }
                credentialVersion = claims.get("credentialVersion", String.class);
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("Invalid token: uri={}", requestUri);
                writeUnauthorizedResponse(response, "Not logged in or token invalid");
                return;
            }

            // A signed token does not keep a deleted or disabled account active.
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null || !Integer.valueOf(0).equals(user.getDeleted())
                    || !Integer.valueOf(1).equals(user.getStatus())) {
                writeUnauthorizedResponse(response, "User account is unavailable");
                return;
            }
            if (!JwtUtils.matchesCredentialVersion(credentialVersion, user.getPassword())) {
                writeUnauthorizedResponse(response, "Credentials changed, please log in again");
                return;
            }

            String username = user.getUsername();
            List<String> roleCodes = sysRoleMapper.selectByUserId(userId).stream()
                    .map(SysRole::getRoleCode)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            LoginUserContext.setCurrentUser(userId, username, roleCodes);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            roleCodes.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Authenticated user: id={}, username={}, roles={}, uri={}", userId, username, roleCodes, requestUri);
            filterChain.doFilter(request, response);
        } finally {
            // Clear ThreadLocal after request completes
            LoginUserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Check if the request URI matches any whitelist pattern
     */
    private boolean isWhitelisted(String requestUri) {
        for (String pattern : WHITELIST_PATTERNS) {
            if (PATH_MATCHER.match(pattern, requestUri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Write a structured 401 JSON response
     */
    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED, message);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(result));
    }
}
