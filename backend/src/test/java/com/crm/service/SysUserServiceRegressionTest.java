package com.crm.service;

import com.crm.common.BusinessException;
import com.crm.dto.UserRoleAssignDTO;
import com.crm.dto.UserUpdateDTO;
import com.crm.dto.UserPasswordResetDTO;
import com.crm.entity.SysRole;
import com.crm.entity.SysUser;
import com.crm.entity.SysUserRole;
import com.crm.mapper.SysRoleMapper;
import com.crm.mapper.SysUserMapper;
import com.crm.mapper.SysUserRoleMapper;
import com.crm.service.impl.SysUserServiceImpl;
import com.crm.utils.LoginUserContext;
import com.crm.utils.PasswordUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SysUserServiceRegressionTest {
    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final SysRoleMapper roleMapper = mock(SysRoleMapper.class);
    private final SysUserRoleMapper relationMapper = mock(SysUserRoleMapper.class);
    private final SysUserServiceImpl service = new SysUserServiceImpl(userMapper, roleMapper, relationMapper);

    @AfterEach
    void clearContext() { LoginUserContext.clear(); }

    @Test
    void editCannotBypassProtectionAgainstDisablingAdmin() {
        when(userMapper.selectById(9L)).thenReturn(user("admin"));
        LoginUserContext.setCurrentUser(20L, "other-admin");
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setStatus(0);
        assertThrows(BusinessException.class, () -> service.updateUser(9L, dto));
        verify(userMapper, never()).updateById(any());
    }

    @Test
    void editCannotDisableOwnAccount() {
        when(userMapper.selectById(9L)).thenReturn(user("sales"));
        LoginUserContext.setCurrentUser(9L, "sales");
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setStatus(0);
        assertThrows(BusinessException.class, () -> service.updateUser(9L, dto));
        verify(userMapper, never()).updateById(any());
    }

    @Test
    void reservedAdminRoleIsIdentifiedByCodeInsteadOfIdOne() {
        when(userMapper.selectById(9L)).thenReturn(user("admin"));
        when(roleMapper.selectById(37L)).thenReturn(role(37L, "ADMIN"));
        UserRoleAssignDTO dto = new UserRoleAssignDTO();
        dto.setRoleIds(Collections.singletonList(37L));
        service.assignRoles(9L, dto);
        ArgumentCaptor<SysUserRole> relation = ArgumentCaptor.forClass(SysUserRole.class);
        verify(relationMapper).insert(relation.capture());
        assertEquals(37L, relation.getValue().getRoleId());
    }

    @Test
    void duplicateRoleIdsAreAssignedOnlyOnce() {
        when(userMapper.selectById(9L)).thenReturn(user("sales"));
        when(roleMapper.selectById(2L)).thenReturn(role(2L, "SALES"));
        UserRoleAssignDTO dto = new UserRoleAssignDTO();
        dto.setRoleIds(Arrays.asList(2L, 2L));
        service.assignRoles(9L, dto);
        verify(relationMapper, times(1)).insert(any());
    }

    @Test
    void missingRoleIdReturnsBusinessErrorInsteadOfPassingNullToMapper() {
        when(userMapper.selectById(9L)).thenReturn(user("sales"));
        UserRoleAssignDTO dto = new UserRoleAssignDTO();
        dto.setRoleIds(Collections.singletonList(null));
        assertThrows(BusinessException.class, () -> service.assignRoles(9L, dto));
        verifyNoInteractions(roleMapper, relationMapper);
    }

    @Test
    void profileEditDoesNotOverwriteLoadedPassword() {
        when(userMapper.selectById(9L)).thenReturn(user("sales"));
        when(roleMapper.selectByUserId(9L)).thenReturn(Collections.singletonList(role(2L, "SALES")));
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setRealName("New name");
        service.updateUser(9L, dto);
        ArgumentCaptor<SysUser> update = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).updateById(update.capture());
        assertEquals("New name", update.getValue().getRealName());
        assertNull(update.getValue().getPassword());
        assertNull(update.getValue().getStatus());
        assertNull(update.getValue().getUsername());
    }

    @Test
    void passwordResetDoesNotOverwriteAccountStatusOrProfile() {
        when(userMapper.selectById(9L)).thenReturn(user("sales"));
        UserPasswordResetDTO dto = new UserPasswordResetDTO();
        dto.setNewPassword("new-password");
        service.resetPassword(9L, dto);
        ArgumentCaptor<SysUser> update = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).updateById(update.capture());
        assertTrue(PasswordUtils.matches("new-password", update.getValue().getPassword()));
        assertNull(update.getValue().getStatus());
        assertNull(update.getValue().getUsername());
    }

    private static SysUser user(String username) {
        SysUser user = new SysUser();
        user.setId(9L);
        user.setUsername(username);
        user.setPassword("existing-hash");
        user.setDeleted(0);
        user.setStatus(1);
        return user;
    }

    private static SysRole role(Long id, String code) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleCode(code);
        role.setDeleted(0);
        role.setStatus(1);
        return role;
    }
}
