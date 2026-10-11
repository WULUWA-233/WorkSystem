package com.crm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * User Password Reset DTO
 *
 * @author CRM Team
 */
public class UserPasswordResetDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "New password cannot be empty")
    @Size(min = 6, max = 64, message = "Password must be 6-64 characters")
    private String newPassword;

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
