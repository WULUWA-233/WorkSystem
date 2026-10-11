package com.crm.dto;

import javax.validation.constraints.Size;

public class OrderPayDTO {
    @Size(max = 32, message = "Payment method must not exceed 32 characters")
    private String paymentMethod;
    @Size(max = 500, message = "Remark must not exceed 500 characters")
    private String remark;
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
