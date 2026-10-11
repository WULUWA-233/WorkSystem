package com.crm.service;

import com.crm.common.PageResult;
import com.crm.dto.*;
import com.crm.vo.UserDetailVO;
import com.crm.vo.UserVO;

/**
 * System User Service Interface
 *
 * @author CRM Team
 */
public interface SysUserService {

    PageResult<UserVO> listUsers(UserQueryDTO query);

    UserDetailVO getUserDetail(Long id);

    void createUser(UserCreateDTO dto);

    void updateUser(Long id, UserUpdateDTO dto);

    void deleteUser(Long id);

    void updateStatus(Long id, UserStatusDTO dto);

    void resetPassword(Long id, UserPasswordResetDTO dto);

    void assignRoles(Long id, UserRoleAssignDTO dto);
}
