package com.crm.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class OpportunityStageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Stage cannot be null")
    @Min(1) @Max(4)
    private Integer stage;

    @Min(0) @Max(100)
    private Integer probability;
    @Size(max = 500)
    private String remark;

    public Integer getStage() { return stage; }
    public void setStage(Integer stage) { this.stage = stage; }
    public Integer getProbability() { return probability; }
    public void setProbability(Integer probability) { this.probability = probability; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
