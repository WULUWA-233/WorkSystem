package com.crm.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;

@ApiModel("Create Customer Request")
public class CustomerCreateDTO {
    @NotBlank @Size(max = 128)
    @ApiModelProperty(value = "Customer name (unique)", required = true, example = "ABC Corp")
    private String customerName;
    @Size(max = 64) @ApiModelProperty("Industry") private String industry;
    @Size(max = 64) @ApiModelProperty("Source") private String source;
    @Min(1) @Max(5) @ApiModelProperty("Level: 1-5") private Integer level;
    @Size(max = 64) @ApiModelProperty("Region") private String region;
    @Size(max = 255) @ApiModelProperty("Address") private String address;
    @Size(max = 64) @ApiModelProperty("Contact name") private String contactName;
    @Size(max = 20) @ApiModelProperty("Contact phone") private String contactPhone;
    @Email @Size(max = 128) @ApiModelProperty("Contact email") private String contactEmail;
    @NotNull @Positive @ApiModelProperty(value = "Owner user ID", required = true, example = "2") private Long ownerId;
    @Min(0) @Max(2) @ApiModelProperty("Status: 0=Invalid, 1=Active, 2=Public (default 1)") private Integer status;
    @Min(0) @Max(100) @ApiModelProperty("Score: 0-100") private Integer score;
    @ApiModelProperty("Next follow time") private String nextFollowTime;
    @Size(max = 500) @ApiModelProperty("Remark") private String remark;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getNextFollowTime() { return nextFollowTime; }
    public void setNextFollowTime(String nextFollowTime) { this.nextFollowTime = nextFollowTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
