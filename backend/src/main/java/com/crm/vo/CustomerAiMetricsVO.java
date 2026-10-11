package com.crm.vo;

import java.io.Serializable;

/**
 * AI Analysis — quantitative metrics
 */
public class CustomerAiMetricsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int opportunityCount;
    private int orderCount;
    private double totalOrderAmount;
    private double paidAmount;
    private int followRecordCount;
    private String lastFollowTime;

    public int getOpportunityCount() { return opportunityCount; }
    public void setOpportunityCount(int opportunityCount) { this.opportunityCount = opportunityCount; }
    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
    public double getTotalOrderAmount() { return totalOrderAmount; }
    public void setTotalOrderAmount(double totalOrderAmount) { this.totalOrderAmount = totalOrderAmount; }
    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }
    public int getFollowRecordCount() { return followRecordCount; }
    public void setFollowRecordCount(int followRecordCount) { this.followRecordCount = followRecordCount; }
    public String getLastFollowTime() { return lastFollowTime; }
    public void setLastFollowTime(String lastFollowTime) { this.lastFollowTime = lastFollowTime; }
}
