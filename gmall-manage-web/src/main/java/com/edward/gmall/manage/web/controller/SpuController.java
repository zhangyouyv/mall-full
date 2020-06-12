package com.edward.gmall.manage.web.controller;

import com.edward.gmall.bean.PmsProductImage;
import com.edward.gmall.bean.PmsProductInfo;
import com.edward.gmall.bean.PmsProductSaleAttr;
import com.edward.gmall.service.SpuService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    SpuService spuService;

    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> getSpuList(String catalog3Id) {
        List<PmsProductInfo> pmsProductInfos = spuService.getSpuList(catalog3Id);
        return pmsProductInfos;
    }

    //新建spu
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo) {
        spuService.saveSpuInfo(pmsProductInfo);
        return "success";
    }

    //查询销售属性
    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> getSpuSaleAttrList(String spuId) {
        List<PmsProductSaleAttr> saleAttrList = spuService.getSpuSaleAttrList(spuId);
        return saleAttrList;
    }


    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> getSpuImageList(String spuId) {
        List<PmsProductImage> pmsProductImages = spuService.getSpuImageList(spuId);
        return pmsProductImages;
    }
}
