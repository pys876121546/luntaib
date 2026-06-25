package com.pys.luntai.product;

import com.pys.luntai.common.util.JwtUtil;
import com.pys.luntai.common.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 查询商品列表（无需登录，未登录价格返回空字符串）
     */
    @GetMapping("/list")
    public Result<List<ProductVO>> queryProductList(
            ProductQueryRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getOptionalUserId(httpRequest);
            List<ProductVO> result = productService.queryProductList(request, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加商品
     */
    @PostMapping("/add")
    public Result<Void> addProduct(@RequestBody Product product, HttpServletRequest httpRequest) {
        try {
            checkAdmin(httpRequest);
            productService.addProduct(product);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改商品
     */
    @PostMapping("/update/{id}")
    public Result<Void> updateProduct(@PathVariable Long id, @RequestBody Product product, HttpServletRequest httpRequest) {
        try {
            checkAdmin(httpRequest);
            product.setId(id);
            productService.updateProduct(product);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量修改商品
     */
    @PostMapping("/update/batch")
    public Result<Void> updateBatch(@RequestBody List<BatchUpdateRequest.Item> items, HttpServletRequest httpRequest) {
        try {
            checkAdmin(httpRequest);
            productService.updateBatch(items);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除商品（单个）
     */
    @PostMapping("/delete/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            checkAdmin(httpRequest);
            productService.deleteProduct(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除商品
     */
    @PostMapping("/delete/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids, HttpServletRequest httpRequest) {
        try {
            checkAdmin(httpRequest);
            productService.deleteBatch(ids);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取筛选条件列表
     */
    @GetMapping("/filters")
    public Result<ProductFilterVO> getFilterOptions() {
        try {
            ProductFilterVO filterVO = productService.getFilterOptions();
            return Result.success(filterVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查是否为管理员
     */
    private void checkAdmin(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        com.pys.luntai.user.User admin = userMapper.selectById(userId);
        if (admin == null || !Boolean.TRUE.equals(admin.getAdmin())) {
            throw new RuntimeException("无权限操作");
        }
    }

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

    /**
     * 获取用户ID（可选，未登录返回null）
     */
    private Long getOptionalUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        token = token.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    @Autowired
    private com.pys.luntai.user.UserMapper userMapper;
}