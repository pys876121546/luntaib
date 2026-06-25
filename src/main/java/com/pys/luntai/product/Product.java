package com.pys.luntai.product;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tire_product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 分类
    private String category;

    // 商品名称
    private String name;

    // 品牌
    private String brand;

    // 胎宽
    private String width;

    // 扁平比
    private String ratio;

    // 轮毂尺寸
    private String rim;

    // 现价
    private BigDecimal price;

    // 原价（特价时存在）
    private BigDecimal originalPrice;

    // 是否特价
    private Boolean special;

    // 库存
    private Integer stock;

    // 商品图片
    private String img;

    // 规格（冗余字段，格式：品牌+型号+胎宽+扁平比R+尺寸，如固铂AT326565R18）
    private String spec;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
