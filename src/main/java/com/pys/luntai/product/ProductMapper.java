package com.pys.luntai.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT DISTINCT brand FROM tire_product WHERE deleted = 0 AND brand IS NOT NULL AND brand != '' ORDER BY brand")
    List<String> selectDistinctBrands();

    @Select("SELECT DISTINCT width FROM tire_product WHERE deleted = 0 AND width IS NOT NULL AND width != '' ORDER BY width")
    List<String> selectDistinctWidths();

    @Select("SELECT DISTINCT ratio FROM tire_product WHERE deleted = 0 AND ratio IS NOT NULL AND ratio != '' ORDER BY ratio")
    List<String> selectDistinctRatios();

    @Select("SELECT DISTINCT rim FROM tire_product WHERE deleted = 0 AND rim IS NOT NULL AND rim != '' ORDER BY rim")
    List<String> selectDistinctRims();

    @Select("SELECT DISTINCT category FROM tire_product WHERE deleted = 0 AND category IS NOT NULL AND category != '' ORDER BY category")
    List<String> selectDistinctCategories();
}
