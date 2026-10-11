package com.crm.service;

import com.crm.common.PageResult;
import com.crm.dto.RoleCreateDTO;
import com.crm.dto.RoleQueryDTO;
import com.crm.dto.RoleUpdateDTO;
import com.crm.vo.RoleOptionVO;
import com.crm.vo.RoleVO;
import com.crm.vo.UserVO;

import java.util.List;

/**
 * System Role Service Interface
 *
 * @author CRM Team
 */
public interface SysRoleService {

    PageResult<RoleVO> listRoles(RoleQueryDTO query);

    List<RoleOptionVO> getRoleOptions();

    RoleVO getRoleDetail(Long id);

    List<UserVO> listRoleUsers(Long id);

    void createRole(RoleCreateDTO dto);

    void updateRole(Long id, RoleUpdateDTO dto);

    void deleteRole(Long id);
}
