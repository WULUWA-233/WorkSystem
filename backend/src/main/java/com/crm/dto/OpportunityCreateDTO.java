package com.crm.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@ApiModel("Create Opportunity Request")
public class OpportunityCreateDTO {
    @NotNull @Positive @ApiModelProperty(value = "Customer ID", required = true) private Long customerId;
    @NotBlank @Size(max = 128) @ApiModelProperty(value = "Opportunity name", required = true) private String opportunityName;
    @DecimalMin("0") @Digits(integer = 10, fraction = 2) @ApiModelProperty("Amount") private BigDecimal amount;
    @Min(1) @Max(4) @ApiModelProperty("Stage: 1-4 (5/6 via /win /lose)") private Integer stage;
    @Min(0) @Max(100) @ApiModelProperty("Probability: 0-100") private Integer probability;
    @ApiModelProperty("Expected close date") private String expectedDate;
    @NotNull @Positive @ApiModelProperty(value = "Owner user ID", required = true) private Long ownerId;
    @Size(max = 64) @ApiModelProperty("Source") private String source;
    @ApiModelProperty("Next follow time") private String nextFollowTime;
    @Size(max = 500) @ApiModelProperty("Remark") private String remark;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getOpportunityName() { return opportunityName; }
    public void setOpportunityName(String opportunityName) { this.opportunityName = opportunityName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Integer getStage() { return stage; }
    public void setStage(Integer stage) { this.stage = stage; }
    public Integer getProbability() { return probability; }
    public void setProbability(Integer probability) { this.probability = probability; }
    public String getExpectedDate() { return expectedDate; }
    public void setExpectedDate(String expectedDate) { this.expectedDate = expectedDate; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getNextFollowTime() { return nextFollowTime; }
    public void setNextFollowTime(String nextFollowTime) { this.nextFollowTime = nextFollowTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
