package com.pys.luntai.user;

import lombok.Data;

@Data
public class AuditRequest {
    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String companyPhotoUrl; // 公司门头照片URL
}
