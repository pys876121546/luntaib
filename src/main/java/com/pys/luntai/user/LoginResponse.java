package com.pys.luntai.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private Long id;
    private String token;
    private String nickname;
    private String avatarUrl;
    private String auditStatus;
    private Boolean isNewUser;
    private Boolean isAdmin;
}
