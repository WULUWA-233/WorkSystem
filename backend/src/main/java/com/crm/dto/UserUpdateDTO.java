package com.crm.dto;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * User Update Request DTO (no username/password changes here)
 *
 * @author CRM Team
 */
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(max = 64) private String realName;
    @Size(max = 64) private String nickname;
    @Size(max = 20) private String phone;
    @Size(max = 128) private String email;
    @Min(0) @Max(2) private Integer gender;
    @Min(0) @Max(1) private Integer status;
    private List<@NotNull @Positive Long> roleIds;
    @Size(max = 500) private String remark;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
