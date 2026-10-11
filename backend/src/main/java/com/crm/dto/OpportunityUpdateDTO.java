package com.crm.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

public class OpportunityUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(max = 128)
    private String opportunityName;
    @DecimalMin("0") @Digits(integer = 10, fraction = 2)
    private BigDecimal amount;
    @Min(0) @Max(100)
    private Integer probability;
    private String expectedDate;
    @Positive
    private Long ownerId;
    @Size(max = 64)
    private String source;
    private String nextFollowTime;
    @Size(max = 500)
    private String remark;

    public String getOpportunityName() { return opportunityName; }
    public void setOpportunityName(String opportunityName) { this.opportunityName = opportunityName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
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
