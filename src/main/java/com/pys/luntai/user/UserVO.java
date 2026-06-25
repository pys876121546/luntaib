package com.pys.luntai.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserVO {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private String auditStatus;
    private Boolean isAdmin;
}
