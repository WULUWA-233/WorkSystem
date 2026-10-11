package com.crm.config;

import com.crm.entity.SysRole;
import com.crm.entity.SysUser;
import com.crm.mapper.SysRoleMapper;
import com.crm.mapper.SysUserMapper;
import com.crm.utils.JwtUtils;
import com.crm.utils.LoginUserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    @org.junit.jupiter.api.Test
    void repeatedLoginsHaveDistinctTokensAndKeepUserIdClaim() {
        String first = com.crm.utils.JwtUtils.generateToken(9L, "sales");
        String second = com.crm.utils.JwtUtils.generateToken(9L, "sales");
        org.junit.jupiter.api.Assertions.assertNotEquals(first, second);
        org.junit.jupiter.api.Assertions.assertEquals(9L, com.crm.utils.JwtUtils.getUserId(first));
        org.junit.jupiter.api.Assertions.assertEquals(9L, com.crm.utils.JwtUtils.getUserId(second));
    }
    private final SysRoleMapper roleMapper = mock(SysRoleMapper.class);
    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(roleMapper, userMapper);

    @AfterEach
    void clearContexts() {
        LoginUserContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void activeAccountUsesCurrentDatabaseIdentityAndClearsContexts() throws Exception {
        SysUser user = activeUser();
        when(userMapper.selectById(9L)).thenReturn(user);
        SysRole role = new SysRole();
        role.setRoleCode("SALES");
        when(roleMapper.selectByUserId(9L)).thenReturn(Arrays.asList(role, role));
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean called = new AtomicBoolean();

        filter.doFilter(request(token("stored-hash")), response, (req, res) -> {
            called.set(true);
            assertEquals(9L, LoginUserContext.getCurrentUserId());
            assertEquals("current-name", LoginUserContext.getCurrentUsername());
            assertEquals(Collections.singletonList("SALES"), LoginUserContext.getCurrentUserRoles());
            assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        });

        assertTrue(called.get());
        assertEquals(200, response.getStatus());
        assertContextsCleared();
    }

    @Test
    void disabledAccountCannotUsePreviouslyIssuedToken() throws Exception {
        SysUser user = activeUser();
        user.setStatus(0);
        when(userMapper.selectById(9L)).thenReturn(user);
        assertRejected(token("stored-hash"));
        verifyNoInteractions(roleMapper);
    }

    @Test
    void deletedAndMissingAccountsAreRejected() throws Exception {
        SysUser user = activeUser();
        user.setDeleted(1);
        when(userMapper.selectById(9L)).thenReturn(user).thenReturn(null);
        assertRejected(token("stored-hash"));
        assertRejected(token("stored-hash"));
        verifyNoInteractions(roleMapper);
    }

    @Test
    void passwordResetInvalidatesOldTokenAndAcceptsNewLogin() throws Exception {
        SysUser user = activeUser();
        user.setPassword("replacement-hash");
        when(userMapper.selectById(9L)).thenReturn(user);
        when(roleMapper.selectByUserId(9L)).thenReturn(Collections.emptyList());
        assertRejected(token("stored-hash"));
        AtomicBoolean called = new AtomicBoolean();
        filter.doFilter(request(token("replacement-hash")), new MockHttpServletResponse(),
                (req, res) -> called.set(true));
        assertTrue(called.get());
        assertContextsCleared();
    }

    @Test
    void legacyTokenWithoutCredentialBindingRequiresLoginAgain() throws Exception {
        when(userMapper.selectById(9L)).thenReturn(activeUser());
        assertRejected(JwtUtils.generateToken(9L, "old-name"));
    }

    @Test
    void missingAndMalformedTokensCannotLeavePreviousThreadContext() throws Exception {
        LoginUserContext.setCurrentUser(88L, "stale", Collections.singletonList("ADMIN"));
        assertRejected(null);
        assertRejected("bad-token");
        verifyNoInteractions(userMapper, roleMapper);
    }

    @Test
    void malformedCredentialClaimIsAnAuthenticationFailure() throws Exception {
        assertRejected(JwtUtils.generateToken(9L, "old-name", Collections.singletonMap("credentialVersion", 123)));
        verifyNoInteractions(userMapper, roleMapper);
    }

    @Test
    void downstreamFailureStillClearsAuthentication() throws Exception {
        when(userMapper.selectById(9L)).thenReturn(activeUser());
        when(roleMapper.selectByUserId(9L)).thenReturn(Collections.emptyList());
        assertThrows(ServletException.class, () -> filter.doFilter(request(token("stored-hash")),
                new MockHttpServletResponse(), (req, res) -> { throw new ServletException("test failure"); }));
        assertContextsCleared();
    }

    @Test
    void publicPathUnderServletContextDoesNotRequireToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/crm/api/v1/auth/login");
        request.setContextPath("/crm");
        request.setServletPath("/api/v1/auth/login");
        AtomicBoolean called = new AtomicBoolean();
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> called.set(true));
        assertTrue(called.get());
        verifyNoInteractions(userMapper, roleMapper);
        assertContextsCleared();
    }

    @Test
    void filterIsRegisteredOnlyWithinSecurityChain() {
        SecurityConfig config = new SecurityConfig(roleMapper, userMapper);
        assertFalse(config.jwtFilterRegistration(filter).isEnabled());
    }

    private void assertRejected(String token) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request(token), response, (req, res) -> fail("Rejected requests must not reach controllers"));
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":401"));
        assertContextsCleared();
    }

    private static MockHttpServletRequest request(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/me");
        if (token != null) request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private static String token(String passwordHash) {
        return JwtUtils.generateToken(9L, "old-name", Collections.singletonMap("credentialVersion",
                JwtUtils.credentialVersion(passwordHash)));
    }

    private static SysUser activeUser() {
        SysUser user = new SysUser();
        user.setId(9L);
        user.setUsername("current-name");
        user.setPassword("stored-hash");
        user.setStatus(1);
        user.setDeleted(0);
        return user;
    }

    private static void assertContextsCleared() {
        assertNull(LoginUserContext.getCurrentUsername());
        assertTrue(LoginUserContext.getCurrentUserRoles().isEmpty());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
