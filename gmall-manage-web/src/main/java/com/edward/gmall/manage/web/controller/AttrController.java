package com.edward.gmall.manage.web.controller;

import com.edward.gmall.bean.PmsBaseAttrInfo;
import com.edward.gmall.bean.PmsBaseAttrValue;
import com.edward.gmall.bean.PmsBaseSaleAttr;
import com.edward.gmall.service.AttrService;
import org.apache.dubbo.config.annotation.Reference;
import org.csource.common.MyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import utils.UploadUtil;

import java.io.IOException;
import java.util.List;

@Controller
@CrossOrigin
public class AttrController {

    @Reference
    AttrService attrService;

//    @Autowired
//    UploadUtil uploadUtil;

    //属性列表
    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<PmsBaseAttrInfo> getCatalog2(String catalog3Id) {

        List<PmsBaseAttrInfo> pmsBaseAttrInfo = attrService.getAttrInfoList(catalog3Id);
        return pmsBaseAttrInfo;
    }

    //添加属性列表
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo) {
        //String类型可以有多种状态
        String success = attrService.saveAttrInfo(pmsBaseAttrInfo);
        return success;
    }


    //获取产品属性列表
    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        List<PmsBaseAttrValue> pmsBaseAttrValues = attrService.getAttrValueList(attrId);
        return pmsBaseAttrValues;
    }

    //获取基本产品属性列表
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> getbaseSaleAttrList() {
        //String类型可以有多种状态
        List<PmsBaseSaleAttr> pmsBaseSaleAttrs = attrService.getBaseSaleAttrList();
        return pmsBaseSaleAttrs;
    }


    //上传图片
    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException, MyException {
        //上传图片到分布式服务器,如果返回成功就说明文件已经上传到文件服务器
        String imgUrl = UploadUtil.uploadImage(file);
        //把url返回给前端
        return imgUrl;
    }
}
