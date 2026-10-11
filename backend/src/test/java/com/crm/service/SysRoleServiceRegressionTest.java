package com.crm.service;

import com.crm.common.BusinessException;
import com.crm.dto.RoleCreateDTO;
import com.crm.dto.RoleUpdateDTO;
import com.crm.entity.SysRole;
import com.crm.mapper.SysRoleMapper;
import com.crm.mapper.SysUserMapper;
import com.crm.mapper.SysUserRoleMapper;
import com.crm.service.impl.SysRoleServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SysRoleServiceRegressionTest {
    private final SysRoleMapper roleMapper = mock(SysRoleMapper.class);
    private final SysUserRoleMapper relationMapper = mock(SysUserRoleMapper.class);
    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final SysRoleServiceImpl service = new SysRoleServiceImpl(roleMapper, relationMapper, userMapper);

    @Test
    void invalidRoleStatusIsRejectedBeforeDatabaseWrite() {
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleName("Example");
        dto.setRoleCode("EXAMPLE");
        dto.setStatus(7);
        assertThrows(BusinessException.class, () -> service.createRole(dto));
        verifyNoInteractions(roleMapper);
    }

    @Test
    void editingRoleCannotDisableRequiredBusinessRole() {
        when(roleMapper.selectById(37L)).thenReturn(role("ADMIN"));
        RoleUpdateDTO dto = new RoleUpdateDTO();
        dto.setStatus(0);
        assertThrows(BusinessException.class, () -> service.updateRole(37L, dto));
        verify(roleMapper, never()).updateById(any());
    }

    @Test
    void editingRoleCannotSaveWhitespaceOnlyName() {
        when(roleMapper.selectById(37L)).thenReturn(role("EXAMPLE"));
        RoleUpdateDTO dto = new RoleUpdateDTO();
        dto.setRoleName("   ");
        assertThrows(BusinessException.class, () -> service.updateRole(37L, dto));
        verify(roleMapper, never()).updateById(any());
    }

    private static SysRole role(String code) {
        SysRole role = new SysRole();
        role.setId(37L);
        role.setRoleCode(code);
        role.setStatus(1);
        role.setDeleted(0);
        return role;
    }
}
