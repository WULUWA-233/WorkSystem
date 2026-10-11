package com.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order Entity
 *
 * @author CRM Team
 */
@Data
@TableName("crm_order")
public class CrmOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long customerId;
    private Long opportunityId;
    private Long contractId;
    private BigDecimal amount;
    private Integer status;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private String logisticsNo;
    private String logisticsCompany;
    private Long ownerId;
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
