package com.crm.dto;

import com.crm.common.PageQuery;

public class FollowRecordQueryDTO extends PageQuery {
    private Long customerId;
    private Long opportunityId;
    private Long followerId;
    private String followType;
    private Integer customerIntention;
    private String startTime;
    private String endTime;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public Long getFollowerId() { return followerId; }
    public void setFollowerId(Long followerId) { this.followerId = followerId; }
    public String getFollowType() { return followType; }
    public void setFollowType(String followType) { this.followType = followType; }
    public Integer getCustomerIntention() { return customerIntention; }
    public void setCustomerIntention(Integer customerIntention) { this.customerIntention = customerIntention; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
