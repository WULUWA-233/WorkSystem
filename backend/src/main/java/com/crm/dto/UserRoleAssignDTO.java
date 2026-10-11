package com.crm.dto;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * User Role Assignment DTO
 *
 * @author CRM Team
 */
public class UserRoleAssignDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "请选择角色")
    private List<@NotNull @Positive Long> roleIds;

    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
}
