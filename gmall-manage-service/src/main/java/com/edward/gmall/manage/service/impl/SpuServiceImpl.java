package com.edward.gmall.manage.service.impl;

import com.edward.gmall.bean.*;
import com.edward.gmall.manage.mapper.*;
import com.edward.gmall.service.SpuService;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    SpuMapper spuMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SpuSalesAttrMapper spuSalesAttrMapper;

    @Autowired
    SpuSalesAttrValueMapper spuSalesAttrValueMapper;

    @Autowired
    PmsProductImageMapper pmsProductImageMapper;

    @Override
    public List<PmsProductInfo> getSpuList(String catalog3Id) {
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfos = spuMapper.select(pmsProductInfo);
        return pmsProductInfos;
    }

    @Override
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {


        //插入产品信息
        int i = spuMapper.insert(pmsProductInfo);

        //获取商品id
        String productId = pmsProductInfo.getId();
        //插入图片
        for (PmsProductImage pmsProductImage : pmsProductInfo.getSpuImageList()) {
            pmsProductImage.setProductId(productId);
            spuImageMapper.insert(pmsProductImage);
        }
        //插入销售属性
        for (PmsProductSaleAttr pmsProductSaleAttr : pmsProductInfo.getSpuSaleAttrList()) {
            pmsProductSaleAttr.setProductId(productId);
            spuSalesAttrMapper.insert(pmsProductSaleAttr);
            for (PmsProductSaleAttrValue pmsProductSaleAttrValue : pmsProductSaleAttr.getSpuSaleAttrValueList()) {
                pmsProductSaleAttrValue.setProductId(productId);
                spuSalesAttrValueMapper.insert(pmsProductSaleAttrValue);
            }
        }
        //插入销售属性值

        return "success";
    }

    @Override
    public List<PmsProductSaleAttr> getSpuSaleAttrList(String spuId) {
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuSalesAttrMapper.select(pmsProductSaleAttr);
        for (PmsProductSaleAttr productSaleAttr : pmsProductSaleAttrs) {
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setProductId(spuId);
            pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());//
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValues = spuSalesAttrValueMapper.select(pmsProductSaleAttrValue);
            productSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValues);
        }

        return pmsProductSaleAttrs;
    }

    @Override
    public List<PmsProductImage> getSpuImageList(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        List<PmsProductImage> pmsProductImages = pmsProductImageMapper.select(pmsProductImage);
        return pmsProductImages;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId, String skuId) {

        //多次查询数据库导致性能低下
/*        //获取销售属性
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(productId);
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuSalesAttrMapper.select(pmsProductSaleAttr);


        for (PmsProductSaleAttr productSaleAttr : pmsProductSaleAttrs) {
            //获取属性id
            String saleAttrId = productSaleAttr.getSaleAttrId();
            //获取销售属性值
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setSaleAttrId(saleAttrId);
            pmsProductSaleAttrValue.setProductId(productId);
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValues = spuSalesAttrValueMapper.select(pmsProductSaleAttrValue);
            //给销售属性绑定销售属性值
            productSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValues);
        }*/

        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuSalesAttrMapper.spuSaleAttrListCheckBySku(productId, skuId);
        return pmsProductSaleAttrs;
    }

}
