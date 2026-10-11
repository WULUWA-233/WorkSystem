package com.crm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * System User Mapper — query only for authentication
 *
 * @author CRM Team
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * Query user by username (excluding logic-deleted records via MyBatis-Plus)
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    SysUser selectByUsername(@Param("username") String username);
}
