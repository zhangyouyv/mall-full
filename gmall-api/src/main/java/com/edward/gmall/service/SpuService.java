package com.edward.gmall.service;

import com.edward.gmall.bean.PmsProductImage;
import com.edward.gmall.bean.PmsProductInfo;
import com.edward.gmall.bean.PmsProductSaleAttr;
import com.edward.gmall.bean.PmsSkuInfo;

import java.util.List;

public interface SpuService {
    List<PmsProductInfo> getSpuList(String catalog3Id);

    String saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> getSpuSaleAttrList(String spuId);

    List<PmsProductImage> getSpuImageList(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId, String skuId);

}
