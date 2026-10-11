package com.crm.vo;

import java.io.Serializable;
import java.math.BigDecimal;

public class StageStatsVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer stage;
    private String stageName;
    private Long count;
    private BigDecimal amount;

    public StageStatsVO() {}
    public StageStatsVO(Integer stage, String stageName, Long count, BigDecimal amount) {
        this.stage = stage; this.stageName = stageName; this.count = count; this.amount = amount;
    }
    public Integer getStage() { return stage; }
    public void setStage(Integer stage) { this.stage = stage; }
    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
