package com.crm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class OpportunityLoseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Loss reason cannot be empty")
    @Size(max = 500)
    private String loseReason;

    @Size(max = 500)
    private String remark;

    public String getLoseReason() { return loseReason; }
    public void setLoseReason(String loseReason) { this.loseReason = loseReason; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
