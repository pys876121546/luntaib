package com.pys.luntai.audit;

import com.pys.luntai.common.util.JwtUtil;
import com.pys.luntai.common.vo.Result;
import com.pys.luntai.user.User;
import com.pys.luntai.user.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取审批列表（管理员）
     */
    @GetMapping("/list")
    public Result<List<AuditRecordVO>> getAuditList(
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        try {
            Long adminId = getCurrentUserId(request);
            checkAdmin(adminId);

            List<AuditRecordVO> result = auditService.getAuditList(status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取待审核数量（管理员）
     */
    @GetMapping("/pending/count")
    public Result<Long> getPendingCount(HttpServletRequest request) {
        try {
            Long adminId = getCurrentUserId(request);
            checkAdmin(adminId);

            long count = auditService.getPendingCount();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 通过审核（管理员）
     */
    @PostMapping("/approve/{id}")
    public Result<Void> approveAudit(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long adminId = getCurrentUserId(request);
            checkAdmin(adminId);

            auditService.approveAudit(adminId, id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 驳回审核（管理员）
     */
    @PostMapping("/reject/{id}")
    public Result<Void> rejectAudit(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long adminId = getCurrentUserId(request);
            checkAdmin(adminId);

            auditService.rejectAudit(adminId, id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重置已通过审核（管理员）
     */
    @PostMapping("/reset/{id}")
    public Result<Void> resetAudit(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long adminId = getCurrentUserId(request);
            checkAdmin(adminId);

            auditService.resetAudit(adminId, id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查是否为管理员
     */
    private void checkAdmin(Long userId) {
        User admin = userMapper.selectById(userId);
        if (admin == null || !Boolean.TRUE.equals(admin.getAdmin())) {
            throw new RuntimeException("无权限操作");
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

    @Data
    public static class RejectRequest {
        private String remark;
    }
}