package com.crm.dto;

import com.crm.common.PageQuery;

/**
 * User Query DTO (extends PageQuery for pagination)
 *
 * @author CRM Team
 */
public class UserQueryDTO extends PageQuery {

    private static final long serialVersionUID = 1L;

    private String username;
    private String realName;
    private String phone;
    private Integer status;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
