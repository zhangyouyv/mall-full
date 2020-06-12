package com.edward.gmall.passport.controller;

import com.alibaba.fastjson.JSON;
import utils.HttpclientUtil;

import java.util.Map;

public class TestOauth2 {
    public static void main(String[] args) {
        //2269378644
        //http://passport.gmall.com:8085/vlogin
        //code: bf62d45bbad95fb1a9b3dda9ce9a2f83
//        HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=2269378644&response_type=code&redirect_uri=http://passport.gmall.com:8085/vlogin");
//        Map<String,String> map = new HashMap<>();
//        map.put("client_id", "2269378644");
//        map.put("client_secret", "b564f01f645a05eef417bf84e29f856e");
//        map.put("grant_type", "authorization_code");
//        map.put("redirect_uri", "http://passport.gmall.com:8085/vlogin");
//        map.put("code", "667b509253bbdaa5371bc6b4d2b1e88e");
//        String access_token = HttpclientUtil.doPost("https://api.weibo.com/oauth2/access_token?client_id=2269378644&client_secret=b564f01f645a05eef417bf84e29f856e&grant_type=authorization_code&redirect_uri=http://passport.gmall.com:8085/vlogin&code=667b509253bbdaa5371bc6b4d2b1e88e", map);
//
//       // {"access_token":"2.00zL9svG3DFaTCe497849dd9GO9MyD","remind_in":"157679999","expires_in":157679999,"uid":"6352026231","isRealName":"true"}
//        Map<String,String> access_map = JSON.parseObject(access_token, Map.class);
//        System.out.println(access_map.get("access_token"));

        //用token查询用户信息
        String s = "https://api.weibo.com/2/users/show.json?access_token=2.00zL9svG3DFaTCe497849dd9GO9MyD&uid=6352026231";
        String userjson = HttpclientUtil.doGet(s);
        Map<String, String> user_map = JSON.parseObject(userjson, Map.class);
        System.out.println(user_map.get(1));
    }
}
