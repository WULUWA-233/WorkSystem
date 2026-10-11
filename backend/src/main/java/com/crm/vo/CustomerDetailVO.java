package com.crm.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Customer Detail VO
 *
 * @author CRM Team
 */
public class CustomerDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String customerName;
    private String industry;
    private String source;
    private Integer level;
    private String region;
    private String address;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private Long ownerId;
    private String ownerName;
    private Integer status;
    private Integer score;
    private LocalDateTime lastFollowTime;
    private LocalDateTime nextFollowTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String remark;

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
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
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
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
