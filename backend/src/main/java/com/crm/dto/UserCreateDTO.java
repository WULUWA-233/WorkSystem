package com.crm.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@ApiModel("Create User Request")
public class UserCreateDTO {
    @NotBlank @Size(min = 2, max = 64)
    @ApiModelProperty(value = "Username", required = true, example = "sales03")
    private String username;
    @NotBlank @Size(min = 6, max = 64)
    @ApiModelProperty(value = "Password (BCrypt encrypted on server)", required = true, example = "123456")
    private String password;
    @Size(max = 64) @ApiModelProperty("Real name") private String realName;
    @Size(max = 64) @ApiModelProperty("Nickname") private String nickname;
    @Size(max = 20) @ApiModelProperty("Phone") private String phone;
    @Size(max = 128) @ApiModelProperty("Email") private String email;
    @Min(0) @Max(2) @ApiModelProperty("Gender: 0=Unknown, 1=Male, 2=Female") private Integer gender;
    @Min(0) @Max(1) @ApiModelProperty("Status: 0=Disabled, 1=Enabled (default 1)") private Integer status;
    @NotEmpty(message = "请选择角色")
    @ApiModelProperty(value = "Role IDs to assign", required = true) private List<@NotNull @Positive Long> roleIds;
    @Size(max = 500) @ApiModelProperty("Remark") private String remark;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
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
