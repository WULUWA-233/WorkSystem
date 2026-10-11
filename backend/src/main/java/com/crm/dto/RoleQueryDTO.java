package com.crm.dto;

import com.crm.common.PageQuery;

/**
 * Role Query DTO (extends PageQuery for pagination)
 *
 * @author CRM Team
 */
public class RoleQueryDTO extends PageQuery {

    private static final long serialVersionUID = 1L;

    private String roleName;
    private String roleCode;
    private Integer status;

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
