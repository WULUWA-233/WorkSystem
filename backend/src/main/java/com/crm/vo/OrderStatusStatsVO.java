package com.crm.vo;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderStatusStatsVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer status;
    private String statusName;
    private Long count;
    private BigDecimal amount;

    public OrderStatusStatsVO() {}
    public OrderStatusStatsVO(Integer status, String statusName, Long count, BigDecimal amount) {
        this.status = status; this.statusName = statusName; this.count = count; this.amount = amount;
    }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
