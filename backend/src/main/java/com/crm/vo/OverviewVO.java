package com.crm.vo;

import java.io.Serializable;
import java.math.BigDecimal;

public class OverviewVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long customerTotal;
    private Long newCustomerCount;
    private Long opportunityTotal;
    private BigDecimal opportunityAmount;
    private Long wonOpportunityCount;
    private Long lostOpportunityCount;
    private Long orderTotal;
    private BigDecimal orderAmount;
    private BigDecimal paidOrderAmount;
    private Long followRecordTotal;
    private Long todayFollowCount;

    public Long getCustomerTotal() { return customerTotal; }
    public void setCustomerTotal(Long customerTotal) { this.customerTotal = customerTotal; }
    public Long getNewCustomerCount() { return newCustomerCount; }
    public void setNewCustomerCount(Long newCustomerCount) { this.newCustomerCount = newCustomerCount; }
    public Long getOpportunityTotal() { return opportunityTotal; }
    public void setOpportunityTotal(Long opportunityTotal) { this.opportunityTotal = opportunityTotal; }
    public BigDecimal getOpportunityAmount() { return opportunityAmount; }
    public void setOpportunityAmount(BigDecimal opportunityAmount) { this.opportunityAmount = opportunityAmount; }
    public Long getWonOpportunityCount() { return wonOpportunityCount; }
    public void setWonOpportunityCount(Long wonOpportunityCount) { this.wonOpportunityCount = wonOpportunityCount; }
    public Long getLostOpportunityCount() { return lostOpportunityCount; }
    public void setLostOpportunityCount(Long lostOpportunityCount) { this.lostOpportunityCount = lostOpportunityCount; }
    public Long getOrderTotal() { return orderTotal; }
    public void setOrderTotal(Long orderTotal) { this.orderTotal = orderTotal; }
    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }
    public BigDecimal getPaidOrderAmount() { return paidOrderAmount; }
    public void setPaidOrderAmount(BigDecimal paidOrderAmount) { this.paidOrderAmount = paidOrderAmount; }
    public Long getFollowRecordTotal() { return followRecordTotal; }
    public void setFollowRecordTotal(Long followRecordTotal) { this.followRecordTotal = followRecordTotal; }
    public Long getTodayFollowCount() { return todayFollowCount; }
    public void setTodayFollowCount(Long todayFollowCount) { this.todayFollowCount = todayFollowCount; }
}
