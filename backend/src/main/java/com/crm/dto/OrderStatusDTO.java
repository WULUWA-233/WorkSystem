package com.crm.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

public class OrderStatusDTO {
    @NotNull(message = "Status cannot be null")
    @Min(value = 1, message = "Status must be 1-5")
    @Max(value = 5, message = "Status must be 1-5")
    private Integer status;
    @Size(max = 32, message = "Payment method must not exceed 32 characters")
    private String paymentMethod;
    @Size(max = 64, message = "Logistics tracking number must not exceed 64 characters")
    private String logisticsNo;
    @Size(max = 64, message = "Logistics company must not exceed 64 characters")
    private String logisticsCompany;
    @Size(max = 500, message = "Remark must not exceed 500 characters")
    private String remark;
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getLogisticsNo() { return logisticsNo; }
    public void setLogisticsNo(String logisticsNo) { this.logisticsNo = logisticsNo; }
    public String getLogisticsCompany() { return logisticsCompany; }
    public void setLogisticsCompany(String logisticsCompany) { this.logisticsCompany = logisticsCompany; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
