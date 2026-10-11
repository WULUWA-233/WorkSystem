package com.crm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class OrderShipDTO {
    @NotBlank(message = "Logistics tracking number cannot be blank")
    @Size(max = 64, message = "Logistics tracking number must not exceed 64 characters")
    private String logisticsNo;
    @Size(max = 64, message = "Logistics company must not exceed 64 characters")
    private String logisticsCompany;
    @Size(max = 500, message = "Remark must not exceed 500 characters")
    private String remark;
    public String getLogisticsNo() { return logisticsNo; }
    public void setLogisticsNo(String logisticsNo) { this.logisticsNo = logisticsNo; }
    public String getLogisticsCompany() { return logisticsCompany; }
    public void setLogisticsCompany(String logisticsCompany) { this.logisticsCompany = logisticsCompany; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
