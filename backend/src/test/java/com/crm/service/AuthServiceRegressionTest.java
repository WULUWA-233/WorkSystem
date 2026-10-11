package com.crm.service;

import com.crm.common.BusinessException;
import com.crm.dto.LoginDTO;
import com.crm.entity.SysUser;
import com.crm.mapper.SysRoleMapper;
import com.crm.mapper.SysUserMapper;
import com.crm.service.impl.AuthServiceImpl;
import com.crm.utils.JwtUtils;
import com.crm.utils.PasswordUtils;
import com.crm.vo.LoginVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceRegressionTest {
    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final SysRoleMapper roleMapper = mock(SysRoleMapper.class);
    private final AuthServiceImpl service = new AuthServiceImpl(userMapper, roleMapper);

    @Test
    void loginBindsTokenToPasswordAndUpdatesOnlyLoginFields() {
        SysUser user = activeUser();
        when(userMapper.selectByUsername("sales")).thenReturn(user);
        when(roleMapper.selectByUserId(9L)).thenReturn(Collections.emptyList());
        LoginVO login = service.login(new LoginDTO("sales", "123456"), "127.0.0.1");

        String version = JwtUtils.parseToken(login.getAccessToken()).get("credentialVersion", String.class);
        assertTrue(JwtUtils.matchesCredentialVersion(version, user.getPassword()));
        assertNotEquals(user.getPassword(), version);
        assertFalse(JwtUtils.parseToken(login.getAccessToken()).containsKey("password"));
        ArgumentCaptor<SysUser> update = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).updateById(update.capture());
        assertEquals(9L, update.getValue().getId());
        assertNotNull(update.getValue().getLastLoginTime());
        assertEquals("127.0.0.1", update.getValue().getLastLoginIp());
        assertNull(update.getValue().getPassword());
        assertNull(update.getValue().getStatus());
    }

    @Test
    void disabledAccountCannotLoadCurrentUser() {
        SysUser user = activeUser();
        user.setStatus(0);
        when(userMapper.selectById(9L)).thenReturn(user);
        assertEquals(401, assertThrows(BusinessException.class, () -> service.getCurrentUser(9L)).getCode());
        verifyNoInteractions(roleMapper);
    }

    @Test
    void deletedAccountCannotLogin() {
        SysUser user = activeUser();
        user.setDeleted(1);
        when(userMapper.selectByUsername("sales")).thenReturn(user);
        assertEquals(401, assertThrows(BusinessException.class,
                () -> service.login(new LoginDTO("sales", "123456"), "127.0.0.1")).getCode());
        verify(userMapper, never()).updateById(any());
    }

    @Test
    void tokenGenerationAcceptsImmutableClaimsWithoutMutatingCaller() {
        Map<String, Object> claims = Collections.singletonMap("example", "value");
        String token = JwtUtils.generateToken(9L, "sales", claims);
        assertEquals("value", JwtUtils.parseToken(token).get("example"));
        assertEquals(Collections.singletonMap("example", "value"), claims);
    }

    private static SysUser activeUser() {
        SysUser user = new SysUser();
        user.setId(9L);
        user.setUsername("sales");
        user.setPassword(PasswordUtils.encode("123456"));
        user.setStatus(1);
        user.setDeleted(0);
        return user;
    }
}
