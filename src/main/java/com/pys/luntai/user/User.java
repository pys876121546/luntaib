package com.pys.luntai.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 微信相关信息
    private String openid;
    private String unionid;
    private String nickname;
    private String avatarUrl;

    // 审核状态: NONE-未申请, PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝
    private String auditStatus;

    // 公司信息（审核时填写）
    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String companyPhotoUrl; // 公司门头照片URL

    // 审核备注（管理员填写）
    private String auditRemark;

    // 是否管理员: 0-否, 1-是
    private Boolean admin;

    // 是否管理员（返回给前端的字段名）
    public Boolean getIsAdmin() {
        return admin;
    }

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
