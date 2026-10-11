package com.crm.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

@ApiModel("Create Follow Record Request")
public class FollowRecordCreateDTO {
    @NotNull @Positive @ApiModelProperty(value = "Customer ID", required = true) private Long customerId;
    @NotNull @Positive @ApiModelProperty(value = "Opportunity ID", required = true) private Long opportunityId;
    @NotBlank @ApiModelProperty(value = "Follow type", required = true, example = "电话") private String followType;
    @NotBlank @ApiModelProperty(value = "Follow content", required = true) private String followContent;
    @Size(max = 255) @ApiModelProperty("Follow result") private String followResult;
    @Min(1) @Max(4) @ApiModelProperty("Customer intention: 1-4") private Integer customerIntention;
    @Size(max = 500) @ApiModelProperty("Next plan") private String nextPlan;
    @ApiModelProperty("Next follow time") private String nextFollowTime;
    @ApiModelProperty("Follow time (default now)") private String followTime;
    @NotNull @Positive @ApiModelProperty(value = "Follower user ID", required = true) private Long followerId;
    @Size(max = 500) @ApiModelProperty("Remark") private String remark;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long v) { this.customerId = v; }
    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long v) { this.opportunityId = v; }
    public String getFollowType() { return followType; }
    public void setFollowType(String v) { this.followType = v; }
    public String getFollowContent() { return followContent; }
    public void setFollowContent(String v) { this.followContent = v; }
    public String getFollowResult() { return followResult; }
    public void setFollowResult(String v) { this.followResult = v; }
    public Integer getCustomerIntention() { return customerIntention; }
    public void setCustomerIntention(Integer v) { this.customerIntention = v; }
    public String getNextPlan() { return nextPlan; }
    public void setNextPlan(String v) { this.nextPlan = v; }
    public String getNextFollowTime() { return nextFollowTime; }
    public void setNextFollowTime(String v) { this.nextFollowTime = v; }
    public String getFollowTime() { return followTime; }
    public void setFollowTime(String v) { this.followTime = v; }
    public Long getFollowerId() { return followerId; }
    public void setFollowerId(Long v) { this.followerId = v; }
    public String getRemark() { return remark; }
    public void setRemark(String v) { this.remark = v; }
}
