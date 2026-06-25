package com.pys.luntai.user;

import lombok.Data;

@Data
public class WxLoginRequest {
    // 微信登录凭证
    private String code;
    
    // 用户信息（需前端授权获取）
    private String nickname;
    private String avatarUrl;
}
