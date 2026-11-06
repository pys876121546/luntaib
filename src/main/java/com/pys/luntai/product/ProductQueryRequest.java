package com.pys.luntai.product;

import lombok.Data;

@Data
public class ProductQueryRequest {
    // 品牌、名称、规格模糊搜索，纯数字关键词支持省略规格中的 R
    private String keyword;

    // 分类：四季轮胎、冬季轮胎
    private String category;

    // 品牌
    private String brand;

    // 胎宽
    private String width;

    // 扁平比
    private String ratio;

    // 轮毂尺寸
    private String rim;

    // 是否特价
    private Boolean special;
}
