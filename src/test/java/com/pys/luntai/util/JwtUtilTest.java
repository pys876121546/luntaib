package com.pys.luntai.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 单元测试
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "test-secret-key-must-be-at-least-32-bytes-long";
    private static final Long TEST_EXPIRATION = 3600000L; // 1小时

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    @Test
    void testGenerateToken() {
        // 正常生成token
        Long userId = 1L;
        String openid = "test_openid_123";

        String token = jwtUtil.generateToken(userId, openid);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testParseToken() {
        // 生成并解析token
        Long userId = 1L;
        String openid = "test_openid_123";
        String token = jwtUtil.generateToken(userId, openid);

        var claims = jwtUtil.parseToken(token);

        assertNotNull(claims);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(openid, claims.get("openid"));
    }

    @Test
    void testGetUserIdFromToken() {
        // 从token获取用户ID
        Long userId = 100L;
        String token = jwtUtil.generateToken(userId, "openid");

        Long extractedUserId = jwtUtil.getUserIdFromToken(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void testValidateToken_ValidToken() {
        // 验证有效token
        String token = jwtUtil.generateToken(1L, "openid");

        boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // 验证无效token
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtUtil.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void testValidateToken_EmptyToken() {
        // 验证空token
        boolean isValid = jwtUtil.validateToken("");

        assertFalse(isValid);
    }
}
