package com.crm.vo;

import java.io.Serializable;

/**
 * Role Option VO (simplified for dropdown lists)
 *
 * @author CRM Team
 */
public class RoleOptionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String roleName;
    private String roleCode;

    public RoleOptionVO() {}

    public RoleOptionVO(Long id, String roleName, String roleCode) {
        this.id = id;
        this.roleName = roleName;
        this.roleCode = roleCode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
}
