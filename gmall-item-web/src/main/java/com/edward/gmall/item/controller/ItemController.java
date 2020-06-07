package com.edward.gmall.item.controller;

import com.alibaba.fastjson.JSON;
import com.edward.gmall.bean.*;
import com.edward.gmall.service.SkuService;
import com.edward.gmall.service.SpuService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
public class ItemController {

    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;

    @RequestMapping("{skuId}.html")
    //@ResponseBody --导致返回前端的不是视图而是json
    public String getItem(@PathVariable String skuId,ModelMap modelMap){
        PmsSkuInfo pmsSkuInfo = skuService.getItemById(skuId);
        modelMap.put("skuInfo", pmsSkuInfo);

        //销售属性列表
        List<PmsProductSaleAttr> spuSaleAttrList = spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(),skuId);
        modelMap.put("spuSaleAttrListCheckBySku", spuSaleAttrList);

        //查询当前sku的spu的其他sku的集合的属性hash表，也就是兄弟姐妹商品,
        // 这样就可以根据所选属性更新当前页面而不需要重复查询数据库
        Map skuSaleAttr = new HashMap<String,String>();
        List<PmsSkuInfo> pmsSkuInfos = skuService.getSkuSaleAttrValueListCheckBySku(pmsSkuInfo.getProductId());
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String k = "";
            String v = skuInfo.getId();

            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                k+=pmsSkuSaleAttrValue.getSaleAttrValueId()+"|";
            }
            skuSaleAttr.put(k, v);

            //将hash表放到页面
            String skuSaleAttrHashJsonStr = JSON.toJSONString(skuSaleAttr);
            modelMap.put("skuSaleAttrHashJsonStr",skuSaleAttrHashJsonStr);
        }
        return "item";
    }
}
