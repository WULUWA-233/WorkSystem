package com.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Customer Entity
 *
 * @author CRM Team
 */
@Data
@TableName("crm_customer")
public class CrmCustomer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String customerName;
    private String industry;
    private String source;
    private String level;
    private String region;
    private String address;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private Long ownerId;
    private Integer status;
    private Integer score;
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
