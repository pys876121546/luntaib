package com.pys.luntai.service;

import com.pys.luntai.dto.AuditRequest;
import com.pys.luntai.dto.WxLoginRequest;
import com.pys.luntai.entity.User;
import com.pys.luntai.mapper.UserMapper;
import com.pys.luntai.util.JwtUtil;
import com.pys.luntai.vo.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "appid", "test_appid");
        ReflectionTestUtils.setField(userService, "secret", "test_secret");
    }

    @Test
    void testGetUserInfo_UserExists() {
        // 测试获取存在的用户
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setNickname("测试用户");

        when(userMapper.selectById(userId)).thenReturn(mockUser);

        User result = userService.getUserInfo(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("测试用户", result.getNickname());
    }

    @Test
    void testGetUserInfo_UserNotExists() {
        // 测试获取不存在的用户
        Long userId = 999L;
        when(userMapper.selectById(userId)).thenReturn(null);

        User result = userService.getUserInfo(userId);

        assertNull(result);
    }

    @Test
    void testSubmitAudit_Success() {
        // 测试提交审核成功
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setAuditStatus("NONE");

        AuditRequest request = new AuditRequest();
        request.setCompanyName("测试公司");
        request.setCompanyAddress("测试地址");
        request.setCompanyPhone("12345678901");

        when(userMapper.selectById(userId)).thenReturn(user);

        userService.submitAudit(userId, request);

        assertEquals("PENDING", user.getAuditStatus());
        assertEquals("测试公司", user.getCompanyName());
        verify(userMapper).updateById(user);
    }

    @Test
    void testSubmitAudit_UserNotFound() {
        // 测试用户不存在
        Long userId = 999L;
        AuditRequest request = new AuditRequest();

        when(userMapper.selectById(userId)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.submitAudit(userId, request);
        });

        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void testSubmitAudit_InvalidStatus() {
        // 测试无效状态提交审核
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setAuditStatus("PENDING");

        AuditRequest request = new AuditRequest();

        when(userMapper.selectById(userId)).thenReturn(user);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.submitAudit(userId, request);
        });

        assertEquals("当前状态无法提交审核", exception.getMessage());
    }

    @Test
    void testSubmitAudit_RejectedCanResubmit() {
        // 测试被拒绝后可以重新提交
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setAuditStatus("REJECTED");

        AuditRequest request = new AuditRequest();
        request.setCompanyName("新公司名");

        when(userMapper.selectById(userId)).thenReturn(user);

        assertDoesNotThrow(() -> userService.submitAudit(userId, request));
        assertEquals("PENDING", user.getAuditStatus());
    }

    @Test
    void testApproveAudit_Success() {
        // 测试通过审核成功
        Long adminId = 1L;
        Long userId = 2L;

        User admin = new User();
        admin.setId(adminId);
        admin.setAdmin(true);

        User user = new User();
        user.setId(userId);
        user.setAuditStatus("PENDING");

        when(userMapper.selectById(adminId)).thenReturn(admin);
        when(userMapper.selectById(userId)).thenReturn(user);

        userService.approveAudit(adminId, userId);

        assertEquals("APPROVED", user.getAuditStatus());
        verify(userMapper).updateById(user);
    }

    @Test
    void testApproveAudit_NotAdmin() {
        // 测试非管理员无法审核
        Long adminId = 1L;
        Long userId = 2L;

        User admin = new User();
        admin.setId(adminId);
        admin.setAdmin(false);

        when(userMapper.selectById(adminId)).thenReturn(admin);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.approveAudit(adminId, userId);
        });

        assertEquals("无权限操作", exception.getMessage());
    }

    @Test
    void testApproveAudit_UserNotFound() {
        // 测试用户不存在
        Long adminId = 1L;
        Long userId = 999L;

        User admin = new User();
        admin.setId(adminId);
        admin.setAdmin(true);

        when(userMapper.selectById(adminId)).thenReturn(admin);
        when(userMapper.selectById(userId)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.approveAudit(adminId, userId);
        });

        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void testApproveAudit_InvalidStatus() {
        // 测试非PENDING状态无法审核
        Long adminId = 1L;
        Long userId = 2L;

        User admin = new User();
        admin.setId(adminId);
        admin.setAdmin(true);

        User user = new User();
        user.setId(userId);
        user.setAuditStatus("APPROVED");

        when(userMapper.selectById(adminId)).thenReturn(admin);
        when(userMapper.selectById(userId)).thenReturn(user);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.approveAudit(adminId, userId);
        });

        assertEquals("当前状态无法审核", exception.getMessage());
    }

    @Test
    void testRejectAudit_Success() {
        // 测试驳回审核成功
        Long adminId = 1L;
        Long userId = 2L;

        User admin = new User();
        admin.setId(adminId);
        admin.setAdmin(true);

        User user = new User();
        user.setId(userId);
        user.setAuditStatus("PENDING");

        when(userMapper.selectById(adminId)).thenReturn(admin);
        when(userMapper.selectById(userId)).thenReturn(user);

        userService.rejectAudit(adminId, userId);

        assertEquals("REJECTED", user.getAuditStatus());
        verify(userMapper).updateById(user);
    }

    @Test
    void testRejectAudit_NotAdmin() {
        // 测试非管理员无法驳回
        Long adminId = 1L;
        Long userId = 2L;

        User admin = new User();
        admin.setId(adminId);
        admin.setAdmin(false);

        when(userMapper.selectById(adminId)).thenReturn(admin);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.rejectAudit(adminId, userId);
        });

        assertEquals("无权限操作", exception.getMessage());
    }
}
