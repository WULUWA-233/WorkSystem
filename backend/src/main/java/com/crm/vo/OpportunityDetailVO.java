package com.crm.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OpportunityDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long customerId;
    private String customerName;
    private String opportunityName;
    private BigDecimal amount;
    private Integer stage;
    private String stageName;
    private Integer probability;
    private LocalDate expectedDate;
    private Long ownerId;
    private String ownerName;
    private String source;
    private String loseReason;
    private LocalDateTime winTime;
    private LocalDateTime closeTime;
    private LocalDateTime lastFollowTime;
    private LocalDateTime nextFollowTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getOpportunityName() { return opportunityName; }
    public void setOpportunityName(String opportunityName) { this.opportunityName = opportunityName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Integer getStage() { return stage; }
    public void setStage(Integer stage) { this.stage = stage; }
    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }
    public Integer getProbability() { return probability; }
    public void setProbability(Integer probability) { this.probability = probability; }
    public LocalDate getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDate expectedDate) { this.expectedDate = expectedDate; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getLoseReason() { return loseReason; }
    public void setLoseReason(String loseReason) { this.loseReason = loseReason; }
    public LocalDateTime getWinTime() { return winTime; }
    public void setWinTime(LocalDateTime winTime) { this.winTime = winTime; }
    public LocalDateTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalDateTime closeTime) { this.closeTime = closeTime; }
    public LocalDateTime getLastFollowTime() { return lastFollowTime; }
    public void setLastFollowTime(LocalDateTime lastFollowTime) { this.lastFollowTime = lastFollowTime; }
    public LocalDateTime getNextFollowTime() { return nextFollowTime; }
    public void setNextFollowTime(LocalDateTime nextFollowTime) { this.nextFollowTime = nextFollowTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
