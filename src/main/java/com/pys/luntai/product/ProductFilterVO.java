package com.pys.luntai.product;

import lombok.Data;

import java.util.List;

@Data
public class ProductFilterVO {
    // 分类列表
    private List<String> categories;
    // 品牌列表
    private List<String> brands;
    // 胎宽列表
    private List<String> widths;
    // 扁平比列表
    private List<String> ratios;
    // 轮毂尺寸列表
    private List<String> rims;
}
