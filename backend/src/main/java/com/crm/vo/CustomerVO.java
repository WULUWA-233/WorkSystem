package com.crm.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Customer List VO
 *
 * @author CRM Team
 */
public class CustomerVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String customerName;
    private String industry;
    private String source;
    private Integer level;
    private String region;
    private String contactName;
    private String contactPhone;
    private Long ownerId;
    private String ownerName;
    private Integer status;
    private Integer score;
    private LocalDateTime lastFollowTime;
    private LocalDateTime nextFollowTime;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public LocalDateTime getLastFollowTime() { return lastFollowTime; }
    public void setLastFollowTime(LocalDateTime lastFollowTime) { this.lastFollowTime = lastFollowTime; }
    public LocalDateTime getNextFollowTime() { return nextFollowTime; }
    public void setNextFollowTime(LocalDateTime nextFollowTime) { this.nextFollowTime = nextFollowTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
