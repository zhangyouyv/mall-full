package com.edward.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.edward.gmall.annotations.LoginRequired;
import com.edward.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import utils.HttpclientUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //拦截的逻辑

        //判断被拦截的请求方法的注解（是否是需要拦截的）通过反射来得到。
        HandlerMethod handler1 = (HandlerMethod) handler;
        LoginRequired methodAnnotation = handler1.getMethodAnnotation(LoginRequired.class);

        //System.out.println(request.getRequestURL());

        //是否拦截
        if (methodAnnotation == null) {
            return true;
        }

        String token = "";

        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);

        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }

        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }

        //是否必须登录
        boolean loginSuccess = methodAnnotation.loginSuccess();

        //调用应用中心进行验证
        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isNotBlank(ip)) {
            ip = request.getRemoteAddr();
        }
        //实际工作中应该抛出异常
        if (StringUtils.isBlank(ip)) {
            ip = "127.0.0.1";
        }
        String success = "fail";
        Map<String, String> successMap = new HashMap<>();
        if (StringUtils.isNotBlank(token)) {
            String successJson = HttpclientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token + "&currentIP=" + ip);
            successMap = JSON.parseObject(successJson, Map.class);
            success = successMap.get("status");
        }

        if (loginSuccess) {
            //必须登录成功才能使用
            if (!success.equals("success")) {
                //验证失败踢回验证中心
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl=" + requestURL);
                return false;
            }
            //验证通过，覆盖cookie中的token
            request.setAttribute("memberId", successMap.get("memberId"));
            request.setAttribute("nickName", successMap.get("nickName"));
            if (StringUtils.isNotBlank(token)) {
                CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 3, true);
            }

        } else {
            //没有登录也要验证，因为影响购物车的分支
            if (success.equals("success")) {
                //需要将token携带的用户信息写入
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickName", successMap.get("nickName"));
                if (StringUtils.isNotBlank(token)) {
                    CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 3, true);
                }
            }

        }


        return true;
    }
}
