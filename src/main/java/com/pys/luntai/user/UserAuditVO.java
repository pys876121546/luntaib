package com.pys.luntai.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAuditVO {
    private Long id;
    private String status;
    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String companyPhotoUrl;
    private String auditRemark;
    private String createTime;
}
