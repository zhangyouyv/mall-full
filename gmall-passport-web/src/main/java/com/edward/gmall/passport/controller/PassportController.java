package com.edward.gmall.passport.controller;

import com.alibaba.fastjson.JSON;
import com.edward.gmall.annotations.LoginRequired;
import com.edward.gmall.bean.UmsMember;
import com.edward.gmall.service.UserService;
import com.edward.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin
public class PassportController {
    @Reference
    UserService userService;

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token, HttpServletRequest request, String currentIP) {
        //验证jwt的真假
        Map<String, String> map = new HashMap<>();
        Map<String, Object> gmall = JwtUtil.decode(token, "gmall", currentIP);

        if (gmall != null) {
            map.put("status", "success");
            map.put("memberId", (String) gmall.get("memberId"));
            map.put("nickname", (String) gmall.get("nickName"));
        } else {
            map.put("status", "fail");
        }

        return JSON.toJSONString(map);
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {
        //验证登录用户的账户和密码
        UmsMember umsMember1 = userService.login(umsMember);

        String token = "";
        if (umsMember1 != null) {
            //登陆成功

            //用jwt生成token
            String memberId = umsMember1.getId();
            String nickName = umsMember1.getNickname();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("memberId", memberId);
            userMap.put("nickName", nickName);

            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isNotBlank(ip)) {
                ip = request.getRemoteAddr();
            }
            //实际工作中应该抛出异常
            if (StringUtils.isBlank(ip)) {
                ip = "127.0.0.1";
            }

            token = JwtUtil.encode("gmall", userMap, ip);
            //存一份token放到redis
            userService.addUserToken(token, memberId);
        } else {
            //登录失败
            token = "fail";

        }
        return token;
    }

    @RequestMapping("index")
    @LoginRequired(loginSuccess = false)
    public String getIndex(String ReturnUrl, ModelMap modelMap) {
        modelMap.put("ReturnUrl", ReturnUrl);
        return "index";
    }
}
