package com.edward.gmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.edward.gmall.bean.OmsCartItem;
import com.edward.gmall.cart.mapper.OmsCartItemMapper;
import com.edward.gmall.service.CartService;
import com.edward.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service//(version = "1.0.0")
public class CartServiceImpl implements CartService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Override
    public OmsCartItem ifCartExistByUser(String skuId, String memberId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem omsCartItem1 = omsCartItemMapper.selectOne(omsCartItem);
        return omsCartItem1;
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {
        if (StringUtils.isNotBlank(omsCartItem.getMemberId())) {
            omsCartItemMapper.insert(omsCartItem);
        }
    }

    @Override
    public void updateCart(OmsCartItem omsCartItemFromDb) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("id", omsCartItemFromDb.getId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDb, example);
    }

    @Override
    public void flushCartCache(String memberId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);

        //同步到缓存中
        Jedis jedis = redisUtil.getJedis();

        Map<String, String> hashMap = new HashMap();
        for (OmsCartItem cartItem : omsCartItems) {
            hashMap.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
        }

        jedis.del("user:" + memberId + ":cart");
        jedis.hmset("user:" + memberId + ":cart", hashMap);

        jedis.close();
    }

    @Override
    public List<OmsCartItem> cartList(String memberId) {
        List<OmsCartItem> omsCartItems = new ArrayList();
        Jedis jedis = redisUtil.getJedis();
        List<String> hvalues = jedis.hvals("user:" + memberId + ":cart");
        for (String hvalue : hvalues) {
            OmsCartItem omsCartItem1 = JSON.parseObject(hvalue, OmsCartItem.class);
            omsCartItems.add(omsCartItem1);
        }

        jedis.close();
        return omsCartItems;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId", omsCartItem.getMemberId()).andEqualTo("productSkuId", omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem, example);

        //同步缓存
        flushCartCache(omsCartItem.getMemberId());
    }

}
