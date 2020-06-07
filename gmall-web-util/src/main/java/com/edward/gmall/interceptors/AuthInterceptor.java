package com.edward.gmall.interceptors;

import com.edward.gmall.annotations.LoginRequired;
import com.edward.gmall.util.CookieUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{


public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    //拦截的逻辑

    //判断被拦截的请求方法的注解（是否是需要拦截的）通过反射来得到。
    HandlerMethod handler1 = (HandlerMethod) handler;
    LoginRequired methodAnnotation = handler1.getMethodAnnotation(LoginRequired.class);

    if(methodAnnotation==null){
        return true;
    }

    System.out.println("进入拦截器的拦截");


    return true;
}
}
