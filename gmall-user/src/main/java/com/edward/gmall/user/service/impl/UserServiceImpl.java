package com.edward.gmall.user.service.impl;


import com.alibaba.fastjson.JSON;
import com.edward.gmall.bean.UmsMember;
import com.edward.gmall.bean.UmsMemberReceiveAddress;
import com.edward.gmall.service.UserService;
import com.edward.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.edward.gmall.user.mapper.UserMapper;
import com.edward.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UmsMember> getAllUser() {
        List<UmsMember> umsMembers = userMapper.selectAllUser();
        return umsMembers;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        //List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.selectByExample(umsMemberReceiveAddress);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);
        return umsMemberReceiveAddresses;
    }

    @Override
    public UmsMember login(UmsMember umsMember) {

        Jedis jedis = redisUtil.getJedis();
    try {
        if (jedis != null) {
            String umsMemberStr = jedis.get("user:" + umsMember.getPassword() + ":info");
            if (StringUtils.isNotBlank(umsMemberStr)) {
                //密码正确
                UmsMember umsMemberFromCache = JSON.parseObject(umsMemberStr, UmsMember.class);
                return umsMemberFromCache;
            } else {
                //密码错误
                //缓存中没有
                //查询数据库
                UmsMember umsMemberFromDB = loginFromDB(umsMember);
                if(umsMemberFromDB!=null){
                    jedis.setex("user:"+umsMember.getPassword(), 60*60*24, JSON.toJSONString(umsMemberFromDB));
                }
                return umsMemberFromDB;
            }
        } else {
            //查数据库
            UmsMember umsMemberFromDB = loginFromDB(umsMember);
            if(umsMemberFromDB!=null){
                jedis.setex("user:"+umsMember.getPassword(), 60*60*24, JSON.toJSONString(umsMemberFromDB));
            }
            return umsMemberFromDB;
        }
    }finally {
        jedis.close();
    }

    }

    private UmsMember loginFromDB(UmsMember umsMember) {
        UmsMember umsMember1 = new UmsMember();
        umsMember1.setUsername(umsMember.getUsername());
        List<UmsMember> users = userMapper.select(umsMember1);

        if(users!=null){
            return users.get(0);
        }

        return null;
    }
}
