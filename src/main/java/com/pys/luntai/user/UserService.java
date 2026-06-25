package com.pys.luntai.user;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pys.luntai.audit.AuditRecord;
import com.pys.luntai.audit.AuditService;
import com.pys.luntai.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService {

    @Value("${wx.appid:your-appid}")
    private String appid;

    @Value("${wx.secret:your-secret}")
    private String secret;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuditService auditService;

    @Autowired
    private JwtUtil jwtUtil;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 微信小程序登录
     */
    public LoginResponse wxLogin(WxLoginRequest request) {
        // 1. 调用微信接口获取openid
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, request.getCode()
        );

        String response = restTemplate.getForObject(url, String.class);
        JSONObject json = JSONObject.parseObject(response);

        if (json.containsKey("errcode")) {
            throw new RuntimeException("微信登录失败: " + json.getString("errmsg"));
        }

        String openid = json.getString("openid");
        String unionid = json.getString("unionid");

        // 2. 查询用户是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);

        boolean isNewUser = false;

        if (user == null) {
            // 3. 新用户注册
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(unionid);
            user.setNickname(request.getNickname());
            user.setAvatarUrl(request.getAvatarUrl());
            user.setAuditStatus("NONE"); // 默认未申请审核
            userMapper.insert(user);
            isNewUser = true;
        } else {
            // 4. 补充缺失的用户信息（不覆盖已有值）
            if (user.getNickname() == null && request.getNickname() != null) {
                user.setNickname(request.getNickname());
            }
            if (user.getAvatarUrl() == null && request.getAvatarUrl() != null) {
                user.setAvatarUrl(request.getAvatarUrl());
            }
            userMapper.updateById(user);
        }

        // 5. 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), openid);

        return LoginResponse.builder()
                .id(user.getId())
                .token(token)
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .auditStatus(user.getAuditStatus())
                .isNewUser(isNewUser)
                .isAdmin(user.getAdmin())
                .build();
    }

    /**
     * 提交审核申请
     */
    public void submitAudit(Long userId, AuditRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 只能从未审核状态或已拒绝状态提交
        if (!"NONE".equals(user.getAuditStatus()) && !"REJECTED".equals(user.getAuditStatus())) {
            throw new RuntimeException("当前状态无法提交审核");
        }

        // 创建审批记录
        auditService.submitAudit(userId, request.getCompanyName(), request.getCompanyAddress(),
                request.getCompanyPhone(), request.getCompanyPhotoUrl());

        // 更新用户审核状态
        user.setAuditStatus("PENDING");
        user.setCompanyName(request.getCompanyName());
        user.setCompanyAddress(request.getCompanyAddress());
        user.setCompanyPhone(request.getCompanyPhone());
        user.setCompanyPhotoUrl(request.getCompanyPhotoUrl());
        user.setAuditRemark(null);
        userMapper.updateById(user);
    }

    /**
     * 获取用户信息
     */
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        return UserVO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .auditStatus(user.getAuditStatus())
                .isAdmin(user.getAdmin())
                .build();
    }

    /**
     * 获取当前用户审核信息
     */
    public UserAuditVO getUserAuditInfo(Long userId) {
        AuditRecord record = auditService.getLatestByUserId(userId);
        if (record == null) {
            return null;
        }
        return UserAuditVO.builder()
                .id(record.getId())
                .status(record.getStatus())
                .companyName(record.getCompanyName())
                .companyAddress(record.getCompanyAddress())
                .companyPhone(record.getCompanyPhone())
                .companyPhotoUrl(record.getCompanyPhotoUrl())
                .auditRemark(record.getAuditRemark())
                .createTime(record.getCreateTime() != null ? 
                    record.getCreateTime().toString().replace("T", " ") : null)
                .build();
    }

    /**
     * 通过审核（管理员）- 保留此方法用于兼容旧的API调用
     * 建议使用 AuditService 的方法直接操作审批记录
     */
    public void approveAudit(Long adminId, Long userId) {
        // 检查操作者是否为管理员
        User admin = userMapper.selectById(adminId);
        if (admin == null || !Boolean.TRUE.equals(admin.getAdmin())) {
            throw new RuntimeException("无权限操作");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!"PENDING".equals(user.getAuditStatus())) {
            throw new RuntimeException("当前状态无法审核");
        }

        // 获取用户的最新审批记录并更新
        AuditRecord record = auditService.getLatestByUserId(userId);
        if (record != null) {
            auditService.approveAudit(adminId, record.getId());
        } else {
            user.setAuditStatus("APPROVED");
            user.setAuditRemark(null);
            userMapper.updateById(user);
        }
    }

    /**
     * 驳回审核（管理员）- 保留此方法用于兼容旧的API调用
     * 建议使用 AuditService 的方法直接操作审批记录
     */
    public void rejectAudit(Long adminId, Long userId) {
        // 检查操作者是否为管理员
        User admin = userMapper.selectById(adminId);
        if (admin == null || !Boolean.TRUE.equals(admin.getAdmin())) {
            throw new RuntimeException("无权限操作");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!"PENDING".equals(user.getAuditStatus())) {
            throw new RuntimeException("当前状态无法审核");
        }

        // 获取用户的最新审批记录并更新
        AuditRecord record = auditService.getLatestByUserId(userId);
        if (record != null) {
            auditService.rejectAudit(adminId, record.getId());
        } else {
            user.setAuditStatus("REJECTED");
            user.setAuditRemark(null);
            userMapper.updateById(user);
        }
    }
}
