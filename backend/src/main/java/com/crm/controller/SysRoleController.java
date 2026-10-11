package com.crm.controller;

import com.crm.annotation.OperationLog;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.dto.RoleCreateDTO;
import com.crm.dto.RoleQueryDTO;
import com.crm.dto.RoleUpdateDTO;
import com.crm.service.SysRoleService;
import com.crm.vo.RoleOptionVO;
import com.crm.vo.RoleVO;
import com.crm.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * System Role Management Controller
 * All endpoints require ADMIN role (enforced by AdminInterceptor).
 *
 * @author CRM Team
 */
@Api(tags = "Role Management")
@RestController
@RequestMapping("/api/v1/system/roles")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @ApiOperation("Role Pagination Query")
    @GetMapping
    public Result<PageResult<RoleVO>> listRoles(RoleQueryDTO query) {
        return Result.success(sysRoleService.listRoles(query));
    }

    @ApiOperation("Role Options (Dropdown)")
    @GetMapping("/options")
    public Result<List<RoleOptionVO>> getRoleOptions() {
        return Result.success(sysRoleService.getRoleOptions());
    }

    @ApiOperation("Role Detail")
    @GetMapping("/{id}")
    public Result<RoleVO> getRoleDetail(@PathVariable Long id) {
        return Result.success(sysRoleService.getRoleDetail(id));
    }

    @ApiOperation("Users assigned to role")
    @GetMapping("/{id}/users")
    public Result<List<UserVO>> listRoleUsers(@PathVariable Long id) {
        return Result.success(sysRoleService.listRoleUsers(id));
    }

    @OperationLog(module = "Role Management", type = "Create", description = "Create role")
    @ApiOperation("Create Role")
    @PostMapping
    public Result<Void> createRole(@Valid @RequestBody RoleCreateDTO dto) {
        sysRoleService.createRole(dto);
        return Result.success();
    }

    @OperationLog(module = "Role Management", type = "Update", description = "Update role")
    @ApiOperation("Update Role")
    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO dto) {
        sysRoleService.updateRole(id, dto);
        return Result.success();
    }

    @OperationLog(module = "Role Management", type = "Delete", description = "Delete role")
    @ApiOperation("Delete Role")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.success();
    }
}
