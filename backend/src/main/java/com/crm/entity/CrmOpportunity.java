package com.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Opportunity Entity
 *
 * @author CRM Team
 */
@Data
@TableName("crm_opportunity")
public class CrmOpportunity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;
    private String opportunityName;
    private BigDecimal amount;
    private String stage;
    private Integer probability;
    private LocalDate expectedDate;
    private Long ownerId;
    private String source;
    private String loseReason;
    private LocalDateTime winTime;
    private LocalDateTime closeTime;
    private LocalDateTime lastFollowTime;
    private LocalDateTime nextFollowTime;
    private Long createdBy;
    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    private String remark;
}
