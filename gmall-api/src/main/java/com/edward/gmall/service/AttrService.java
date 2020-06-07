package com.edward.gmall.service;

import com.edward.gmall.bean.PmsBaseAttrInfo;
import com.edward.gmall.bean.PmsBaseAttrValue;
import com.edward.gmall.bean.PmsBaseSaleAttr;
import com.edward.gmall.bean.PmsProductInfo;

import java.util.List;
import java.util.Set;

public interface AttrService {
    List<PmsBaseAttrInfo> getAttrInfoList(String catalog3Id);

    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    List<PmsBaseSaleAttr> getBaseSaleAttrList();

    List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueSet);
}
