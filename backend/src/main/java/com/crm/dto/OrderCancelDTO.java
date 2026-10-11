package com.crm.dto;

import javax.validation.constraints.Size;

public class OrderCancelDTO {
    @Size(max = 500, message = "Remark must not exceed 500 characters")
    private String remark;
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
