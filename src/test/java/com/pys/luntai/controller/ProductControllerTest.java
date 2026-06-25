package com.pys.luntai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pys.luntai.dto.BatchUpdateRequest;
import com.pys.luntai.dto.ProductQueryRequest;
import com.pys.luntai.entity.Product;
import com.pys.luntai.service.ProductService;
import com.pys.luntai.util.JwtUtil;
import com.pys.luntai.vo.ProductVO;
import com.pys.luntai.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProductController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ProductController productController;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
    }

    @Test
    void testList_Success() {
        // 测试查询商品列表
        ProductQueryRequest queryRequest = new ProductQueryRequest();
        queryRequest.setPageNum(1);
        queryRequest.setPageSize(10);

        Page<ProductVO> mockPage = new Page<>();
        mockPage.setTotal(100);
        mockPage.setRecords(Collections.emptyList());

        when(productService.queryProductList(any(), any())).thenReturn(mockPage);

        Result<Page<ProductVO>> result = productController.list(queryRequest, request);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testAdd_Success() {
        // 测试添加商品
        Product product = new Product();
        product.setName("测试轮胎");

        doNothing().when(productService).addProduct(any());

        Result<Void> result = productController.add(product);

        assertEquals(200, result.getCode());
    }

    @Test
    void testAdd_Failure() {
        // 测试添加失败
        Product product = new Product();

        doThrow(new RuntimeException("商品名称不能为空")).when(productService).addProduct(any());

        Result<Void> result = productController.add(product);

        assertEquals(500, result.getCode());
        assertEquals("商品名称不能为空", result.getMessage());
    }

    @Test
    void testUpdate_Success() {
        // 测试修改商品
        Long id = 1L;
        Product product = new Product();
        product.setName("更新后的名称");

        doNothing().when(productService).updateProduct(any());

        Result<Void> result = productController.update(id, product);

        assertEquals(200, result.getCode());
        assertEquals(id, product.getId());
    }

    @Test
    void testDelete_Success() {
        // 测试删除商品
        Long id = 1L;

        doNothing().when(productService).deleteProduct(id);

        Result<Void> result = productController.delete(id);

        assertEquals(200, result.getCode());
    }

    @Test
    void testDeleteBatch_Success() {
        // 测试批量删除
        var ids = Arrays.asList(1L, 2L, 3L);

        doNothing().when(productService).deleteBatch(ids);

        Result<Void> result = productController.deleteBatch(ids);

        assertEquals(200, result.getCode());
    }

    @Test
    void testDeleteBatch_Empty() {
        // 测试批量删除空列表
        doThrow(new RuntimeException("删除ID列表不能为空"))
                .when(productService).deleteBatch(Collections.emptyList());

        Result<Void> result = productController.deleteBatch(Collections.emptyList());

        assertEquals(500, result.getCode());
    }

    @Test
    void testUpdateBatch_Success() {
        // 测试批量修改
        BatchUpdateRequest updateRequest = new BatchUpdateRequest();
        updateRequest.setIds(Arrays.asList(1L, 2L));
        updateRequest.setStock(100);

        doNothing().when(productService).updateBatch(any());

        Result<Void> result = productController.updateBatch(updateRequest);

        assertEquals(200, result.getCode());
    }

    @Test
    void testUpdateBatch_NoField() {
        // 测试批量修改无字段
        BatchUpdateRequest updateRequest = new BatchUpdateRequest();
        updateRequest.setIds(Arrays.asList(1L, 2L));

        doThrow(new RuntimeException("至少需要指定一个要修改的字段"))
                .when(productService).updateBatch(any());

        Result<Void> result = productController.updateBatch(updateRequest);

        assertEquals(500, result.getCode());
    }
}
