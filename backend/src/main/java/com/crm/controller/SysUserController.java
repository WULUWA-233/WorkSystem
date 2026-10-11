package com.crm.controller;

import com.crm.annotation.OperationLog;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.dto.*;
import com.crm.service.SysUserService;
import com.crm.vo.UserDetailVO;
import com.crm.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * System User Management Controller
 * All endpoints require ADMIN role (enforced by AdminInterceptor).
 *
 * @author CRM Team
 */
@Api(tags = "User Management")
@RestController
@RequestMapping("/api/v1/system/users")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @ApiOperation("User Pagination Query")
    @GetMapping
    public Result<PageResult<UserVO>> listUsers(UserQueryDTO query) {
        return Result.success(sysUserService.listUsers(query));
    }

    @ApiOperation("User Detail")
    @GetMapping("/{id}")
    public Result<UserDetailVO> getUserDetail(@PathVariable Long id) {
        return Result.success(sysUserService.getUserDetail(id));
    }

    @OperationLog(module = "User Management", type = "Create", description = "Create user")
    @ApiOperation("Create User")
    @PostMapping
    public Result<Void> createUser(@Valid @RequestBody UserCreateDTO dto) {
        sysUserService.createUser(dto);
        return Result.success();
    }

    @OperationLog(module = "User Management", type = "Update", description = "Update user")
    @ApiOperation("Update User")
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        sysUserService.updateUser(id, dto);
        return Result.success();
    }

    @OperationLog(module = "User Management", type = "Delete", description = "Delete user")
    @ApiOperation("Delete User (Logic Delete)")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }

    @OperationLog(module = "User Management", type = "Status Change", description = "Enable/disable user")
    @ApiOperation("Enable/Disable User")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusDTO dto) {
        sysUserService.updateStatus(id, dto);
        return Result.success();
    }

    @OperationLog(module = "User Management", type = "Password Reset", description = "Reset user password")
    @ApiOperation("Reset Password")
    @PatchMapping("/{id}/password/reset")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody UserPasswordResetDTO dto) {
        sysUserService.resetPassword(id, dto);
        return Result.success();
    }

    @OperationLog(module = "User Management", type = "Role Assignment", description = "Assign roles to user")
    @ApiOperation("Assign Roles to User")
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody UserRoleAssignDTO dto) {
        sysUserService.assignRoles(id, dto);
        return Result.success();
    }
}
