package com.crm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.BusinessException;
import com.crm.common.PageResult;
import com.crm.common.ResultCode;
import com.crm.dto.*;
import com.crm.entity.SysRole;
import com.crm.entity.SysUser;
import com.crm.entity.SysUserRole;
import com.crm.mapper.SysRoleMapper;
import com.crm.mapper.SysUserMapper;
import com.crm.mapper.SysUserRoleMapper;
import com.crm.service.SysUserService;
import com.crm.utils.LoginUserContext;
import com.crm.utils.PasswordUtils;
import com.crm.vo.RoleOptionVO;
import com.crm.vo.UserDetailVO;
import com.crm.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * System User Service Implementation
 *
 * @author CRM Team
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    public SysUserServiceImpl(SysUserMapper sysUserMapper, SysRoleMapper sysRoleMapper,
                              SysUserRoleMapper sysUserRoleMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    @Override
    public PageResult<UserVO> listUsers(UserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDeleted, 0);

        if (query.getUsername() != null && !query.getUsername().isEmpty()) {
            wrapper.like(SysUser::getUsername, query.getUsername());
        }
        if (query.getRealName() != null && !query.getRealName().isEmpty()) {
            wrapper.like(SysUser::getRealName, query.getRealName());
        }
        if (query.getPhone() != null && !query.getPhone().isEmpty()) {
            wrapper.like(SysUser::getPhone, query.getPhone());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysUser::getId);

        Page<SysUser> page = new Page<>(query.getPage(), query.getSize());
        Page<SysUser> result = sysUserMapper.selectPage(page, wrapper);

        List<UserVO> records = result.getRecords().stream().map(user -> {
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
        }).collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public UserDetailVO getUserDetail(Long id) {
        SysUser user = getUserOrThrow(id);

        UserDetailVO vo = new UserDetailVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setGender(user.getGender());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        vo.setRemark(user.getRemark());

        List<SysRole> roles = sysRoleMapper.selectByUserId(id);
        vo.setRoles(roles.stream()
                .map(r -> new RoleOptionVO(r.getId(), r.getRoleName(), r.getRoleCode()))
                .collect(Collectors.toList()));
        vo.setRoleIds(roles.stream().map(SysRole::getId).collect(Collectors.toList()));

        return vo;
    }

    @Override
    @Transactional
    public void createUser(UserCreateDTO dto) {
        validateStatus(dto.getStatus());
        validateGender(dto.getGender());
        requireRoleIds(dto.getRoleIds(), "请选择角色");
        validateRoleIds(dto.getRoleIds());

        // Check username uniqueness
        SysUser existing = sysUserMapper.selectByUsername(dto.getUsername());
        if (existing != null) {
            throw new BusinessException(ResultCode.DUPLICATE_KEY, "Username already exists: " + dto.getUsername());
        }

        // Create user
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtils.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setGender(dto.getGender() != null ? dto.getGender() : 0);
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        user.setRemark(dto.getRemark());
        sysUserMapper.insert(user);

        replaceUserRoles(user.getId(), dto.getRoleIds());

        log.info("User created: id={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateDTO dto) {
        SysUser user = getUserOrThrow(id);
        validateStatusChange(user, dto.getStatus());
        validateGender(dto.getGender());
        if (dto.getRoleIds() != null) {
            requireRoleIds(dto.getRoleIds(), "不允许用户无角色");
            validateRoleIds(dto.getRoleIds());
            ensureAdminRoleKept(user, dto.getRoleIds());
        } else if (sysRoleMapper.selectByUserId(id).isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不允许用户无角色");
        }

        // Do not write the previously loaded password or login metadata back.
        SysUser update = new SysUser();
        update.setId(id);
        update.setRealName(dto.getRealName());
        update.setNickname(dto.getNickname());
        update.setPhone(dto.getPhone());
        update.setEmail(dto.getEmail());
        update.setGender(dto.getGender());
        update.setStatus(dto.getStatus());
        update.setRemark(dto.getRemark());
        sysUserMapper.updateById(update);
        if (dto.getRoleIds() != null) {
            replaceUserRoles(id, dto.getRoleIds());
        }
        log.info("User updated: id={}", id);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        SysUser user = getUserOrThrow(id);
        Long currentUserId = LoginUserContext.getCurrentUserId();

        // Protect self
        if (user.getId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "不能删除自己");
        }

        // Admin users can be deleted only by another admin user.
        if (isAdminUser(user.getId()) && !isAdminUser(currentUserId)) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "删除管理员用户必须由另一个管理员执行");
        }

        // Logic delete user (use LambdaUpdateWrapper to bypass @TableLogic field stripping)
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysUser> deleteWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        deleteWrapper.eq(SysUser::getId, id).set(SysUser::getDeleted, 1);
        sysUserMapper.update(null, deleteWrapper);

        // Logic delete user-role associations
        sysUserRoleMapper.deleteByUserId(id);

        log.info("User deleted: id={}, username={}", id, user.getUsername());
    }

    @Override
    public void updateStatus(Long id, UserStatusDTO dto) {
        if (dto.getStatus() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Status must be 0 or 1");
        }

        SysUser user = getUserOrThrow(id);
        validateStatusChange(user, dto.getStatus());
        SysUser update = new SysUser();
        update.setId(id);
        update.setStatus(dto.getStatus());
        sysUserMapper.updateById(update);
        log.info("User status changed: id={}, status={}", id, dto.getStatus());
    }

    @Override
    public void resetPassword(Long id, UserPasswordResetDTO dto) {
        SysUser user = getUserOrThrow(id);

        SysUser update = new SysUser();
        update.setId(id);
        update.setPassword(PasswordUtils.encode(dto.getNewPassword()));
        sysUserMapper.updateById(update);
        log.info("Password reset: id={}, username={}", id, user.getUsername());
    }

    @Override
    @Transactional
    public void assignRoles(Long id, UserRoleAssignDTO dto) {
        SysUser user = getUserOrThrow(id);
        requireRoleIds(dto.getRoleIds(), "不允许用户无角色");

        validateRoleIds(dto.getRoleIds());
        ensureAdminRoleKept(user, dto.getRoleIds());

        replaceUserRoles(id, dto.getRoleIds());

        log.info("Roles assigned: userId={}, roleIds={}", id, dto.getRoleIds());
    }

    // ========== Private helpers ==========

    private SysUser getUserOrThrow(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || !Integer.valueOf(0).equals(user.getDeleted())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "User not found");
        }
        return user;
    }

    private void validateRoleIds(List<Long> roleIds) {
        for (Long roleId : new LinkedHashSet<>(roleIds)) {
            if (roleId == null || roleId <= 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "Role ID must be positive");
            }
            SysRole role = sysRoleMapper.selectById(roleId);
            if (role == null || !Integer.valueOf(0).equals(role.getDeleted())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "Role not found: id=" + roleId);
            }
            if (!Integer.valueOf(1).equals(role.getStatus())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "Role is disabled: " + role.getRoleCode());
            }
        }
    }

    private void requireRoleIds(List<Long> roleIds, String message) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, message);
        }
    }

    private void ensureAdminRoleKept(SysUser user, List<Long> roleIds) {
        if ("admin".equals(user.getUsername()) && roleIds.stream().distinct()
                .map(sysRoleMapper::selectById)
                .noneMatch(role -> role != null && "ADMIN".equals(role.getRoleCode()))) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "Cannot remove ADMIN role from admin user");
        }
    }

    private void validateStatus(Integer status) {
        if (status != null && status != 0 && status != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Status must be 0 or 1");
        }
    }

    private void validateGender(Integer gender) {
        if (gender != null && (gender < 0 || gender > 2)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Gender must be 0, 1 or 2");
        }
    }

    private void validateStatusChange(SysUser user, Integer status) {
        validateStatus(status);
        if (Integer.valueOf(0).equals(status)) {
            if ("admin".equals(user.getUsername())) {
                throw new BusinessException(ResultCode.OPERATION_FAILED, "Cannot disable the admin account");
            }
            if (user.getId().equals(LoginUserContext.getCurrentUserId())) {
                throw new BusinessException(ResultCode.OPERATION_FAILED, "Cannot disable your own account");
            }
        }
    }

    private boolean isAdminUser(Long userId) {
        return sysRoleMapper.selectByUserId(userId).stream()
                .anyMatch(role -> "ADMIN".equals(role.getRoleCode()));
    }

    private void replaceUserRoles(Long userId, List<Long> roleIds) {
        sysUserRoleMapper.physicalDeleteByUserId(userId);
        for (Long roleId : new LinkedHashSet<>(roleIds)) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            sysUserRoleMapper.insert(ur);
        }
    }
}
