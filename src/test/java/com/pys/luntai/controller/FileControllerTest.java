package com.pys.luntai.controller;

import com.pys.luntai.service.FileService;
import com.pys.luntai.vo.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * FileController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    @Test
    void testUploadFile_Success() throws Exception {
        // 测试文件上传成功
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        when(fileService.uploadFile(any(), eq("product")))
                .thenReturn("http://localhost:8080/uploads/product/xxx.jpg");

        Result<String> result = fileController.uploadFile(file, "product");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().contains("product"));
    }

    @Test
    void testUploadFile_DefaultType() throws Exception {
        // 测试默认类型
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        when(fileService.uploadFile(any(), eq("common")))
                .thenReturn("http://localhost:8080/uploads/common/xxx.jpg");

        Result<String> result = fileController.uploadFile(file, "common");

        assertEquals(200, result.getCode());
    }

    @Test
    void testUploadFile_Failure() throws Exception {
        // 测试上传失败
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        when(fileService.uploadFile(any(), any()))
                .thenThrow(new RuntimeException("文件上传失败"));

        Result<String> result = fileController.uploadFile(file, "test");

        assertEquals(500, result.getCode());
        assertEquals("文件上传失败", result.getMessage());
    }

    @Test
    void testUploadCompanyPhoto_Success() throws Exception {
        // 测试上传公司照片
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "company.jpg",
                "image/jpeg",
                "company photo".getBytes()
        );

        when(fileService.uploadFile(any(), eq("company")))
                .thenReturn("http://localhost:8080/uploads/company/xxx.jpg");

        Result<String> result = fileController.uploadCompanyPhoto(file);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testUploadCompanyPhoto_EmptyFile() throws Exception {
        // 测试空文件
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        when(fileService.uploadFile(any(), eq("company")))
                .thenThrow(new RuntimeException("文件不能为空"));

        Result<String> result = fileController.uploadCompanyPhoto(emptyFile);

        assertEquals(500, result.getCode());
        assertEquals("文件不能为空", result.getMessage());
    }
}
