package com.edward.gmall.service;

import com.edward.gmall.bean.PmsSearchParam;
import com.edward.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {

    List<PmsSearchSkuInfo> getList(PmsSearchParam pmsSearchParam);
}
