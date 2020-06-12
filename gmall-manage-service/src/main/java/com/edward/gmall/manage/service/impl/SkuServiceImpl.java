package com.edward.gmall.manage.service.impl;


import com.alibaba.fastjson.JSON;
import com.edward.gmall.bean.PmsSkuAttrValue;
import com.edward.gmall.bean.PmsSkuImage;
import com.edward.gmall.bean.PmsSkuInfo;
import com.edward.gmall.bean.PmsSkuSaleAttrValue;
import com.edward.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.edward.gmall.manage.mapper.PmsSkuImageMapper;
import com.edward.gmall.manage.mapper.PmsSkuInfoMapper;
import com.edward.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.edward.gmall.service.SkuService;
import com.edward.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        //插入sku信息
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());
        pmsSkuInfoMapper.insert(pmsSkuInfo);

        //获取skuid

        String skuId = pmsSkuInfo.getId();
        //插入sku图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insert(pmsSkuImage);
        }


        //插入平台属性信息
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insert(pmsSkuAttrValue);
        }
        //插入销售属性信息
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insert(pmsSkuSaleAttrValue);
        }
        return "success";
    }

    public PmsSkuInfo getItemByIdFromDB(String skuId) {
        //高并发会导致服务器压力过大
        //获取商品详情
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        pmsSkuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //获取图片集合
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
        pmsSkuInfo.setSkuImageList(pmsSkuImages);


        return pmsSkuInfo;
    }

    @Override
    public PmsSkuInfo getItemById(String skuId) {
        //高并发会导致服务器压力过大

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //使用缓存
        //链接缓存
        Jedis jedis = redisUtil.getJedis();

        //查询缓存
        //拼接key
        String skuKey = "sku:" + skuId + "info";
        String skuJson = jedis.get(skuKey);

        if (StringUtils.isNotBlank(skuJson)) {
            pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
        } else {
            //需要设置redis分布式锁来防止缓存击穿问题
            //为了防止误删锁，我们需要给锁加上识别码
            String token = UUID.randomUUID().toString();
            String lock = jedis.set("sku:" + skuId + ":redisLock", token, "nx", "px", 1000);
            if (StringUtils.isNotBlank(lock) && "OK".equals(lock)) {

                //如果缓存中没有，查询mysql
                pmsSkuInfo = getItemByIdFromDB(skuId);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //mysql查询结果存入redis
                if (pmsSkuInfo != null) {
                    jedis.set("sku:" + skuId + ":info", JSON.toJSONString(pmsSkuInfo));
                } else {
                    //数据库中不存在改sku
                    //为了防止缓存穿透，将null或者空字符串设置给redis
                    jedis.setex("sku:" + skuId + ":info", 60 * 3, JSON.toJSONString(""));
                }
                //删锁之前验证token
                String lockToken = jedis.get("sku:" + skuId + ":redisLock");
                if (StringUtils.isNotBlank(lockToken) && token.equals(lockToken)) {
                    //jedis.eval("lua");//使用lua脚本，查询到key的同时删除该key，防止高并发下的线程问题
                    //在访问mysql后，删除锁
                    jedis.del("sku:" + skuId + ":info");
                }
            } else {
                //失败后自旋

                return getItemById(skuId);
            }
        }
        //最后要关闭链接,一般写在finally里保证必然执行
        jedis.close();


        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListCheckBySku(String productId) {
        List<PmsSkuInfo> pmsSkuInfs = pmsSkuInfoMapper.selectSkuSaleAttrValueListCheckBySku(productId);
        return pmsSkuInfs;
    }

    @Override
    public List<PmsSkuInfo> getAll() {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            List<PmsSkuAttrValue> pmsSkuAttrValueList = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValueList);
        }

        return pmsSkuInfos;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal price) {
        //消极处理，不会出错
        boolean b = false;

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        //金钱价格的比较，必须使用big decimal
        if (price.compareTo(pmsSkuInfo1.getPrice()) == 0) {
            b = true;
        }
        return b;
    }
}
