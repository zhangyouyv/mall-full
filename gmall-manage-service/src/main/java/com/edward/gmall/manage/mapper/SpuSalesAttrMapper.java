package com.edward.gmall.manage.mapper;

import com.edward.gmall.bean.PmsProductSaleAttr;
import com.edward.gmall.bean.PmsProductSaleAttrValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SpuSalesAttrMapper extends tk.mybatis.mapper.common.Mapper<PmsProductSaleAttr> {
    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(@Param("productId") String productId, @Param("skuId")String skuId);
}
