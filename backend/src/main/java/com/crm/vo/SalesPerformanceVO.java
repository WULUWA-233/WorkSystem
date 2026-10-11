package com.crm.vo;

import java.io.Serializable;
import java.math.BigDecimal;

public class SalesPerformanceVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long ownerId;
    private String ownerName;
    private Long customerCount;
    private Long opportunityCount;
    private Long wonOpportunityCount;
    private Long orderCount;
    private BigDecimal orderAmount;

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public Long getCustomerCount() { return customerCount; }
    public void setCustomerCount(Long customerCount) { this.customerCount = customerCount; }
    public Long getOpportunityCount() { return opportunityCount; }
    public void setOpportunityCount(Long opportunityCount) { this.opportunityCount = opportunityCount; }
    public Long getWonOpportunityCount() { return wonOpportunityCount; }
    public void setWonOpportunityCount(Long wonOpportunityCount) { this.wonOpportunityCount = wonOpportunityCount; }
    public Long getOrderCount() { return orderCount; }
    public void setOrderCount(Long orderCount) { this.orderCount = orderCount; }
    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }
}
