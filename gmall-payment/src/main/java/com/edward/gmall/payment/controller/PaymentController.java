package com.edward.gmall.payment.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.edward.gmall.annotations.LoginRequired;
import com.edward.gmall.bean.OmsOrder;
import com.edward.gmall.bean.PaymentInfo;
import com.edward.gmall.payment.config.AlipayConfig;
import com.edward.gmall.payment.service.PaymentService;
import com.edward.gmall.service.OrderService;
import jodd.util.StringUtil;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;

    @Reference
    OrderService orderService;

    @Autowired
    PaymentService paymentService;


    @RequestMapping("alipay/callback/return")
    @LoginRequired(loginSuccess = true)
    public String alipayCallbackReturn(HttpServletRequest request, ModelMap modelMap) {

        //回调中获取支付宝参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        String call_back_content = request.getParameter("call_back_content");

        //回调成功表明支付成功，修改订单支付付款状态
        if (StringUtil.isNotBlank(sign)) {
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setCallbackContent(call_back_content);
            paymentInfo.setCallbackTime(new Date());

            paymentService.updatePaymentInfo(paymentInfo);

        }


        return "finish";
    }

    @RequestMapping("alipay/submit")
    @LoginRequired(loginSuccess = true)
    //不加这个注释容易导致找不到模板错误
    @ResponseBody
    public String alipaySubmit(String outTradeCode, BigDecimal totalPrice, HttpServletRequest request, ModelMap modelMap) {

        //需要获得支付宝请求的客户端，不是连接，是封装好的表单请求
        //*********************来自阿里开发文档，快速接入

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest(); //创建API对应的request

        //回调函数
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url); //在公共参数中设置回跳和通知地址

        //参数封装
        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no", outTradeCode);
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", 0.1);
        map.put("subject", "edward");

        String param = JSON.toJSONString(map);
        alipayRequest.setBizContent(param);

        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();  //调用SDK生成表单
            //System.out.println(form);
            //modelMap.put("innerHtml", form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //*********************

        //生成并保存用户支付信息
        OmsOrder omsOrder = orderService.getOrderByOutTradeCode(outTradeCode);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outTradeCode);
        paymentInfo.setPaymentStatus("正在付款中。。。");
        paymentInfo.setSubject("商品一件");
        paymentService.savePaymentInfo(paymentInfo);

        //由于调了支付宝的支付接口后需要被动的等待支付宝的回调才能知道支付结果
        //所以使用延迟队列检查支付状态
        paymentService.sendDelayPaymentCheckResult(outTradeCode, 5);
        return form;
    }


    @RequestMapping("wx/submit")
    @LoginRequired(loginSuccess = true)
    public String wxSubmit(String outTradeCode, BigDecimal totalPrice, HttpServletRequest request, ModelMap modelMap) {
        return null;
    }

    @RequestMapping("index")
    @LoginRequired(loginSuccess = true)
    public String index(String outTradeCode, BigDecimal totalPrice, HttpServletRequest request, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickName");

        modelMap.put("orderId", outTradeCode);
        modelMap.put("totalAmount", totalPrice);
        return "index";
    }
}
