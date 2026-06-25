package com.pys.luntai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileService 单元测试
 */
class FileServiceTest {

    private FileService fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileService = new FileService();
        ReflectionTestUtils.setField(fileService, "uploadPath", tempDir.toString());
        ReflectionTestUtils.setField(fileService, "serverUrl", "http://localhost:8080");
    }

    @Test
    void testUploadFile_Success() throws IOException {
        // 测试正常上传图片
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        String fileUrl = fileService.uploadFile(file, "test");

        assertNotNull(fileUrl);
        assertTrue(fileUrl.startsWith("http://localhost:8080/uploads/"));
        assertTrue(fileUrl.contains("test/"));
        assertTrue(fileUrl.endsWith(".jpg"));
    }

    @Test
    void testUploadFile_EmptyFile() {
        // 测试空文件
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fileService.uploadFile(emptyFile, "test");
        });

        assertEquals("文件不能为空", exception.getMessage());
    }

    @Test
    void testUploadFile_NotImage() {
        // 测试非图片文件
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "text content".getBytes()
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fileService.uploadFile(textFile, "test");
        });

        assertEquals("只允许上传图片文件", exception.getMessage());
    }

    @Test
    void testUploadFile_PngImage() throws IOException {
        // 测试上传PNG图片
        MockMultipartFile pngFile = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "png image content".getBytes()
        );

        String fileUrl = fileService.uploadFile(pngFile, "avatar");

        assertNotNull(fileUrl);
        assertTrue(fileUrl.endsWith(".png"));
    }

    @Test
    void testUploadFile_GifImage() throws IOException {
        // 测试上传GIF图片
        MockMultipartFile gifFile = new MockMultipartFile(
                "file",
                "test.gif",
                "image/gif",
                "gif image content".getBytes()
        );

        String fileUrl = fileService.uploadFile(gifFile, "product");

        assertNotNull(fileUrl);
        assertTrue(fileUrl.endsWith(".gif"));
    }

    @Test
    void testDeleteFile_Success() throws IOException {
        // 测试删除文件
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "delete_test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        String fileUrl = fileService.uploadFile(file, "test");
        assertNotNull(fileUrl);

        // 删除文件
        assertDoesNotThrow(() -> fileService.deleteFile(fileUrl));
    }

    @Test
    void testDeleteFile_NotExists() {
        // 测试删除不存在的文件
        String notExistUrl = "http://localhost:8080/uploads/test/not_exist.jpg";

        // 不应该抛出异常
        assertDoesNotThrow(() -> fileService.deleteFile(notExistUrl));
    }

    @Test
    void testUploadFile_CreateDirectory() throws IOException {
        // 测试自动创建目录
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deep/nested/path.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        String fileUrl = fileService.uploadFile(file, "company");

        assertNotNull(fileUrl);
        assertTrue(fileUrl.contains("company/"));
    }
}
