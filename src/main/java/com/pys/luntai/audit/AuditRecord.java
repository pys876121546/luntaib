package com.pys.luntai.audit;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("audit_record")
public class AuditRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 用户ID
    private Long userId;

    // 公司名称
    private String companyName;

    // 公司地址
    private String companyAddress;

    // 公司电话
    private String companyPhone;

    // 公司门头照片URL
    private String companyPhotoUrl;

    // 审批状态: PENDING=待审核, APPROVED=已通过, REJECTED=已拒绝
    private String status;

    // 审批备注（管理员填写）
    private String auditRemark;

    // 审核人ID
    private Long auditorId;

    // 审核时间
    private LocalDateTime auditTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
