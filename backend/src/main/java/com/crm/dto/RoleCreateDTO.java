package com.crm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import java.io.Serializable;
import java.util.Locale;

/**
 * Role Create Request DTO
 *
 * @author CRM Team
 */
public class RoleCreateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Role name cannot be empty")
    @Size(max = 64)
    private String roleName;

    @NotBlank(message = "Role code cannot be empty")
    @Size(max = 64)
    private String roleCode;

    @Size(max = 255) private String description;
    @Min(0) @Max(1) private Integer status;
    private Integer sort;
    @Size(max = 500) private String remark;

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode != null ? roleCode.trim().toUpperCase(Locale.ROOT) : null; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
