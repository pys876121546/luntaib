package com.pys.luntai.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批量修改商品请求（逐个独立修改）
 */
@Data
public class BatchUpdateRequest {
    
    /**
     * 商品修改列表，每项独立处理
     */
    private List<Item> items;
    
    @Data
    public static class Item {
        /** 商品ID */
        private Long id;
        
        /** 库存数量（null表示不修改） */
        private Integer stock;
        
        /** 现价（null表示不修改）
         *  非特价时：originalPrice 和 price 都改为此值
         *  特价时：仅 price 改为此值，originalPrice 不变
         */
        private BigDecimal price;
        
        /** 是否特价：false=非特价，true=特价（null表示不修改）
         *  非特价：price和originalPrice同步
         *  特价：只改price，originalPrice不变
         */
        private Boolean special;
    }
}
