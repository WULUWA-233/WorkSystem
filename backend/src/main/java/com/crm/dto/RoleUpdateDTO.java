package com.crm.dto;

import java.io.Serializable;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

/**
 * Role Update Request DTO (roleCode not editable)
 *
 * @author CRM Team
 */
public class RoleUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(min = 1, max = 64) private String roleName;
    @Size(max = 255) private String description;
    @Min(0) @Max(1) private Integer status;
    private Integer sort;
    @Size(max = 500) private String remark;

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
