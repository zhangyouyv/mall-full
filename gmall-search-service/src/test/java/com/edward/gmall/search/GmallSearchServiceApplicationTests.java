package com.edward.gmall.search;

import com.edward.gmall.bean.PmsSearchSkuInfo;
import com.edward.gmall.bean.PmsSkuInfo;
import com.edward.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallSearchServiceApplicationTests {

    @Reference
    SkuService skuService;

    @Autowired
    JestClient jestClient;

    @Test
    public void contextLoads() throws IOException {
        //查询mysql
        List<PmsSkuInfo> pmsSkuInfos = new ArrayList<>();
        pmsSkuInfos = skuService.getAll();
        //转化成es数据结构
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSkuInfo, pmsSearchSkuInfos);
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
        }

        //插入到es
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            Index put = new Index.Builder(pmsSkuInfo).index("gmall").type("PmsSkuInfo").id(pmsSkuInfo.getId()).build();
            jestClient.execute(put);
        }

    }

}
