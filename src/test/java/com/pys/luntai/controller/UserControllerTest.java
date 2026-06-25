package com.pys.luntai.controller;

import com.pys.luntai.dto.AdminAuditRequest;
import com.pys.luntai.dto.AuditRequest;
import com.pys.luntai.dto.WxLoginRequest;
import com.pys.luntai.entity.User;
import com.pys.luntai.service.UserService;
import com.pys.luntai.util.JwtUtil;
import com.pys.luntai.vo.LoginResponse;
import com.pys.luntai.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserController userController;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
    }

    @Test
    void testWxLogin_Success() {
        // 测试登录成功
        WxLoginRequest loginRequest = new WxLoginRequest();
        loginRequest.setCode("test_code");

        LoginResponse mockResponse = LoginResponse.builder()
                .userId(1L)
                .token("test_token")
                .nickname("测试用户")
                .auditStatus("NONE")
                .isNewUser(true)
                .admin(false)
                .build();

        when(userService.wxLogin(any())).thenReturn(mockResponse);

        Result<LoginResponse> result = userController.wxLogin(loginRequest);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("test_token", result.getData().getToken());
    }

    @Test
    void testWxLogin_Failure() {
        // 测试登录失败
        WxLoginRequest loginRequest = new WxLoginRequest();
        loginRequest.setCode("invalid_code");

        when(userService.wxLogin(any())).thenThrow(new RuntimeException("微信登录失败"));

        Result<LoginResponse> result = userController.wxLogin(loginRequest);

        assertEquals(500, result.getCode());
        assertEquals("微信登录失败", result.getMessage());
    }

    @Test
    void testSubmitAudit_Success() {
        // 测试提交审核成功
        request.addHeader("Authorization", "Bearer valid_token");

        when(jwtUtil.validateToken("valid_token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid_token")).thenReturn(1L);
        doNothing().when(userService).submitAudit(any(), any());

        AuditRequest auditRequest = new AuditRequest();
        auditRequest.setCompanyName("测试公司");

        Result<Void> result = userController.submitAudit(auditRequest, request);

        assertEquals(200, result.getCode());
    }

    @Test
    void testSubmitAudit_NoToken() {
        // 测试没有token
        AuditRequest auditRequest = new AuditRequest();

        Result<Void> result = userController.submitAudit(auditRequest, request);

        assertEquals(500, result.getCode());
        assertEquals("未登录", result.getMessage());
    }

    @Test
    void testSubmitAudit_InvalidToken() {
        // 测试无效token
        request.addHeader("Authorization", "Bearer invalid_token");
        when(jwtUtil.validateToken("invalid_token")).thenReturn(false);

        AuditRequest auditRequest = new AuditRequest();

        Result<Void> result = userController.submitAudit(auditRequest, request);

        assertEquals(500, result.getCode());
        assertEquals("Token无效", result.getMessage());
    }

    @Test
    void testGetUserInfo_Success() {
        // 测试获取用户信息成功
        request.addHeader("Authorization", "Bearer valid_token");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setNickname("测试用户");
        mockUser.setAuditStatus("APPROVED");

        when(jwtUtil.validateToken("valid_token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid_token")).thenReturn(1L);
        when(userService.getUserInfo(1L)).thenReturn(mockUser);

        Result<User> result = userController.getUserInfo(request);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("测试用户", result.getData().getNickname());
    }

    @Test
    void testGetUserInfo_NotLogin() {
        // 测试未登录
        Result<User> result = userController.getUserInfo(request);

        assertEquals(500, result.getCode());
    }

    @Test
    void testApproveAudit_Success() {
        // 测试通过审核成功
        request.addHeader("Authorization", "Bearer valid_token");

        when(jwtUtil.validateToken("valid_token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid_token")).thenReturn(1L);
        doNothing().when(userService).approveAudit(any(), any());

        AdminAuditRequest auditRequest = new AdminAuditRequest();
        auditRequest.setUserId(2L);

        Result<Void> result = userController.approveAudit(auditRequest, request);

        assertEquals(200, result.getCode());
    }

    @Test
    void testApproveAudit_NotAdmin() {
        // 测试非管理员无法通过审核
        request.addHeader("Authorization", "Bearer valid_token");

        when(jwtUtil.validateToken("valid_token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid_token")).thenReturn(1L);
        doThrow(new RuntimeException("无权限操作")).when(userService).approveAudit(any(), any());

        AdminAuditRequest auditRequest = new AdminAuditRequest();
        auditRequest.setUserId(2L);

        Result<Void> result = userController.approveAudit(auditRequest, request);

        assertEquals(500, result.getCode());
        assertEquals("无权限操作", result.getMessage());
    }

    @Test
    void testRejectAudit_Success() {
        // 测试驳回审核成功
        request.addHeader("Authorization", "Bearer valid_token");

        when(jwtUtil.validateToken("valid_token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid_token")).thenReturn(1L);
        doNothing().when(userService).rejectAudit(any(), any());

        AdminAuditRequest auditRequest = new AdminAuditRequest();
        auditRequest.setUserId(2L);

        Result<Void> result = userController.rejectAudit(auditRequest, request);

        assertEquals(200, result.getCode());
    }

    @Test
    void testRejectAudit_NotAdmin() {
        // 测试非管理员无法驳回审核
        request.addHeader("Authorization", "Bearer valid_token");

        when(jwtUtil.validateToken("valid_token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid_token")).thenReturn(1L);
        doThrow(new RuntimeException("无权限操作")).when(userService).rejectAudit(any(), any());

        AdminAuditRequest auditRequest = new AdminAuditRequest();
        auditRequest.setUserId(2L);

        Result<Void> result = userController.rejectAudit(auditRequest, request);

        assertEquals(500, result.getCode());
        assertEquals("无权限操作", result.getMessage());
    }
}
