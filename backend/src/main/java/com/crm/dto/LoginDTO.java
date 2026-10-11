package com.crm.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@ApiModel("Login Request")
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Username cannot be empty")
    @ApiModelProperty(value = "Username", required = true, example = "admin")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @ApiModelProperty(value = "Password", required = true, example = "123456")
    private String password;

    public LoginDTO() {
    }

    public LoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginDTO{username='" + username + "', password='***'}";
    }
}
