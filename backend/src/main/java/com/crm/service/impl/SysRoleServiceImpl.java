package com.crm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.BusinessException;
import com.crm.common.PageResult;
import com.crm.common.ResultCode;
import com.crm.dto.RoleCreateDTO;
import com.crm.dto.RoleQueryDTO;
import com.crm.dto.RoleUpdateDTO;
import com.crm.entity.SysRole;
import com.crm.entity.SysUser;
import com.crm.entity.SysUserRole;
import com.crm.mapper.SysRoleMapper;
import com.crm.mapper.SysUserMapper;
import com.crm.mapper.SysUserRoleMapper;
import com.crm.service.SysRoleService;
import com.crm.vo.RoleOptionVO;
import com.crm.vo.RoleVO;
import com.crm.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * System Role Service Implementation
 *
 * @author CRM Team
 */
@Service
public class SysRoleServiceImpl implements SysRoleService {

    private static final Logger log = LoggerFactory.getLogger(SysRoleServiceImpl.class);

    /** Built-in role codes cannot be deleted because business authorization depends on them. */
    private static final List<String> BUILTIN_ROLE_CODES = java.util.Arrays.asList("ADMIN", "SALES", "MANAGER", "AUDITOR", "USER");
    private static final List<String> REQUIRED_ENABLED_ROLE_CODES = java.util.Arrays.asList("ADMIN", "SALES", "MANAGER", "AUDITOR");

    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysUserMapper sysUserMapper;

    public SysRoleServiceImpl(SysRoleMapper sysRoleMapper, SysUserRoleMapper sysUserRoleMapper,
                              SysUserMapper sysUserMapper) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public PageResult<RoleVO> listRoles(RoleQueryDTO query) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDeleted, 0);

        if (query.getRoleName() != null && !query.getRoleName().isEmpty()) {
            wrapper.like(SysRole::getRoleName, query.getRoleName());
        }
        if (query.getRoleCode() != null && !query.getRoleCode().isEmpty()) {
            wrapper.like(SysRole::getRoleCode, query.getRoleCode().toUpperCase());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysRole::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(SysRole::getSort);

        Page<SysRole> page = new Page<>(query.getPage(), query.getSize());
        Page<SysRole> result = sysRoleMapper.selectPage(page, wrapper);

        List<RoleVO> records = result.getRecords().stream().map(this::toRoleVO).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public List<RoleOptionVO> getRoleOptions() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDeleted, 0)
               .eq(SysRole::getStatus, 1)
               .orderByAsc(SysRole::getSort);

        List<SysRole> roles = sysRoleMapper.selectList(wrapper);
        return roles.stream()
                .map(r -> new RoleOptionVO(r.getId(), r.getRoleName(), r.getRoleCode()))
                .collect(Collectors.toList());
    }

    @Override
    public RoleVO getRoleDetail(Long id) {
        SysRole role = getRoleOrThrow(id);
        return toRoleVO(role);
    }

    @Override
    public List<UserVO> listRoleUsers(Long id) {
        SysRole role = getRoleOrThrow(id);
        LambdaQueryWrapper<SysUserRole> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(SysUserRole::getRoleId, role.getId())
                .eq(SysUserRole::getDeleted, 0);
        List<Long> userIds = sysUserRoleMapper.selectList(relationWrapper).stream()
                .map(SysUserRole::getUserId)
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.in(SysUser::getId, userIds)
                .eq(SysUser::getDeleted, 0)
                .orderByAsc(SysUser::getId);
        return sysUserMapper.selectList(userWrapper).stream()
                .map(this::toUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public void createRole(RoleCreateDTO dto) {
        validateStatus(dto.getStatus());
        // Check roleCode uniqueness
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, dto.getRoleCode())
               .eq(SysRole::getDeleted, 0);
        SysRole existing = sysRoleMapper.selectOne(wrapper);
        if (existing != null) {
            throw new BusinessException(ResultCode.DUPLICATE_KEY, "Role code already exists: " + dto.getRoleCode());
        }

        SysRole role = new SysRole();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setDescription(dto.getDescription());
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        role.setSort(dto.getSort() != null ? dto.getSort() : 0);
        role.setRemark(dto.getRemark());
        sysRoleMapper.insert(role);

        log.info("Role created: id={}, roleCode={}", role.getId(), role.getRoleCode());
    }

    @Override
    public void updateRole(Long id, RoleUpdateDTO dto) {
        SysRole role = getRoleOrThrow(id);
        validateStatus(dto.getStatus());
        if (dto.getRoleName() != null && dto.getRoleName().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Role name cannot be empty");
        }

        if (dto.getStatus() != null && dto.getStatus() == 0
                && REQUIRED_ENABLED_ROLE_CODES.contains(role.getRoleCode())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED,
                    "关键内置角色不可禁用：" + role.getRoleCode());
        }

        if (dto.getRoleName() != null) role.setRoleName(dto.getRoleName());
        if (dto.getDescription() != null) role.setDescription(dto.getDescription());
        if (dto.getStatus() != null) role.setStatus(dto.getStatus());
        if (dto.getSort() != null) role.setSort(dto.getSort());
        if (dto.getRemark() != null) role.setRemark(dto.getRemark());

        sysRoleMapper.updateById(role);
        log.info("Role updated: id={}", id);
    }

    @Override
    public void deleteRole(Long id) {
        SysRole role = getRoleOrThrow(id);

        // Protect built-in roles
        if (BUILTIN_ROLE_CODES.contains(role.getRoleCode())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED,
                    "内置角色不可删除：" + role.getRoleCode());
        }

        // Check if role is in use
        int userCount = sysUserRoleMapper.countByRoleId(id);
        if (userCount > 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED,
                    "该角色已分配给 " + userCount + " 个用户，不能删除");
        }

        // Logic delete role (use LambdaUpdateWrapper to bypass @TableLogic field stripping)
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysRole> deleteWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        deleteWrapper.eq(SysRole::getId, id).set(SysRole::getDeleted, 1);
        sysRoleMapper.update(null, deleteWrapper);
        log.info("Role deleted: id={}, roleCode={}", id, role.getRoleCode());
    }

    // ========== Private helpers ==========

    private SysRole getRoleOrThrow(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null || !Integer.valueOf(0).equals(role.getDeleted())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Role not found");
        }
        return role;
    }

    private void validateStatus(Integer status) {
        if (status != null && status != 0 && status != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Status must be 0 or 1");
        }
    }

    private RoleVO toRoleVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setDescription(role.getDescription());
        vo.setStatus(role.getStatus());
        vo.setSort(role.getSort());
        vo.setUserCount(sysUserRoleMapper.countByRoleId(role.getId()));
        vo.setCreatedAt(role.getCreatedAt());
        vo.setUpdatedAt(role.getUpdatedAt());
        vo.setRemark(role.getRemark());
        return vo;
    }

    private UserVO toUserVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setGender(user.getGender());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setRemark(user.getRemark());
        List<SysRole> roles = sysRoleMapper.selectByUserId(user.getId());
        vo.setRoles(roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList()));
        return vo;
    }
}
