package com.pys.luntai.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pys.luntai.user.User;
import com.pys.luntai.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 查询商品列表
     * @param request 查询条件
     * @param userId 用户ID（用于判断是否显示价格）
     */
    public List<ProductVO> queryProductList(ProductQueryRequest request, Long userId) {
        // 1. 判断用户是否已审核
        boolean showPrice = checkUserAuditStatus(userId);

        // 2. 构建查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 关键词模糊搜索（品牌、型号、规格），兼容省略 R 的数字规格
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(Product::getBrand, request.getKeyword())
                    .or()
                    .like(Product::getName, request.getKeyword())
                    .or()
                    .like(Product::getSpec, request.getKeyword())
                    .or(request.getKeyword().matches("[0-9]+"), compact -> compact.apply(
                            "REPLACE(REPLACE(spec, 'R', ''), 'r', '') LIKE {0}",
                            "%" + request.getKeyword() + "%")));
        }

        // 分类
        if (StringUtils.hasText(request.getCategory())) {
            wrapper.eq(Product::getCategory, request.getCategory());
        }
        if (StringUtils.hasText(request.getBrand())) {
            wrapper.eq(Product::getBrand, request.getBrand());
        }
        if (StringUtils.hasText(request.getWidth())) {
            wrapper.eq(Product::getWidth, request.getWidth());
        }
        if (StringUtils.hasText(request.getRatio())) {
            wrapper.eq(Product::getRatio, request.getRatio());
        }
        if (StringUtils.hasText(request.getRim())) {
            wrapper.eq(Product::getRim, request.getRim());
        }
        if (request.getSpecial() != null) {
            wrapper.eq(Product::getSpecial, request.getSpecial());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Product::getCreateTime);

        // 3. 执行查询
        List<Product> productList = productMapper.selectList(wrapper);

        // 4. 转换为VO并返回
        return productList.stream()
                .map(product -> convertToVO(product, showPrice))
                .collect(Collectors.toList());
    }

    /**
     * 添加商品
     */
    public void addProduct(Product product) {
        // 同步更新规格字段
        product.setSpec(generateSpec(product));
        productMapper.insert(product);
    }

    /**
     * 修改商品
     */
    public void updateProduct(Product product) {
        if (product.getId() == null) {
            throw new RuntimeException("商品ID不能为空");
        }
        // 同步更新规格字段
        product.setSpec(generateSpec(product));
        productMapper.updateById(product);
    }

    /**
     * 生成规格字符串，格式：品牌+型号+胎宽+扁平比R+尺寸，如固铂AT326565R18
     */
    private String generateSpec(Product product) {
        StringBuilder spec = new StringBuilder();
        if (StringUtils.hasText(product.getBrand())) {
            spec.append(product.getBrand());
        }
        if (StringUtils.hasText(product.getName())) {
            spec.append(product.getName());
        }
        if (StringUtils.hasText(product.getWidth())) {
            spec.append(product.getWidth());
        }
        if (StringUtils.hasText(product.getRatio())) {
            spec.append(product.getRatio()).append("R");
        }
        if (StringUtils.hasText(product.getRim())) {
            spec.append(product.getRim());
        }
        return spec.toString();
    }

    /**
     * 删除商品（单个）
     */
    public void deleteProduct(Long productId) {
        productMapper.deleteById(productId);
    }

    /**
     * 批量删除商品
     */
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("删除ID列表不能为空");
        }
        productMapper.deleteBatchIds(ids);
    }

    /**
     * 批量修改商品（逐个独立处理）
     *
     * 价格逻辑：
     * - 特价(special=true)：originalPrice 不动，只修改 price
     * - 非特价(special=false)：同时修改 originalPrice 和 price（相同值）
     */
    public void updateBatch(List<BatchUpdateRequest.Item> items) {
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("商品列表不能为空");
        }

        for (BatchUpdateRequest.Item item : items) {
            if (item.getId() == null) continue;
            
            Product product = productMapper.selectById(item.getId());
            if (product == null) continue;

            // 是否特价
            if (Boolean.TRUE.equals(item.getSpecial())) {
                product.setSpecial(true);
                // 特价：只改 price
                if (item.getPrice() != null) {
                    product.setPrice(item.getPrice());
                }
            } else {
                // 非特价：price 和 originalPrice 同步
                if (item.getPrice() != null) {
                    product.setPrice(item.getPrice());
                    product.setOriginalPrice(item.getPrice());
                }
                if (Boolean.FALSE.equals(item.getSpecial())) {
                    product.setSpecial(false);
                }
            }

            if (item.getStock() != null) {
                product.setStock(item.getStock());
            }

            productMapper.updateById(product);
        }
    }

    /**
     * 检查用户是否显示价格（已审核 或 管理员）
     */
    private boolean checkUserAuditStatus(Long userId) {
        if (userId == null) {
            return false;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        // 已审核 或 管理员 都显示价格
        return "APPROVED".equals(user.getAuditStatus()) || Boolean.TRUE.equals(user.getAdmin());
    }

    /**
     * 转换为VO，根据审核状态决定是否显示价格
     */
    private ProductVO convertToVO(Product product, boolean showPrice) {
        return ProductVO.builder()
                .id(product.getId())
                .category(product.getCategory())
                .name(product.getName())
                .brand(product.getBrand())
                .width(product.getWidth())
                .ratio(product.getRatio())
                .rim(product.getRim())
                .price(showPrice && product.getPrice() != null ? product.getPrice().toString() : "")
                .originalPrice(showPrice && product.getOriginalPrice() != null ? product.getOriginalPrice().toString() : "")
                .special(product.getSpecial())
                .stock(product.getStock())
                .img(product.getImg())
                .build();
    }

    /**
     * 获取筛选条件列表（分类、品牌、胎宽、扁平比、轮毂尺寸）
     */
    public ProductFilterVO getFilterOptions() {
        ProductFilterVO filterVO = new ProductFilterVO();
        filterVO.setCategories(productMapper.selectDistinctCategories());
        filterVO.setBrands(productMapper.selectDistinctBrands());
        filterVO.setWidths(productMapper.selectDistinctWidths());
        filterVO.setRatios(productMapper.selectDistinctRatios());
        filterVO.setRims(productMapper.selectDistinctRims());
        return filterVO;
    }
}
