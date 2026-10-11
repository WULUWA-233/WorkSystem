package com.crm.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

public class FollowRecordVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long customerId;
    private String customerName;
    private Long opportunityId;
    private String opportunityName;
    private String followType;
    private String followContent;
    private String followResult;
    private Integer customerIntention;
    private String nextPlan;
    private LocalDateTime nextFollowTime;
    private LocalDateTime followTime;
    private Long followerId;
    private String followerName;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public String getOpportunityName() { return opportunityName; }
    public void setOpportunityName(String opportunityName) { this.opportunityName = opportunityName; }
    public String getFollowType() { return followType; }
    public void setFollowType(String followType) { this.followType = followType; }
    public String getFollowContent() { return followContent; }
    public void setFollowContent(String followContent) { this.followContent = followContent; }
    public String getFollowResult() { return followResult; }
    public void setFollowResult(String followResult) { this.followResult = followResult; }
    public Integer getCustomerIntention() { return customerIntention; }
    public void setCustomerIntention(Integer customerIntention) { this.customerIntention = customerIntention; }
    public String getNextPlan() { return nextPlan; }
    public void setNextPlan(String nextPlan) { this.nextPlan = nextPlan; }
    public LocalDateTime getNextFollowTime() { return nextFollowTime; }
    public void setNextFollowTime(LocalDateTime nextFollowTime) { this.nextFollowTime = nextFollowTime; }
    public LocalDateTime getFollowTime() { return followTime; }
    public void setFollowTime(LocalDateTime followTime) { this.followTime = followTime; }
    public Long getFollowerId() { return followerId; }
    public void setFollowerId(Long followerId) { this.followerId = followerId; }
    public String getFollowerName() { return followerName; }
    public void setFollowerName(String followerName) { this.followerName = followerName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
