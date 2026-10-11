package com.crm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.entity.SysUserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * User-Role Association Mapper
 *
 * @author CRM Team
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * Logic-delete all role associations for a user
     */
    @Delete("UPDATE sys_user_role SET deleted = 1 WHERE user_id = #{userId} AND deleted = 0")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * Physical-delete all role associations for a user (for role reassignment)
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int physicalDeleteByUserId(@Param("userId") Long userId);

    /**
     * Count active (non-deleted) role assignments for a given role ID
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM sys_user_role WHERE role_id = #{roleId} AND deleted = 0")
    int countByRoleId(@Param("roleId") Long roleId);
}
