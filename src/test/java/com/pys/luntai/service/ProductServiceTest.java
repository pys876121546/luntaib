package com.pys.luntai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pys.luntai.dto.BatchUpdateRequest;
import com.pys.luntai.dto.ProductQueryRequest;
import com.pys.luntai.entity.Product;
import com.pys.luntai.entity.User;
import com.pys.luntai.mapper.ProductMapper;
import com.pys.luntai.mapper.UserMapper;
import com.pys.luntai.vo.ProductVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProductService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ProductService productService;

    private Product createTestProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("米其林 PS4");
        product.setBrand("米其林");
        product.setPrice(new BigDecimal("599.00"));
        product.setOriginalPrice(new BigDecimal("699.00"));
        product.setStock(100);
        product.setSpecial(true);
        return product;
    }

    @Test
    void testAddProduct() {
        // 测试添加商品
        Product product = createTestProduct();

        productService.addProduct(product);

        verify(productMapper).insert(product);
    }

    @Test
    void testUpdateProduct_Success() {
        // 测试修改商品成功
        Product product = createTestProduct();

        productService.updateProduct(product);

        verify(productMapper).updateById(product);
    }

    @Test
    void testUpdateProduct_NullId() {
        // 测试商品ID为空
        Product product = new Product();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(product);
        });

        assertEquals("商品ID不能为空", exception.getMessage());
    }

    @Test
    void testDeleteProduct() {
        // 测试删除商品
        Long productId = 1L;

        productService.deleteProduct(productId);

        verify(productMapper).deleteById(productId);
    }

    @Test
    void testDeleteBatch_Success() {
        // 测试批量删除成功
        var ids = Arrays.asList(1L, 2L, 3L);

        productService.deleteBatch(ids);

        verify(productMapper).deleteBatchIds(ids);
    }

    @Test
    void testDeleteBatch_EmptyList() {
        // 测试空列表
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.deleteBatch(Collections.emptyList());
        });

        assertEquals("删除ID列表不能为空", exception.getMessage());
    }

    @Test
    void testDeleteBatch_NullList() {
        // 测试null列表
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.deleteBatch(null);
        });

        assertEquals("删除ID列表不能为空", exception.getMessage());
    }

    @Test
    void testUpdateBatch_Success() {
        // 测试批量修改成功
        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setIds(Arrays.asList(1L, 2L));
        request.setStock(50);
        request.setPrice(new BigDecimal("499.00"));

        assertDoesNotThrow(() -> productService.updateBatch(request));
        verify(productMapper).update(any(Product.class), any());
    }

    @Test
    void testUpdateBatch_EmptyIds() {
        // 测试空ID列表
        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setIds(Collections.emptyList());
        request.setStock(50);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.updateBatch(request);
        });

        assertEquals("商品ID列表不能为空", exception.getMessage());
    }

    @Test
    void testUpdateBatch_NoUpdateField() {
        // 测试没有更新字段
        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setIds(Arrays.asList(1L, 2L));
        // 不设置任何更新字段

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.updateBatch(request);
        });

        assertEquals("至少需要指定一个要修改的字段", exception.getMessage());
    }

    @Test
    void testUpdateBatch_OnlySpecial() {
        // 测试只更新特价状态
        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setIds(Arrays.asList(1L, 2L));
        request.setSpecial(true);

        assertDoesNotThrow(() -> productService.updateBatch(request));
        verify(productMapper).update(any(Product.class), any());
    }

    @Test
    void testUpdateBatch_SpecialWithPrice() {
        // 测试设置特价并修改价格（原price移到originalPrice）
        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setIds(Arrays.asList(1L));
        request.setSpecial(true);
        request.setPrice(new BigDecimal("399.00"));

        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setPrice(new BigDecimal("599.00"));
        existingProduct.setOriginalPrice(null);
        existingProduct.setSpecial(false);

        when(productMapper.selectById(1L)).thenReturn(existingProduct);

        productService.updateBatch(request);

        // 验证：原price移到originalPrice，新price设置
        assertEquals(new BigDecimal("399.00"), existingProduct.getPrice());
        assertEquals(new BigDecimal("599.00"), existingProduct.getOriginalPrice());
        assertTrue(existingProduct.getSpecial());
        verify(productMapper).updateById(existingProduct);
    }

    @Test
    void testUpdateBatch_SpecialWithPriceAndStock() {
        // 测试设置特价、修改价格、同时修改库存
        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setIds(Arrays.asList(1L, 2L));
        request.setSpecial(true);
        request.setPrice(new BigDecimal("299.00"));
        request.setStock(50);

        Product product1 = new Product();
        product1.setId(1L);
        product1.setPrice(new BigDecimal("500.00"));
        product1.setStock(100);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setPrice(new BigDecimal("600.00"));
        product2.setStock(200);

        when(productMapper.selectById(1L)).thenReturn(product1);
        when(productMapper.selectById(2L)).thenReturn(product2);

        productService.updateBatch(request);

        // 验证商品1
        assertEquals(new BigDecimal("299.00"), product1.getPrice());
        assertEquals(new BigDecimal("500.00"), product1.getOriginalPrice());
        assertEquals(50, product1.getStock());
        assertTrue(product1.getSpecial());

        // 验证商品2
        assertEquals(new BigDecimal("299.00"), product2.getPrice());
        assertEquals(new BigDecimal("600.00"), product2.getOriginalPrice());
        assertEquals(50, product2.getStock());
        assertTrue(product2.getSpecial());

        verify(productMapper, times(2)).updateById(any(Product.class));
    }

    @Test
    void testUpdateBatch_SpecialWithNullOriginalPrice() {
        // 测试原originalPrice为null时设置特价
        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setIds(Arrays.asList(1L));
        request.setSpecial(true);
        request.setPrice(new BigDecimal("199.00"));

        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setPrice(new BigDecimal("399.00"));
        existingProduct.setOriginalPrice(null);

        when(productMapper.selectById(1L)).thenReturn(existingProduct);

        productService.updateBatch(request);

        assertEquals(new BigDecimal("199.00"), existingProduct.getPrice());
        assertEquals(new BigDecimal("399.00"), existingProduct.getOriginalPrice());
    }

    @Test
    void testCheckUserAuditStatus_NullUserId() {
        // 测试null用户ID
        ProductQueryRequest request = new ProductQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        Page<Product> emptyPage = new Page<>();
        when(productMapper.selectPage(any(), any())).thenReturn(emptyPage);

        Page<ProductVO> result = productService.queryProductList(request, null);

        assertNotNull(result);
        // 未登录用户不显示价格
    }

    @Test
    void testCheckUserAuditStatus_NotApproved() {
        // 测试未审核用户
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setAuditStatus("PENDING");

        when(userMapper.selectById(userId)).thenReturn(user);

        ProductQueryRequest request = new ProductQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        Page<Product> emptyPage = new Page<>();
        when(productMapper.selectPage(any(), any())).thenReturn(emptyPage);

        Page<ProductVO> result = productService.queryProductList(request, userId);

        assertNotNull(result);
        // PENDING状态不显示价格
    }

    @Test
    void testCheckUserAuditStatus_Approved() {
        // 测试已审核用户
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setAuditStatus("APPROVED");

        when(userMapper.selectById(userId)).thenReturn(user);

        ProductQueryRequest request = new ProductQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        Page<Product> emptyPage = new Page<>();
        when(productMapper.selectPage(any(), any())).thenReturn(emptyPage);

        Page<ProductVO> result = productService.queryProductList(request, userId);

        assertNotNull(result);
        // APPROVED状态显示价格
    }
}
