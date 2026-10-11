package com.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Follow Record Entity
 *
 * @author CRM Team
 */
@Data
@TableName("crm_follow_record")
public class CrmFollowRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;
    private Long opportunityId;
    private String followType;
    private String followContent;
    private String followResult;
    private Integer customerIntention;
    private String nextPlan;
    private LocalDateTime nextFollowTime;
    private LocalDateTime followTime;
    private Long followerId;
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
