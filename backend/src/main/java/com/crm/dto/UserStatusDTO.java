package com.crm.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import java.io.Serializable;

/**
 * User Status Toggle DTO
 *
 * @author CRM Team
 */
public class UserStatusDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Status cannot be null")
    @Min(0) @Max(1)
    private Integer status;

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
