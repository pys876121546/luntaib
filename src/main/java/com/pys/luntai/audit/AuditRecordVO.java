package com.pys.luntai.audit;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditRecordVO {

    // 审批记录ID
    private Long id;

    // 用户ID
    private Long userId;

    // 用户昵称
    private String nickname;

    // 用户头像
    private String avatarUrl;

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

    // 审批备注
    private String auditRemark;

    // 审核人ID
    private Long auditorId;

    // 审核人昵称
    private String auditorName;

    // 审核时间
    private LocalDateTime auditTime;

    // 提交时间
    private LocalDateTime createTime;
}
