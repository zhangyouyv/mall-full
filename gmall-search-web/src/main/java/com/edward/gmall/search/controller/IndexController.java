package com.edward.gmall.search.controller;

import com.edward.gmall.bean.PmsBaseAttrInfo;
import com.edward.gmall.bean.PmsSearchParam;
import com.edward.gmall.bean.PmsSearchSkuInfo;
import com.edward.gmall.bean.PmsSkuAttrValue;
import com.edward.gmall.service.AttrService;
import com.edward.gmall.service.SearchService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@CrossOrigin
public class IndexController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    public String getList(PmsSearchParam pmsSearchParam, ModelMap modelMap){
        //调用搜索服务，返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = searchService.getList(pmsSearchParam);
        modelMap.put("skuLsInfoList",pmsSearchSkuInfoList);

        //利用set集合去重或者es的聚合函数对涉及到的valueid去重
        Set<String> valueSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueSet.add(valueId);
            }
        }
        //根据value查询属性值
        List<PmsBaseAttrInfo> pmsBaseAttrInfoList = attrService.getAttrValueListByValueId(valueSet);
        modelMap.put("attrList", pmsBaseAttrInfoList);
        return "list";
    }
    @RequestMapping("index")
    public String getIndex(){
        return "index";
    }
}
