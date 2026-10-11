package com.crm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * System Role Mapper — query only for authentication
 *
 * @author CRM Team
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * Query roles for a given user ID
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0 AND ur.deleted = 0 AND r.status = 1")
    List<SysRole> selectByUserId(@Param("userId") Long userId);
}
