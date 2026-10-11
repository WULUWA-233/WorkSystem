package com.crm.dto;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Customer Status Change DTO
 *
 * @author CRM Team
 */
public class CustomerStatusDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Status cannot be null")
    private Integer status;

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
