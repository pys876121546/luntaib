package com.pys.luntai.user;

import com.pys.luntai.common.util.JwtUtil;
import com.pys.luntai.common.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 微信小程序登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> wxLogin(@RequestBody WxLoginRequest request) {
        try {
            LoginResponse response = userService.wxLogin(request);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 提交审核申请
     */
    @PostMapping("/audit")
    public Result<Void> submitAudit(@RequestBody AuditRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            userService.submitAudit(userId, request);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            UserVO user = userService.getUserInfo(userId);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户审核信息
     */
    @GetMapping("/audit/info")
    public Result<UserAuditVO> getUserAuditInfo(HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            UserAuditVO auditInfo = userService.getUserAuditInfo(userId);
            return Result.success(auditInfo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 通过审核（管理员）
     */
    @PostMapping("/audit/approve")
    public Result<Void> approveAudit(@RequestBody AdminAuditRequest request, HttpServletRequest httpRequest) {
        try {
            Long adminId = getCurrentUserId(httpRequest);
            userService.approveAudit(adminId, request.getUserId());
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 驳回审核（管理员）
     */
    @PostMapping("/audit/reject")
    public Result<Void> rejectAudit(@RequestBody AdminAuditRequest request, HttpServletRequest httpRequest) {
        try {
            Long adminId = getCurrentUserId(httpRequest);
            userService.rejectAudit(adminId, request.getUserId());
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从请求头获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未登录");
        }
        token = token.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Token无效");
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
