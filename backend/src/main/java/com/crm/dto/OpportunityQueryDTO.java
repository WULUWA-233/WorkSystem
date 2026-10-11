package com.crm.dto;

import com.crm.common.PageQuery;
import java.math.BigDecimal;

public class OpportunityQueryDTO extends PageQuery {
    private static final long serialVersionUID = 1L;

    private Long customerId;
    private String opportunityName;
    private Integer stage;
    private Long ownerId;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String expectedStartDate;
    private String expectedEndDate;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getOpportunityName() { return opportunityName; }
    public void setOpportunityName(String opportunityName) { this.opportunityName = opportunityName; }
    public Integer getStage() { return stage; }
    public void setStage(Integer stage) { this.stage = stage; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
    public String getExpectedStartDate() { return expectedStartDate; }
    public void setExpectedStartDate(String expectedStartDate) { this.expectedStartDate = expectedStartDate; }
    public String getExpectedEndDate() { return expectedEndDate; }
    public void setExpectedEndDate(String expectedEndDate) { this.expectedEndDate = expectedEndDate; }
}
