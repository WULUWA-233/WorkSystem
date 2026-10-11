package com.crm.dto;

import com.crm.common.PageQuery;

/**
 * Customer Query DTO (extends PageQuery)
 *
 * @author CRM Team
 */
public class CustomerQueryDTO extends PageQuery {
    private static final long serialVersionUID = 1L;

    private String customerName;
    private String industry;
    private String source;
    private Integer level;
    private String region;
    private Integer status;
    private Long ownerId;
    private String startTime;
    private String endTime;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
