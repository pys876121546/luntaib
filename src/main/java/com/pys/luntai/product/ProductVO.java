package com.pys.luntai.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductVO {
    private Long id;
    private String category;
    private String name;
    private String brand;
    private String width;
    private String ratio;
    private String rim;
    private String price;
    private String originalPrice;
    private Boolean special;
    private Integer stock;
    private String img;
}
